package com.ebay.assessment.flight.booking.repository;

import com.ebay.assessment.flight.booking.domain.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe, in-memory implementation of {@link BookingRepository}.
 *
 * <p>A {@link ConcurrentHashMap} keyed by booking ID provides the
 * backing store — the same pattern used by
 * {@link InMemoryFlightRepository} and
 * {@link InMemoryPassengerRepository}.</p>
 *
 * <p>This implementation is intentionally simple — it lets the
 * application run and be tested without any external database while
 * honouring the same contract that a future JPA or JDBC
 * implementation would fulfil.</p>
 */
@Slf4j
@Repository
public class InMemoryBookingRepository implements BookingRepository {

    private final ConcurrentMap<String, Booking> store = new ConcurrentHashMap<>();

    /* ------------------------------------------------------------------ */
    /*  BookingRepository contract                                        */
    /* ------------------------------------------------------------------ */

    @Override
    public Booking save(Booking booking) {
        store.put(booking.getBookingId(), booking);
        log.debug("Saved booking [{}] – store size: {}", booking.getBookingId(), store.size());
        return booking;
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(store.get(bookingId));
    }

    @Override
    public List<Booking> findAll() {
        return Collections.unmodifiableList(List.copyOf(store.values()));
    }

    @Override
    public boolean deleteById(String bookingId) {
        boolean removed = store.remove(bookingId) != null;
        if (removed) {
            log.debug("Deleted booking [{}] – store size: {}", bookingId, store.size());
        }
        return removed;
    }
}
