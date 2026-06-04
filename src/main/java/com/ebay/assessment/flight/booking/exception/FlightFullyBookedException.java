package com.ebay.assessment.flight.booking.exception;

/**
 * Thrown when a booking is attempted on a flight that has no
 * available seats remaining.
 */
public class FlightFullyBookedException extends RuntimeException {

    public FlightFullyBookedException(String message) {
        super(message);
    }

    public FlightFullyBookedException(String message, Throwable cause) {
        super(message, cause);
    }
}
