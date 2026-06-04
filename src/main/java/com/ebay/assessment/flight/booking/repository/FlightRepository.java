package com.ebay.assessment.flight.booking.repository;

import com.ebay.assessment.flight.booking.domain.Flight;

import java.util.List;
import java.util.Optional;

/**
 * Port (abstraction) through which the domain/service layer accesses
 * flight persistence.
 *
 * <p>Following the <strong>Dependency Inversion Principle</strong>,
 * higher-level modules depend on this interface — never on a concrete
 * storage mechanism.  Implementations may be backed by an in-memory
 * map, an RDBMS, a document store, or an external API without
 * requiring any change to the consuming code.</p>
 */
public interface FlightRepository {

    /**
     * Returns every flight currently held in the store.
     *
     * @return an unmodifiable list of all flights; never {@code null}
     */
    List<Flight> findAll();

    /**
     * Looks up a single flight by its unique flight number.
     *
     * @param flightNumber the IATA-style flight number (e.g. {@code "UK-835"})
     * @return an {@link Optional} containing the flight if found,
     *         or {@link Optional#empty()} otherwise
     */
    Optional<Flight> findByFlightNumber(String flightNumber);

    /**
     * Finds every flight departing from the given source city and
     * arriving at the given destination city.
     *
     * @param sourceCity      departure city name (e.g. {@code "Bengaluru"})
     * @param destinationCity arrival city name   (e.g. {@code "New Delhi"})
     * @return a list of matching flights; may be empty, never {@code null}
     */
    List<Flight> findBySourceCityAndDestinationCity(String sourceCity, String destinationCity);

    /**
     * Persists a new or fully-replaced flight in the store, keyed by
     * its flight number.
     *
     * <p>Because the {@link Flight} domain object is immutable, an
     * "update" is always a full replacement: callers rebuild the
     * object via its builder (e.g. decrementing available seats) and
     * pass the new instance here.</p>
     *
     * @param flight the flight to save; must not be {@code null}
     * @return the flight that was saved
     */
    Flight save(Flight flight);
}
