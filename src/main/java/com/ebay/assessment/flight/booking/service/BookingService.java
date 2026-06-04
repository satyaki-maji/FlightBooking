package com.ebay.assessment.flight.booking.service;

import com.ebay.assessment.flight.booking.domain.Booking;
import com.ebay.assessment.flight.booking.domain.Flight;
import com.ebay.assessment.flight.booking.domain.Passenger;
import com.ebay.assessment.flight.booking.exception.FlightFullyBookedException;
import com.ebay.assessment.flight.booking.repository.BookingRepository;
import com.ebay.assessment.flight.booking.repository.FlightRepository;
import com.ebay.assessment.flight.booking.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service responsible for booking passengers onto flights.
 *
 * <p>Thread safety under high concurrency is guaranteed by the design
 * of {@link Flight#getAvailableSeatNumbers()}: each flight owns a
 * {@link java.util.concurrent.ConcurrentLinkedQueue ConcurrentLinkedQueue}
 * whose {@code poll()} operation is atomic and lock-free.  Two threads
 * racing to book the last seat will never receive the same seat number;
 * exactly one will get the seat and the other will observe {@code null}
 * and receive a {@link FlightFullyBookedException}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Double DEFAULT_TICKET_PRICE = 5_500.00;
    private static final String CONFIRMED_STATUS     = "CONFIRMED";

    private final FlightRepository    flightRepository;
    private final PassengerRepository passengerRepository;
    private final BookingRepository   bookingRepository;

    /* ------------------------------------------------------------------ */
    /*  Booking                                                           */
    /* ------------------------------------------------------------------ */

    /**
     * Books a passenger onto the specified flight.
     *
     * <h4>Algorithm</h4>
     * <ol>
     *   <li>Look up the flight by number — fail fast with
     *       {@link NoSuchElementException} if it does not exist.</li>
     *   <li>Atomically {@code poll()} the next available seat number
     *       from the flight's {@code ConcurrentLinkedQueue}.  If the
     *       queue is empty (i.e. {@code poll()} returns {@code null}),
     *       throw {@link FlightFullyBookedException}.</li>
     *   <li>Assign the passenger a generated UUID as their ID and
     *       persist them via {@link PassengerRepository}.</li>
     *   <li>Assemble and return a fully-hydrated, immutable
     *       {@link Booking} containing the generated booking UUID,
     *       the unique seat number, the current timestamp, a ticket
     *       price, and a {@code "CONFIRMED"} status.</li>
     * </ol>
     *
     * @param flightNumber     the IATA-style flight number to book
     * @param passengerDetails passenger information supplied by the caller;
     *                         the {@code passengerId} field may be
     *                         {@code null} — a UUID will be generated
     * @return an immutable {@link Booking} with status {@code CONFIRMED}
     * @throws NoSuchElementException      if no flight with the given
     *                                     number exists
     * @throws FlightFullyBookedException  if the flight has no remaining
     *                                     seats
     */
    public Booking bookFlight(String flightNumber, Passenger passengerDetails) {

        // 1. Resolve the flight ──────────────────────────────────────────
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new NoSuchElementException(
                        "Flight not found: " + flightNumber));

        // 2. Atomically claim a seat (lock-free, thread-safe) ───────────
        String seatNumber = flight.getAvailableSeatNumbers().poll();
        if (seatNumber == null) {
            throw new FlightFullyBookedException(
                    "Flight " + flightNumber + " is fully booked — no seats remaining");
        }

        log.info("Seat [{}] claimed on flight [{}]", seatNumber, flightNumber);

        // 3. Persist the passenger ──────────────────────────────────────
        Passenger passenger = Passenger.builder()
                .passengerId(UUID.randomUUID().toString())
                .fullName(passengerDetails.getFullName())
                .mobileNumber(passengerDetails.getMobileNumber())
                .address(passengerDetails.getAddress())
                .checkInTime(passengerDetails.getCheckInTime())
                .build();

        passengerRepository.save(passenger);

        // 4. Assemble the immutable Booking ─────────────────────────────
        Booking booking = Booking.builder()
                .bookingId(UUID.randomUUID().toString())
                .flight(flight)
                .passenger(passenger)
                .seatNumber(seatNumber)
                .ticketPrice(DEFAULT_TICKET_PRICE)
                .bookingStatus(CONFIRMED_STATUS)
                .bookingTimestamp(LocalDateTime.now())
                .build();

        // 5. Persist the booking ───────────────────────────────────────
        bookingRepository.save(booking);

        log.info("Booking [{}] confirmed – passenger [{}] on flight [{}], seat [{}]",
                booking.getBookingId(),
                passenger.getPassengerId(),
                flightNumber,
                seatNumber);

        return booking;
    }

    /* ------------------------------------------------------------------ */
    /*  Query                                                             */
    /* ------------------------------------------------------------------ */

    /**
     * Looks up a single booking by its unique identifier.
     *
     * @param bookingId the booking's unique ID
     * @return an {@link Optional} containing the booking if found,
     *         or {@link Optional#empty()} otherwise
     */
    public Optional<Booking> getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }

    /* ------------------------------------------------------------------ */
    /*  Cancellation                                                      */
    /* ------------------------------------------------------------------ */

    /**
     * Cancels an existing booking and returns the claimed seat back to
     * the flight's available-seat pool.
     *
     * <h4>Algorithm</h4>
     * <ol>
     *   <li>Retrieve the {@link Booking} by its ID — fail fast with
     *       {@link NoSuchElementException} if it does not exist.</li>
     *   <li>Remove the booking from the {@link BookingRepository}.</li>
     *   <li>Resolve the associated {@link Flight} from the
     *       {@link FlightRepository} — fail fast if the flight no
     *       longer exists.</li>
     *   <li>Atomically return the previously assigned seat number to
     *       the flight's {@link java.util.concurrent.ConcurrentLinkedQueue}
     *       via {@code offer()}, making it immediately available for
     *       re-booking by another thread.</li>
     * </ol>
     *
     * @param bookingId the unique identifier of the booking to cancel
     * @return the cancelled {@link Booking}
     * @throws NoSuchElementException if no booking or associated flight
     *                                with the given identifiers exists
     */
    public Booking cancelBooking(UUID bookingId) {

        // 1. Retrieve the booking ────────────────────────────────────────
        Booking booking = bookingRepository.findById(bookingId.toString())
                .orElseThrow(() -> new NoSuchElementException(
                        "Booking not found: " + bookingId));

        // 2. Remove the booking from the repository ─────────────────────
        bookingRepository.deleteById(bookingId.toString());

        log.info("Booking [{}] removed from repository", bookingId);

        // 3. Locate the matching flight ─────────────────────────────────
        String flightNumber = booking.getFlight().getFlightNumber();
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new NoSuchElementException(
                        "Flight not found: " + flightNumber));

        // 4. Atomically return the seat to the flight's pool ────────────
        flight.getAvailableSeatNumbers().offer(booking.getSeatNumber());

        log.info("Booking [{}] cancelled – seat [{}] returned to flight [{}]",
                bookingId,
                booking.getSeatNumber(),
                flightNumber);

        return booking;
    }
}
