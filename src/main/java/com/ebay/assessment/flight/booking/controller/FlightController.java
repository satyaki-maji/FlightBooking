package com.ebay.assessment.flight.booking.controller;

import com.ebay.assessment.flight.booking.domain.Flight;
import com.ebay.assessment.flight.booking.dto.ErrorResponse;
import com.ebay.assessment.flight.booking.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST controller exposing flight-management endpoints under
 * {@code /api/v1/flights}.
 *
 * <p>All business logic is delegated entirely to {@link FlightService};
 * this controller contains nothing but API definitions and response
 * mapping.</p>
 */
@Tag(name = "Flights", description = "Flight management endpoints")
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    /* ------------------------------------------------------------------ */
    /*  POST /flights                                                     */
    /* ------------------------------------------------------------------ */

    /**
     * Creates a new flight.
     *
     * @param flight the flight payload
     * @return the persisted flight with HTTP 201
     */
    @Operation(
            summary = "Create a new flight",
            description = "Persists a new flight into the system",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Flight created successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping
    public ResponseEntity<Flight> createFlight(@RequestBody Flight flight) {
        Flight created = flightService.createFlight(flight);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ------------------------------------------------------------------ */
    /*  GET /flights                                                      */
    /* ------------------------------------------------------------------ */

    /**
     * Returns every flight currently in the system.
     *
     * @return list of all flights
     */
    @Operation(
            summary = "List all flights",
            description = "Returns every flight currently in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
            })
    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE /flights/{flightNumber}                                     */
    /* ------------------------------------------------------------------ */

    /**
     * Deletes the flight identified by the given flight number.
     *
     * @param flightNumber the IATA-style flight number
     * @return HTTP 204 on success
     * @throws NoSuchElementException propagated to the global handler
     *                                when the flight does not exist
     */
    @Operation(
            summary = "Delete a flight",
            description = "Removes the flight identified by the given flight number",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Flight deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Flight not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @DeleteMapping("/{flightNumber}")
    public ResponseEntity<Void> deleteFlight(
            @Parameter(description = "IATA-style flight number", example = "UK-835")
            @PathVariable String flightNumber) {
        flightService.deleteFlight(flightNumber);
        return ResponseEntity.noContent().build();
    }

    /* ------------------------------------------------------------------ */
    /*  GET /flights/{flightNumber}/seats                                  */
    /* ------------------------------------------------------------------ */

    /**
     * Returns the list of seat numbers still available on the specified
     * flight.
     *
     * @param flightNumber the IATA-style flight number
     * @return a list of available seat labels (e.g. {@code ["1A","1B",…]})
     * @throws NoSuchElementException propagated to the global handler
     *                                when the flight does not exist
     */
    @Operation(
            summary = "Get available seats",
            description = "Returns the seat numbers still available on the specified flight",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seat list retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Flight not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/{flightNumber}/seats")
    public ResponseEntity<List<String>> getAvailableSeats(
            @Parameter(description = "IATA-style flight number", example = "UK-835")
            @PathVariable String flightNumber) {
        Flight flight = flightService.getFlightByNumber(flightNumber)
                .orElseThrow(() -> new NoSuchElementException(
                        "Flight not found: " + flightNumber));

        List<String> seats = List.copyOf(flight.getAvailableSeatNumbers());
        return ResponseEntity.ok(seats);
    }
}
