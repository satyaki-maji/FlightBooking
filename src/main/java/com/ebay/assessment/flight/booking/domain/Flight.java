package com.ebay.assessment.flight.booking.domain;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Domain model representing a scheduled flight.
 *
 * <p>Most fields are effectively {@code final} — instances are created
 * exclusively through the generated builder.  The one intentional
 * exception is {@link #availableSeatNumbers}: its <em>reference</em> is
 * final (the field is still declared {@code final} by Lombok's
 * {@code @Value}), but the {@link ConcurrentLinkedQueue} it points to
 * is designed to be mutated in-place via {@code poll()} during booking,
 * giving lock-free, thread-safe seat dispensing without rebuilding the
 * whole object.</p>
 */
@Value
@Builder
public class Flight {

    String flightNumber;
    String flightName;
    String airlineCompany;
    String companyGst;
    String route;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    Duration journeyDuration;
    String sourceCity;
    String destinationCity;
    int totalCapacity;

    /**
     * Thread-safe queue of seat numbers still available for booking on
     * this flight (e.g. {@code "1A"}, {@code "14C"}).
     *
     * <p>The queue is pre-filled at seed time with exactly 100 unique,
     * sequential seat numbers spanning rows 1–25 and columns A–D.
     * Booking a passenger simply calls {@link ConcurrentLinkedQueue#poll()}
     * to atomically claim the next available seat; when the queue is
     * empty the flight is fully booked.</p>
     */
    ConcurrentLinkedQueue<String> availableSeatNumbers;
}
