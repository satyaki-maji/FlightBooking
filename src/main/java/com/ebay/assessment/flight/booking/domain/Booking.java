package com.ebay.assessment.flight.booking.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Immutable domain model representing a flight booking.
 *
 * <p>Composes a {@link Flight} and a {@link Passenger} — the full
 * aggregate is itself immutable so it can be shared freely across
 * threads without synchronisation.</p>
 */
@Value
@Builder
public class Booking {

    String bookingId;
    Flight flight;
    Passenger passenger;
    String seatNumber;
    Double ticketPrice;
    String bookingStatus;
    LocalDateTime bookingTimestamp;
}
