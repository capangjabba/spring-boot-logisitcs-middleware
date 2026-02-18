# Logistics Middleware – Courier Rate Aggregator

[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)

A **Spring Boot-based middleware** that aggregates shipping rates from multiple couriers in Malaysia (and potentially beyond).  
Built as a learning project to practice real-world API integrations, parallel reactive calls, unified responses, and extensible design.

> **Status:** WIP  
> Currently integrated: **CityLink** + **J&T**  
> More couriers can (and should!) be added easily.

## Why this project exists

Instead of your frontend or app calling 3–5 different courier APIs (each with unique formats, auth, quirks), this service gives you:

- **One standardized REST endpoint**
- Parallel calls to all couriers
- Unified, sorted response (cheapest first)
- Graceful failure handling (one courier down → others still work)

Perfect for e-commerce checkouts, logistics dashboards, or personal shipping comparison tools.

## Supported Couriers (current)

### CityLink
- Requires postcode → state mapping (included in utils)
- Special fallback logic for cross-state shipments

### J&T
- Scrapes CSRF token + cookies from public rates page
- Submits XHR-style POST
- Parses HTML table with **Jsoup** if JSON not available
- **Warning:** HTML-based → fragile if site layout changes

> ⚠️ **Scraping-based integrations can break** when courier websites update their HTML/CSS/JS.  
> Monitor logs and consider official APIs when available.

## API

### Endpoint
GET /api/v1/logistic/rates
Swagger UI: `http://localhost:8080/swagger-ui/index.html` (or `/swagger-ui.html`)

### Required Query Parameters

| Parameter                | Example    | Description                          | Notes                     |
|--------------------------|------------|--------------------------------------|---------------------------|
| originCountryCode        | MY         | ISO 3166-1 alpha-2                   | Currently only MY tested  |
| originPostcode           | 50000      | Origin postcode                      | 5 digits (MY)             |
| destinationCountryCode   | MY         | ISO 3166-1 alpha-2                   |                           |
| destinationPostcode      | 43000      | Destination postcode                 | Format varies by country  |
| weightKg                 | 1.2        | Weight in kilograms (decimal OK)     |                           |
| lengthCm                 | 10         | Length in cm (decimal OK)            |                           |
| widthCm                  | 10         | Width in cm                          |                           |
| heightCm                 | 10         | Height in cm                         |                           |

### Example Request

```bash
curl "http://localhost:8080/api/v1/logistic/rates?originCountryCode=MY&originPostcode=50000&destinationCountryCode=MY&destinationPostcode=43000&weightKg=1.2&lengthCm=10&widthCm=10&heightCm=10"
```

## Quick Start
### Running Locally (without Docker)
```bash
git clone https://github.com/yourusername/logistics-middleware.git
cd logistics-middleware
./mvnw spring-boot:run
```
Open http://localhost:8080/swagger-ui/index.html

### Running with Docker
```bash
# Build image
docker build -t logistics-middleware .

# Run
docker run -p 8080:8080 logistics-middleware

# With custom JVM options
docker run -p 8080:8080 -e JAVA_OPTS="-Xms256m -Xmx512m" logistics-middleware
```

# Adding a New Courier

1. Create package couriers/<couriername-lowercase>
2. Implement CourierClient interface:
```java
JavaMono<CourierRateResponse> getRate(RateRequestDto request);
```
3. Add your client to `RateServiceFlux.merge(...)` list
4. Handle errors gracefully → return Mono.empty() on failure


