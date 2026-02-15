# Logistics Middleware

A simple Spring Boot-based courier rates aggregator middleware for Malaysian domestic shipments.

## What It Is For

This middleware acts as a unified API gateway for fetching shipping rates from multiple courier providers (e.g., CityLink, J&T, PosLaju, etc.). 

The frontend (or any client) sends a single standardized request with basic shipment details (postcodes, country codes, weight, optional dimensions). The middleware:
- Validates the input
- Maps/transforms the data for each courier's specific API requirements
- Calls the courier APIs **in parallel**
- Collects successful responses
- Returns a clean, sorted list of available rates

This decouples the frontend from individual courier quirks and provides a single source of truth for rate comparisons.

Example response:
```json
{
  "data": [
    {
      "courier": "Citylink",
      "rate": 24
    }
  ]
}
```

Current endpoint: `GET /api/v1/logistic/rates` with query params (e.g., `originPostcode`, `originCountryCode`, `weightKg`, etc.).

## For Learning Purposes

This project is built primarily as a **learning exercise** in modern Spring Boot architecture. It demonstrates real-world patterns such as:
- Clean separation of concerns (controllers, services, clients, utils)
- Reactive programming with WebClient and Project Reactor (parallel external calls)
- Input validation with Jakarta Bean Validation
- Resilient external API integration (timeouts, graceful degradation, detailed logging)
- Scalable design for adding more couriers
- Best practices (Lombok, logging, configuration, DTOs)

## Design Overview

### Core Principles
- **Standardized public API**: One simple `RateRequestDto` (validated) → no courier-specific fields leak to the client.
- **Per-courier clients**: Each courier has its own class implementing `CourierClient` (returns `Mono<CourierRateResponse>`).
- **Parallel aggregation**: `RateService` uses `Flux.merge` to call all clients concurrently → low latency even with many couriers.
- **Reactive but pragmatic**: WebClient for non-blocking I/O, but block only once in the synchronous MVC controller.
- **Resilience**: Individual courier failures are logged and ignored → always return partial results if possible.
- **Extensibility**: Adding a new courier = new client class + add to merge in service.

### Key Components
- **`RateRequestDto`** (dto/request): Public input — minimal fields (postcodes, weight, optional dims).
- **`CourierRateResponse` / `RatesApiResponse`** (dto/response): Simple, unified output.
- **`RateController`**: Entry point — binds query params with `@ModelAttribute` + `@Valid`.
- **`RateService`**: Orchestrates parallel calls, sorts by rate.
- **`CourierClient` interface**: All courier clients implement this (reactive `Mono` return).
- **Per-courier package** (e.g., `couriers/citylink`):
  - Courier-specific request POJO (e.g., `CityLinkRequest`)
  - Response POJO matching actual API JSON (nested for CityLink)
  - Mapping logic + detailed logging
- **`PostcodeStateMapper`**: Utility to resolve Malaysian state from postcode (required by some couriers like CityLink).
- **`WebClientConfig`**: Centralized timeouts and connector setup.

### Tech Stack
- Spring Boot 3.x (MVC + WebFlux for WebClient)
- Lombok
- Jakarta Validation
- SLF4J logging
- Reactor for reactive streams

Run with `./mvnw spring-boot:run` and test with curl or Postman.

