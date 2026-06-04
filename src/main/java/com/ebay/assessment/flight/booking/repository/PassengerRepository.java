package com.ebay.assessment.flight.booking.repository;

import com.ebay.assessment.flight.booking.domain.Passenger;

import java.util.List;
import java.util.Optional;

/**
 * Port (abstraction) through which the domain/service layer accesses
 * passenger persistence.
 *
 * <p>Mirrors the design of {@link FlightRepository}: higher-level
 * modules depend on this interface and remain agnostic of the backing
 * storage technology.</p>
 */
public interface PassengerRepository {

    /**
     * Returns every passenger currently held in the store.
     *
     * @return an unmodifiable list of all passengers; never {@code null}
     */
    List<Passenger> findAll();

    /**
     * Looks up a single passenger by their unique identifier.
     *
     * @param passengerId the passenger's unique ID
     * @return an {@link Optional} containing the passenger if found,
     *         or {@link Optional#empty()} otherwise
     */
    Optional<Passenger> findByPassengerId(String passengerId);

    /**
     * Persists a new or fully-replaced passenger in the store, keyed
     * by their passenger ID.
     *
     * <p>Because {@link Passenger} is immutable, an "update" is always
     * a full replacement.</p>
     *
     * @param passenger the passenger to save; must not be {@code null}
     * @return the passenger that was saved
     */
    Passenger save(Passenger passenger);

    /**
     * Removes the passenger identified by the given ID from the store,
     * if they exist.
     *
     * @param passengerId the passenger ID to delete
     * @return {@code true} if the passenger was present and removed,
     *         {@code false} if no such passenger existed
     */
    boolean deleteByPassengerId(String passengerId);
}
