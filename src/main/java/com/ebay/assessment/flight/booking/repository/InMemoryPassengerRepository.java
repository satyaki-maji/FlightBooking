package com.ebay.assessment.flight.booking.repository;

import com.ebay.assessment.flight.booking.domain.Passenger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe, in-memory implementation of {@link PassengerRepository}.
 *
 * <p>A {@link ConcurrentHashMap} keyed by passenger ID provides the
 * backing store — the same pattern used by
 * {@link InMemoryFlightRepository}.</p>
 */
@Slf4j
@Repository
public class InMemoryPassengerRepository implements PassengerRepository {

    private final ConcurrentMap<String, Passenger> store = new ConcurrentHashMap<>();

    /* ------------------------------------------------------------------ */
    /*  PassengerRepository contract                                      */
    /* ------------------------------------------------------------------ */

    @Override
    public List<Passenger> findAll() {
        return Collections.unmodifiableList(List.copyOf(store.values()));
    }

    @Override
    public Optional<Passenger> findByPassengerId(String passengerId) {
        return Optional.ofNullable(store.get(passengerId));
    }

    @Override
    public Passenger save(Passenger passenger) {
        store.put(passenger.getPassengerId(), passenger);
        log.debug("Saved passenger [{}] – store size: {}", passenger.getPassengerId(), store.size());
        return passenger;
    }

    @Override
    public boolean deleteByPassengerId(String passengerId) {
        boolean removed = store.remove(passengerId) != null;
        if (removed) {
            log.debug("Deleted passenger [{}] – store size: {}", passengerId, store.size());
        }
        return removed;
    }
}
