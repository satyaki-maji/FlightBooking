package com.ebay.assessment.flight.booking.exception;

import com.ebay.assessment.flight.booking.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Centralised exception handler for all {@code @RestController} classes.
 *
 * <p>Translates domain and framework exceptions into cleanly formatted
 * JSON error responses, keeping controllers completely free of
 * try/catch blocks and HTTP-status logic.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ------------------------------------------------------------------ */
    /*  Jakarta Bean Validation failures (HTTP 400)                       */
    /* ------------------------------------------------------------------ */

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when
     * {@code @Valid} rejects the request body.
     *
     * <p>Each field error is mapped to an
     * {@link ErrorResponse.FieldError} with the field name, the
     * rejected value, and the validation message.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields have invalid values")
                .fieldErrors(fieldErrors)
                .build();

        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /* ------------------------------------------------------------------ */
    /*  Domain: FlightFullyBookedException (HTTP 400)                     */
    /* ------------------------------------------------------------------ */

    /**
     * Handles {@link FlightFullyBookedException} — the flight exists
     * but has no remaining seats.
     */
    @ExceptionHandler(FlightFullyBookedException.class)
    public ResponseEntity<ErrorResponse> handleFlightFullyBooked(
            FlightFullyBookedException ex) {

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Flight Fully Booked")
                .message(ex.getMessage())
                .build();

        log.warn("Flight fully booked: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /* ------------------------------------------------------------------ */
    /*  Domain: NoSuchElementException (HTTP 404)                         */
    /* ------------------------------------------------------------------ */

    /**
     * Handles {@link NoSuchElementException} — the requested resource
     * (flight, booking, passenger) does not exist.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoSuchElementException ex) {

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /* ------------------------------------------------------------------ */
    /*  Type mismatch — e.g. invalid UUID in path (HTTP 400)              */
    /* ------------------------------------------------------------------ */

    /**
     * Handles {@link MethodArgumentTypeMismatchException} — typically
     * a malformed UUID in a path variable.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message("Parameter '" + ex.getName()
                        + "' must be of type " + expectedType)
                .build();

        log.warn("Type mismatch on parameter [{}]: {}", ex.getName(), ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /* ------------------------------------------------------------------ */
    /*  Static resource / route not found (HTTP 404)                      */
    /* ------------------------------------------------------------------ */

    /**
     * Handles {@link NoResourceFoundException} — raised by Spring MVC when
     * a request targets a static resource path (e.g. favicon.ico) that does
     * not exist. Returning 404 quietly avoids polluting logs with ERROR
     * entries for harmless browser-generated requests.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex) {

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        log.debug("Static resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /* ------------------------------------------------------------------ */
    /*  Catch-all (HTTP 500)                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Catch-all for any unhandled exception — prevents raw stack traces
     * from leaking to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .build();

        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
