# Flight Booking API

A production-style RESTful service for managing flights and passenger bookings, built with **Spring Boot 3.5**, **Java 21**, and a **thread-safe in-memory** persistence layer. The application ships with three pre-seeded flights and is ready to accept bookings the moment it starts -- no database installation required.

---

## Table of Contents

1. [Project Overview & Setup](#project-overview--setup)
2. [Business Use Cases](#business-use-cases)
3. [Swagger API Documentation](#swagger-api-documentation)
4. [API Reference](#api-reference)
5. [Technical Architecture](#technical-architecture)
6. [Future Improvements](#future-improvements)

---

## Project Overview & Setup

### Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 21+     |

> **Note:** You do **not** need Maven installed globally. The included Maven Wrapper (`mvnw`) handles everything.

### Build

```bash
./mvnw clean package
```

### Run the Tests

```bash
./mvnw test
```

### Start the Application

```bash
./mvnw spring-boot:run
```

The server starts on **`http://localhost:8080`** with three realistic seed flights (`UK-835`, `6E-2025`, `AI-780`) and 100 bookable seats per flight.

---

## Business Use Cases

### Flight Management

- **Add a new flight** -- register a flight with full metadata (airline, route, departure/arrival times, capacity, GST number).
- **View all flights** -- list every flight currently available in the system.
- **Remove a flight** -- delete a flight by its IATA-style flight number.
- **View available seats** -- query the remaining seat numbers on a specific flight in real time.

### Passenger Booking

- **Create a booking** -- book a passenger onto a flight; the next available seat is atomically assigned.
- **View a booking by ID** -- retrieve the full booking record (flight details, passenger info, seat number, price, status) using its UUID.
- **Search bookings by passenger name & mobile** -- find bookings using case-insensitive name substring matching and/or exact mobile number filtering.
- **Cancel a booking** -- cancel an existing booking and instantly return the seat to the flight's available pool for re-booking.

---

## Swagger API Documentation

The project integrates **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui 2.8.8`) to auto-generate an interactive OpenAPI 3.0 specification directly from the controller annotations.

Once the application is running, open the Swagger UI in your browser:

> **http://localhost:8080/api/v1/swagger-ui/index.html**

From the UI you can explore every endpoint, inspect request/response schemas, and execute live API calls without leaving the browser.

| Resource               | URL                                                  |
|------------------------|------------------------------------------------------|
| Swagger UI             | `http://localhost:8080/api/v1/swagger-ui/index.html` |
| OpenAPI JSON           | `http://localhost:8080/api/v1/api-docs`              |

---

## API Reference

> All examples use `localhost:8080`. The three seed flights are **`UK-835`** (BLR-DEL), **`6E-2025`** (DEL-BOM), and **`AI-780`** (BOM-CCU).

### 1. Add Flight

```bash
curl -X POST http://localhost:8080/api/v1/flights \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "SG-422",
    "flightName": "SpiceJet Evening Connect",
    "airlineCompany": "SpiceJet Ltd.",
    "companyGst": "07AABCS1234P1ZQ",
    "route": "BLR-GOI",
    "departureTime": "2025-09-01T18:00:00",
    "arrivalTime": "2025-09-01T19:15:00",
    "journeyDuration": "PT1H15M",
    "sourceCity": "Bengaluru",
    "destinationCity": "Goa",
    "totalCapacity": 180,
    "availableSeatNumbers": ["1A","1B","1C","1D","2A","2B","2C","2D"]
  }'
```

### 2. View All Flights

```bash
curl -X GET http://localhost:8080/api/v1/flights \
  -H "Accept: application/json"
```

### 3. Remove Flight

```bash
curl -X DELETE http://localhost:8080/api/v1/flights/UK-835
```

### 4. View Available Seats

```bash
curl -X GET http://localhost:8080/api/v1/flights/6E-2025/seats \
  -H "Accept: application/json"
```

### 5. Create Booking

```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "UK-835",
    "passengerName": "Ravi Kumar",
    "mobileNumber": "+91-9876543210",
    "address": "42 MG Road, Bengaluru"
  }'
```

**Response** (`201 Created`):

```json
{
  "bookingId": "b47ac10b-58cc-4372-a567-0e02b2c3d479",
  "flight": { "flightNumber": "UK-835", "..." : "..." },
  "passenger": {
    "passengerId": "a12dc3e4-...",
    "fullName": "Ravi Kumar",
    "mobileNumber": "+91-9876543210",
    "address": "42 MG Road, Bengaluru"
  },
  "seatNumber": "1A",
  "ticketPrice": 5500.0,
  "bookingStatus": "CONFIRMED",
  "bookingTimestamp": "2025-08-15T10:30:00"
}
```

### 6. View Booking by ID

```bash
curl -X GET http://localhost:8080/api/v1/bookings/b47ac10b-58cc-4372-a567-0e02b2c3d479 \
  -H "Accept: application/json"
```

### 7. Search Bookings by Passenger Name & Mobile

Both query parameters are optional. Omitting one skips that filter.

```bash
# Search by name only (case-insensitive, substring match)
curl -X GET "http://localhost:8080/api/v1/bookings/search?name=Ravi" \
  -H "Accept: application/json"

# Search by mobile only (exact match)
curl -X GET "http://localhost:8080/api/v1/bookings/search?mobile=%2B91-9876543210" \
  -H "Accept: application/json"

# Search by both name and mobile
curl -X GET "http://localhost:8080/api/v1/bookings/search?name=Ravi&mobile=%2B91-9876543210" \
  -H "Accept: application/json"
```

### 8. Cancel Booking

```bash
curl -X DELETE http://localhost:8080/api/v1/bookings/b47ac10b-58cc-4372-a567-0e02b2c3d479
```

---

## Technical Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│                         REST Controllers                         │
│             FlightController  ·  BookingController               │
├───────────────────────────────────────────────────────────────────┤
│                        Service Layer                             │
│      FlightService  ·  BookingService  ·  PassengerService       │
├───────────────────────────────────────────────────────────────────┤
│                   Repository Interfaces (Ports)                  │
│  FlightRepository  ·  BookingRepository  ·  PassengerRepository  │
├───────────────────────────────────────────────────────────────────┤
│             In-Memory Implementations (Adapters)                 │
│         ConcurrentHashMap-backed  ·  Zero external deps          │
└───────────────────────────────────────────────────────────────────┘
```

### Thread-Safe In-Memory Design

The entire persistence layer is built for safe concurrent access without external infrastructure:

- **`ConcurrentHashMap`** backs every repository (flights, bookings, passengers), providing atomic reads and writes across threads.
- **`ConcurrentLinkedQueue`** powers the per-flight seat pool. Booking a passenger calls `poll()` to atomically claim the next seat; cancellation calls `offer()` to return it. Both operations are **lock-free** and **O(1)**, meaning two threads racing for the last seat will never be assigned the same seat number -- exactly one succeeds and the other receives a `400 Flight Fully Booked` response.
- **Immutable domain objects** (`@Value` via Lombok) ensure that `Flight`, `Booking`, and `Passenger` instances can be shared across threads without defensive copying or synchronization.
- **Repository interfaces** follow the Dependency Inversion Principle: the service layer programs against abstract contracts, making it trivial to swap in a JPA/JDBC implementation later without touching business logic.

### Cross-Cutting Concerns

- **Global Exception Handler** (`@RestControllerAdvice`) translates all domain and framework exceptions into uniform `ErrorResponse` JSON payloads with proper HTTP status codes (400, 404, 500).
- **Jakarta Bean Validation** (`@Valid` + `@NotBlank`) rejects malformed booking requests at the controller boundary before they reach the service layer.

---

## Future Improvements

| Area | Current State | Proposed Improvement |
|------|---------------|----------------------|
| **Persistence** | `ConcurrentHashMap`-based in-memory store; data is lost on restart. | Migrate to a **relational database** (PostgreSQL / MySQL) with Spring Data JPA. The existing `Repository` interfaces already define the contract -- only new `@Repository` adapter classes are needed; zero service-layer changes required. |
| **Seat Allocation Concurrency** | `ConcurrentLinkedQueue.poll()` provides lock-free, single-JVM thread safety. | In a horizontally scaled (multi-instance) deployment, replace the in-process queue with **distributed locking via Redis** (e.g., Redisson `RLock` or Spring Integration `RedisLockRegistry`) to guarantee globally exclusive seat assignment across nodes. |
| **Pricing Model** | Ticket price is stored as `Double` (`5500.0`), which is susceptible to floating-point rounding errors. | Transition the `ticketPrice` field to **`BigDecimal`** with explicit `RoundingMode` and scale to ensure cent-accurate arithmetic -- critical for invoicing, tax calculation, and financial reporting. |
| **Authentication & Authorization** | Open API; no security layer. | Introduce Spring Security with JWT-based authentication and role-based access control (e.g., `ADMIN` for flight management, `USER` for booking). |
| **Observability** | SLF4J logging only. | Add Spring Boot Actuator endpoints, Micrometer metrics, and distributed tracing (OpenTelemetry) for production monitoring. |

---
