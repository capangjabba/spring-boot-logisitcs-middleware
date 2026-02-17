# Spring Boot API Middleware

A Spring Boot-based **courier rate aggregator** (middleware) for shipments — built as a learning project to practice real-world API integration patterns.

> Status: **Learning project / WIP**  
> Currently integrated: **CityLink** + **J&T** (more can be added)

---

## What this project does

Instead of your frontend integrating multiple courier platforms (each with different request/response formats), this service provides a **single, standardized API**:

- Client sends *one request* (origin/destination, weight, dimensions)
- Middleware transforms the request to each courier’s format
- Calls courier endpoints **in parallel**
- Returns a unified response sorted by **cheapest rate first**

---

## Features

- ✅ Single REST endpoint for rate lookup
- ✅ Parallel courier calls using **WebClient + Reactor**
- ✅ Unified response shape (`courier`, `rate`)
- ✅ Validation with **Jakarta Bean Validation**
- ✅ Extensible design: add new couriers by implementing `CourierClient`
- ✅ Dockerfile included

---

## Supported couriers (current)

### CityLink
- Uses a postcode → state mapping (required for CityLink request)
- Note: current implementation applies a special destination postcode fallback for cross-state shipments

### J&T
- Fetches CSRF token + cookies from J&T shipping rates page
- Posts request with required headers (XHR-style)
- If response comes back as HTML, it parses the “Parcel” shipping rate from the returned table (Jsoup)

> ⚠️ Because some integrations rely on public web endpoints / HTML structures, they can break if the courier website changes.

---

## API

### Endpoint
`GET /api/v1/logistic/rates`

### Query parameters (required)
| Name | Example | Notes |
|------|---------|------|
| `originCountryCode` | `MY` | ISO-3166 alpha-2 |
| `originPostcode` | `43000` | |
| `destinationCountryCode` | `MY` | ISO-3166 alpha-2 |
| `destinationPostcode` | `50000` | |
| `weightKg` | `1.2` | decimal allowed |
| `lengthCm` | `10` | decimal allowed |
| `widthCm` | `10` | decimal allowed |
| `heightCm` | `10` | decimal allowed |

### Example request
```bash
curl "http://localhost:8080/api/v1/logistic/rates?originCountryCode=MY&originPostcode=43000&destinationCountryCode=MY&destinationPostcode=50000&weightKg=1.2&lengthCm=10&widthCm=10&heightCm=10"
```

### Example response
```json
{
  "data": [
    { "courier": "CityLink", "rate": 24.00 },
    { "courier": "J&T", "rate": 26.50 }
  ]
}
```

## Running locally

### Prerequisites

- Java **21**
    
- Maven (or just use the Maven wrapper included)
    

### Run

`./mvnw spring-boot:run`

App runs on:

- `http://localhost:8080`
    

---

## Running with Docker

Build:

`docker build -t logistics-middleware .`

Run:

`docker run -p 8080:8080 logistics-middleware`

Optional Java opts:

`docker run -p 8080:8080 -e JAVA_OPTS="-Xms256m -Xmx512m" logistics-middleware`

---

## Project structure (high level)

- `controllers/RateController`  
    Exposes the REST endpoint and validates input
    
- `services/RateService`  
    Calls all couriers in parallel, filters failures, sorts by rate
    
- `couriers/*`  
    Each courier integration lives in its own package and implements `CourierClient`
    
- `utils/*`  
    Shared helpers (postcode → state mapping, CSRF session helper, HTML parser, etc.)
    

---

## Adding a new courier

1. Create a new client class under `couriers/<courier_name>/`
    
2. Implement `CourierClient`:
    
    - Accept `RateRequestDto`
        
    - Return `Mono<CourierRateResponse>`
        
3. Plug it into `RateService`:
    
    - Add to the `Flux.merge(...)` list
        
4. Keep the external integration resilient:
    
    - log failures clearly
        
    - return `Mono.empty()` on courier failure so other couriers can still succeed

