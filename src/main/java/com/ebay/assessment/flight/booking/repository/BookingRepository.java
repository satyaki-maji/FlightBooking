package com.ebay.assessment.flight.booking.repository;

import com.ebay.assessment.flight.booking.domain.Booking;

import java.util.List;
import java.util.Optional;

/**
 * Port (abstraction) through which the domain/service layer accesses
 * booking persistence.
 *
 * <p>Following the <strong>Dependency Inversion Principle</strong>,
 * higher-level modules depend on this interface — never on a concrete
 * storage mechanism.  Implementations may be backed by an in-memory
 * map, an RDBMS, a document store, or an external API without
 * requiring any change to the consuming code.</p>
 */
public interface BookingRepository {

    /**
     * Persists a new or fully-replaced booking in the store, keyed by
     * its booking ID.
     *
     * <p>Because {@link Booking} is immutable, an "update" is always
     * a full replacement.</p>
     *
     * @param booking the booking to save; must not be {@code null}
     * @return the booking that was saved
     */
    Booking save(Booking booking);

    /**
     * Looks up a single booking by its unique identifier.
     *
     * @param bookingId the booking's unique ID
     * @return an {@link Optional} containing the booking if found,
     *         or {@link Optional#empty()} otherwise
     */
    Optional<Booking> findById(String bookingId);

    /**
     * Returns every booking currently held in the store.
     *
     * @return an unmodifiable list of all bookings; never {@code null}
     */
    List<Booking> findAll();

    /**
     * Removes the booking identified by the given ID from the store,
     * if it exists.
     *
     * @param bookingId the booking ID to delete
     * @return {@code true} if the booking was present and removed,
     *         {@code false} if no such booking existed
     */
    boolean deleteById(String bookingId);
}
