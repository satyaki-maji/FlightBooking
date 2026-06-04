package com.ebay.assessment.flight.booking.service;

import com.ebay.assessment.flight.booking.domain.Passenger;
import com.ebay.assessment.flight.booking.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Application service encapsulating full CRUD operations for
 * {@link Passenger} entities.
 *
 * <p>All persistence is delegated to the injected
 * {@link PassengerRepository}, keeping this class free of storage
 * concerns and easy to test in isolation.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;

    /* ------------------------------------------------------------------ */
    /*  Create                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Persists a new passenger.
     *
     * @param passenger the passenger to create; must not be {@code null}
     * @return the persisted passenger
     */
    public Passenger createPassenger(Passenger passenger) {
        log.info("Creating passenger [{}]", passenger.getPassengerId());
        return passengerRepository.save(passenger);
    }

    /* ------------------------------------------------------------------ */
    /*  Read                                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Returns every passenger in the store.
     *
     * @return an unmodifiable list of all passengers; never {@code null}
     */
    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    /**
     * Looks up a single passenger by their unique ID.
     *
     * @param passengerId the passenger's unique identifier
     * @return an {@link Optional} containing the passenger, or empty
     */
    public Optional<Passenger> getPassengerById(String passengerId) {
        return passengerRepository.findByPassengerId(passengerId);
    }

    /* ------------------------------------------------------------------ */
    /*  Update                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Replaces an existing passenger with the supplied instance.
     *
     * <p>Because {@link Passenger} is immutable, an "update" is always
     * a full replacement keyed by passenger ID.  If no passenger with
     * the given ID exists, a {@link NoSuchElementException} is
     * thrown.</p>
     *
     * @param passenger the replacement passenger
     * @return the saved passenger
     * @throws NoSuchElementException if the passenger ID is not found
     */
    public Passenger updatePassenger(Passenger passenger) {
        String id = passenger.getPassengerId();
        if (passengerRepository.findByPassengerId(id).isEmpty()) {
            throw new NoSuchElementException("Passenger not found: " + id);
        }
        log.info("Updating passenger [{}]", id);
        return passengerRepository.save(passenger);
    }

    /* ------------------------------------------------------------------ */
    /*  Delete                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Deletes the passenger identified by the given ID.
     *
     * @param passengerId the passenger ID to remove
     * @return {@code true} if the passenger existed and was removed
     * @throws NoSuchElementException if no passenger with that ID exists
     */
    public boolean deletePassenger(String passengerId) {
        if (passengerRepository.findByPassengerId(passengerId).isEmpty()) {
            throw new NoSuchElementException("Passenger not found: " + passengerId);
        }
        log.info("Deleting passenger [{}]", passengerId);
        return passengerRepository.deleteByPassengerId(passengerId);
    }
}
