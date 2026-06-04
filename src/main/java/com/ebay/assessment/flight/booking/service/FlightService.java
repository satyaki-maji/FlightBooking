package com.ebay.assessment.flight.booking.service;

import com.ebay.assessment.flight.booking.domain.Flight;
import com.ebay.assessment.flight.booking.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Application service encapsulating full CRUD operations for
 * {@link Flight} entities.
 *
 * <p>All persistence is delegated to the injected
 * {@link FlightRepository}, keeping this class free of storage
 * concerns and easy to test in isolation.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

    /* ------------------------------------------------------------------ */
    /*  Create                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Persists a new flight.
     *
     * @param flight the flight to create; must not be {@code null}
     * @return the persisted flight
     */
    public Flight createFlight(Flight flight) {
        log.info("Creating flight [{}]", flight.getFlightNumber());
        return flightRepository.save(flight);
    }

    /* ------------------------------------------------------------------ */
    /*  Read                                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Returns every flight in the store.
     *
     * @return an unmodifiable list of all flights; never {@code null}
     */
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    /**
     * Looks up a single flight by its flight number.
     *
     * @param flightNumber the IATA-style flight number
     * @return an {@link Optional} containing the flight, or empty
     */
    public Optional<Flight> getFlightByNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }

    /**
     * Finds flights operating between two cities.
     *
     * @param sourceCity      departure city
     * @param destinationCity arrival city
     * @return matching flights; may be empty, never {@code null}
     */
    public List<Flight> getFlightsByRoute(String sourceCity, String destinationCity) {
        return flightRepository.findBySourceCityAndDestinationCity(sourceCity, destinationCity);
    }

    /* ------------------------------------------------------------------ */
    /*  Update                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Replaces an existing flight with the supplied instance.
     *
     * <p>Because {@link Flight} is immutable, an "update" is always a
     * full replacement keyed by flight number.  If no flight with the
     * given number exists, a {@link NoSuchElementException} is
     * thrown.</p>
     *
     * @param flight the replacement flight
     * @return the saved flight
     * @throws NoSuchElementException if the flight number is not found
     */
    public Flight updateFlight(Flight flight) {
        String number = flight.getFlightNumber();
        if (flightRepository.findByFlightNumber(number).isEmpty()) {
            throw new NoSuchElementException("Flight not found: " + number);
        }
        log.info("Updating flight [{}]", number);
        return flightRepository.save(flight);
    }

    /* ------------------------------------------------------------------ */
    /*  Delete                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Deletes the flight identified by the given flight number.
     *
     * @param flightNumber the flight number to remove
     * @return {@code true} if the flight existed and was removed
     * @throws NoSuchElementException if no flight with that number exists
     */
    public boolean deleteFlight(String flightNumber) {
        if (flightRepository.findByFlightNumber(flightNumber).isEmpty()) {
            throw new NoSuchElementException("Flight not found: " + flightNumber);
        }
        log.info("Deleting flight [{}]", flightNumber);
        return flightRepository.deleteByFlightNumber(flightNumber);
    }
}
