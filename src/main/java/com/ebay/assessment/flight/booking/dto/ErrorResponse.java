package com.ebay.assessment.flight.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uniform error payload returned by the
 * {@link com.ebay.assessment.flight.booking.exception.GlobalExceptionHandler GlobalExceptionHandler}.
 *
 * <p>Fields that are {@code null} (e.g. {@code fieldErrors} for
 * non-validation errors) are omitted from the JSON output.</p>
 */
@Schema(description = "Uniform error payload returned for all error responses")
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    int status;

    @Schema(description = "Short error category", example = "Validation Failed")
    String error;

    @Schema(description = "Human-readable description of the problem",
            example = "One or more fields have invalid values")
    String message;

    @Schema(description = "ISO-8601 timestamp of when the error occurred")
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Per-field validation errors — only present when the request body
     * fails Jakarta Bean Validation.
     */
    List<FieldError> fieldErrors;

    /**
     * A single field-level validation error.
     */
    @Value
    @Builder
    public static class FieldError {

        @Schema(description = "JSON field name that failed validation", example = "flightNumber")
        String field;

        @Schema(description = "The rejected value", example = "null")
        Object rejectedValue;

        @Schema(description = "Explanation of why the value was rejected",
                example = "flightNumber is required")
        String message;
    }
}
