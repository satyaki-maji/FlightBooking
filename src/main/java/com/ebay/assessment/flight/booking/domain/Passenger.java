package com.ebay.assessment.flight.booking.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Immutable domain model representing a passenger.
 *
 * <p>Created via the generated builder; every field is {@code final}
 * and no setter is ever exposed.</p>
 */
@Value
@Builder
public class Passenger {

    String passengerId;
    String fullName;
    String mobileNumber;
    String address;
    LocalDateTime checkInTime;
}
