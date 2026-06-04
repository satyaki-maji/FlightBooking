package com.ebay.assessment.flight.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Inbound request DTO for creating a new flight booking.
 *
 * <p>Jakarta Bean Validation annotations are applied so that Spring's
 * {@code @Valid} integration rejects malformed payloads before they
 * reach the service layer.</p>
 */
@Schema(description = "Request payload for creating a new flight booking")
@Value
@Builder
@Jacksonized
public class BookingRequest {

    /**
     * IATA-style flight number to book (e.g. {@code "UK-835"}).
     */
    @Schema(description = "IATA-style flight number", example = "UK-835")
    @NotBlank(message = "flightNumber is required")
    String flightNumber;

    /**
     * Full name of the passenger.
     */
    @Schema(description = "Full name of the passenger", example = "Ravi Kumar")
    @NotBlank(message = "passengerName is required")
    String passengerName;

    /**
     * Contact mobile number for the passenger.
     */
    @Schema(description = "Contact mobile number", example = "+91-9876543210")
    @NotBlank(message = "mobileNumber is required")
    String mobileNumber;

    /**
     * Residential or postal address of the passenger.
     */
    @Schema(description = "Residential or postal address", example = "42 MG Road, Bengaluru")
    String address;
}
