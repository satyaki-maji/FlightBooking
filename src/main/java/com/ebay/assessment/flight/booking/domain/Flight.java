package com.ebay.assessment.flight.booking.domain;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Immutable domain model representing a scheduled flight.
 *
 * <p>All fields are effectively {@code final} — instances are created
 * exclusively through the generated builder and can never be mutated
 * after construction.</p>
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
    int availableSeats;
}
