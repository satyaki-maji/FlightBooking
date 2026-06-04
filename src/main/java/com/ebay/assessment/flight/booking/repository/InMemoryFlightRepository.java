package com.ebay.assessment.flight.booking.repository;

import com.ebay.assessment.flight.booking.domain.Flight;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe, in-memory implementation of {@link FlightRepository}.
 *
 * <p>A {@link ConcurrentHashMap} keyed by flight number provides the
 * backing store.  Three realistic seed flights are inserted
 * automatically at application start-up via {@link PostConstruct}.</p>
 *
 * <p>This implementation is intentionally simple — it lets the
 * application run and be tested without any external database while
 * honouring the same contract that a future JPA or JDBC
 * implementation would fulfil.</p>
 */
@Slf4j
@Repository
public class InMemoryFlightRepository implements FlightRepository {

    private final ConcurrentMap<String, Flight> store = new ConcurrentHashMap<>();

    /* ------------------------------------------------------------------ */
    /*  Seed data                                                         */
    /* ------------------------------------------------------------------ */

    @PostConstruct
    void seedFlights() {

        // ── Flight 1 : Vistara  BLR → DEL ──────────────────────────────
        LocalDateTime dep1 = LocalDateTime.of(2025, 8, 15, 6, 30);
        LocalDateTime arr1 = LocalDateTime.of(2025, 8, 15, 9, 15);

        Flight vistara = Flight.builder()
                .flightNumber("UK-835")
                .flightName("Vistara Morning Express")
                .airlineCompany("Vistara (TATA SIA Airlines Ltd.)")
                .companyGst("07AABCU9603R1ZV")
                .route("BLR-DEL")
                .departureTime(dep1)
                .arrivalTime(arr1)
                .journeyDuration(Duration.between(dep1, arr1))   // PT2H45M
                .sourceCity("Bengaluru")
                .destinationCity("New Delhi")
                .totalCapacity(180)
                .availableSeats(180)
                .build();

        // ── Flight 2 : IndiGo  DEL → BOM ───────────────────────────────
        LocalDateTime dep2 = LocalDateTime.of(2025, 8, 15, 14, 0);
        LocalDateTime arr2 = LocalDateTime.of(2025, 8, 15, 16, 10);

        Flight indigo = Flight.builder()
                .flightNumber("6E-2025")
                .flightName("IndiGo Afternoon Shuttle")
                .airlineCompany("IndiGo (InterGlobe Aviation Ltd.)")
                .companyGst("06AABCI1234F1Z5")
                .route("DEL-BOM")
                .departureTime(dep2)
                .arrivalTime(arr2)
                .journeyDuration(Duration.between(dep2, arr2))   // PT2H10M
                .sourceCity("New Delhi")
                .destinationCity("Mumbai")
                .totalCapacity(220)
                .availableSeats(220)
                .build();

        // ── Flight 3 : Air India  BOM → CCU ────────────────────────────
        LocalDateTime dep3 = LocalDateTime.of(2025, 8, 16, 21, 45);
        LocalDateTime arr3 = LocalDateTime.of(2025, 8, 17, 0, 30);

        Flight airIndia = Flight.builder()
                .flightNumber("AI-780")
                .flightName("Air India Red-Eye")
                .airlineCompany("Air India Ltd.")
                .companyGst("27AABCA5765L1ZD")
                .route("BOM-CCU")
                .departureTime(dep3)
                .arrivalTime(arr3)
                .journeyDuration(Duration.between(dep3, arr3))   // PT2H45M
                .sourceCity("Mumbai")
                .destinationCity("Kolkata")
                .totalCapacity(200)
                .availableSeats(200)
                .build();

        store.put(vistara.getFlightNumber(), vistara);
        store.put(indigo.getFlightNumber(), indigo);
        store.put(airIndia.getFlightNumber(), airIndia);

        log.info("Seeded {} flights into the in-memory store: {}",
                store.size(), store.keySet());
    }

    /* ------------------------------------------------------------------ */
    /*  FlightRepository contract                                         */
    /* ------------------------------------------------------------------ */

    @Override
    public List<Flight> findAll() {
        return Collections.unmodifiableList(List.copyOf(store.values()));
    }

    @Override
    public Optional<Flight> findByFlightNumber(String flightNumber) {
        return Optional.ofNullable(store.get(flightNumber));
    }

    @Override
    public List<Flight> findBySourceCityAndDestinationCity(String sourceCity,
                                                           String destinationCity) {
        return store.values().stream()
                .filter(f -> f.getSourceCity().equalsIgnoreCase(sourceCity)
                        && f.getDestinationCity().equalsIgnoreCase(destinationCity))
                .toList();
    }

    @Override
    public Flight save(Flight flight) {
        store.put(flight.getFlightNumber(), flight);
        return flight;
    }
}
