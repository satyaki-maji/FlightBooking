package com.ebay.assessment.flight.booking.controller;

import com.ebay.assessment.flight.booking.domain.Booking;
import com.ebay.assessment.flight.booking.domain.Passenger;
import com.ebay.assessment.flight.booking.dto.BookingRequest;
import com.ebay.assessment.flight.booking.dto.ErrorResponse;
import com.ebay.assessment.flight.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * REST controller exposing booking-management endpoints under
 * {@code /api/v1/bookings}.
 *
 * <p>All business logic is delegated entirely to {@link BookingService};
 * this controller contains nothing but API definitions, DTO-to-domain
 * mapping, and response mapping.</p>
 */
@Tag(name = "Bookings", description = "Booking management endpoints")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /* ------------------------------------------------------------------ */
    /*  POST /bookings                                                    */
    /* ------------------------------------------------------------------ */

    /**
     * Creates a new booking for the specified flight and passenger.
     *
     * <p>The inbound {@link BookingRequest} is validated via Jakarta
     * Bean Validation ({@code @Valid}) before reaching this method;
     * constraint violations are intercepted by the
     * {@link com.ebay.assessment.flight.booking.exception.GlobalExceptionHandler
     * GlobalExceptionHandler} and returned as structured HTTP 400
     * responses.</p>
     *
     * @param request validated booking request DTO
     * @return the confirmed booking with HTTP 201
     */
    @Operation(
            summary = "Create a new booking",
            description = "Books a passenger onto the specified flight, atomically claiming the next available seat",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Booking confirmed"),
                    @ApiResponse(responseCode = "400", description = "Validation error or flight fully booked",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Flight not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        Passenger passengerDetails = Passenger.builder()
                .fullName(request.getPassengerName())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .build();

        Booking booking = bookingService.bookFlight(request.getFlightNumber(), passengerDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /* ------------------------------------------------------------------ */
    /*  GET /bookings/{id}                                                */
    /* ------------------------------------------------------------------ */

    /**
     * Retrieves a single booking by its unique identifier.
     *
     * @param id the booking UUID
     * @return the booking if found
     * @throws NoSuchElementException propagated to the global handler
     *                                when the booking does not exist
     */
    @Operation(
            summary = "Get a booking by ID",
            description = "Retrieves a single booking by its unique identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Booking found"),
                    @ApiResponse(responseCode = "404", description = "Booking not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @Parameter(description = "Booking UUID", example = "b47ac10b-58cc-4372-a567-0e02b2c3d479")
            @PathVariable String id) {
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Booking not found: " + id));
        return ResponseEntity.ok(booking);
    }

    /* ------------------------------------------------------------------ */
    /*  GET /bookings/search?name={name}&mobile={mobile}                  */
    /* ------------------------------------------------------------------ */

    /**
     * Searches bookings by passenger name and/or mobile number.
     *
     * <p>Both query parameters are optional.  When omitted the
     * corresponding filter is simply skipped, so calling
     * {@code GET /bookings/search} with no parameters returns every
     * booking.</p>
     *
     * @param name   full or partial passenger name (case-insensitive)
     * @param mobile exact mobile number
     * @return matching bookings; may be empty
     */
    @Operation(
            summary = "Search bookings",
            description = "Searches bookings by passenger name and/or mobile number. "
                        + "Both parameters are optional; omitting one skips that filter",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Search results returned")
            })
    @GetMapping("/search")
    public ResponseEntity<List<Booking>> searchBookings(
            @Parameter(description = "Full or partial passenger name (case-insensitive)",
                       example = "Ravi")
            @RequestParam(required = false) String name,
            @Parameter(description = "Exact mobile number",
                       example = "+91-9876543210")
            @RequestParam(required = false) String mobile) {

        List<Booking> results = bookingService.searchBookings(name, mobile);
        return ResponseEntity.ok(results);
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE /bookings/{id}                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Cancels the booking identified by the given UUID, returning the
     * assigned seat back to the flight's available-seat pool.
     *
     * @param id the booking UUID
     * @return HTTP 204 on successful cancellation
     * @throws NoSuchElementException propagated to the global handler
     *                                when the booking does not exist
     */
    @Operation(
            summary = "Cancel a booking",
            description = "Cancels the booking and atomically returns the assigned seat back to the flight's pool",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid UUID format",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Booking or flight not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "Booking UUID", example = "b47ac10b-58cc-4372-a567-0e02b2c3d479")
            @PathVariable UUID id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
