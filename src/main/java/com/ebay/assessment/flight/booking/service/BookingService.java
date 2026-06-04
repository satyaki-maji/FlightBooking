package com.ebay.assessment.flight.booking.service;

import com.ebay.assessment.flight.booking.domain.Booking;
import com.ebay.assessment.flight.booking.domain.Flight;
import com.ebay.assessment.flight.booking.domain.Passenger;
import com.ebay.assessment.flight.booking.exception.FlightFullyBookedException;
import com.ebay.assessment.flight.booking.repository.FlightRepository;
import com.ebay.assessment.flight.booking.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
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

        log.info("Booking [{}] confirmed – passenger [{}] on flight [{}], seat [{}]",
                booking.getBookingId(),
                passenger.getPassengerId(),
                flightNumber,
                seatNumber);

        return booking;
    }
}
