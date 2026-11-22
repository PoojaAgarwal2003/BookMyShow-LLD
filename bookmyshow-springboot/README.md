# BookMyShow – Spring Boot LLD

A modular-monolith reference implementation of a movie-ticket booking backend using Java 21, Spring Boot 3.5, PostgreSQL, Redis, JWT and Gradle.

## Features

- Movie, city, theatre, screen, seat and show catalog APIs
- JWT registration/login with BCrypt password hashing
- Redis seat locks with TTL and compare-and-delete release (prevents one user releasing another user's lock)
- PostgreSQL show-seat inventory with optimistic version field and pessimistic row locking during booking/payment
- Pending booking creation with idempotency key
- Mock payment Strategy + Factory
- First-start demo catalog seed (one movie, one theatre, one screen, 12 seats and one show)
- Booking ownership checks, request validation and common error responses
- Docker Compose for PostgreSQL and Redis
- Unit tests for payment strategy/factory

## Architecture

`Controller -> Service -> Repository -> PostgreSQL`

Redis is used only for short-lived seat locks. PostgreSQL is the source of truth for bookings and inventory. A successful payment transitions the booking to `CONFIRMED` and the show-seat rows to `BOOKED` in one database transaction.

## Prerequisites

- JDK 21
- Gradle 8.14+ (or generate/use a Gradle wrapper)
- Docker Desktop / Docker Engine

## Run locally

```bash
docker compose up -d
gradle clean test
gradle bootRun
```

Application: `http://localhost:8080`

Health: `GET http://localhost:8080/actuator/health`

Default local database:
- database: `bookmyshow`
- username: `bms`
- password: `bms`

Override using `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, and `JWT_SECRET`.

**Demo JWT secret:** The configured secret is for local development only. Set a fresh base64-encoded secret (32 bytes minimum) in any shared or production environment.

## API quick start

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json

{"name":"Pooja","email":"pooja@example.com","password":"Password123"}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{"email":"pooja@example.com","password":"Password123"}
```

Use the returned token as `Authorization: Bearer <token>`.

### Browse catalog

```http
GET /api/v1/movies
GET /api/v1/cities
GET /api/v1/theatres?cityId=1
GET /api/v1/shows?movieId=1
GET /api/v1/shows/1/seats
```

### Lock seats

```http
POST /api/v1/bookings/lock
Authorization: Bearer <token>
Content-Type: application/json

{"showId":1,"showSeatIds":[1,2]}
```

### Create pending booking

```http
POST /api/v1/bookings
Authorization: Bearer <token>
Content-Type: application/json

{"showId":1,"showSeatIds":[1,2],"idempotencyKey":"client-generated-unique-key"}
```

### Pay

```http
POST /api/v1/payments/booking/1
Authorization: Bearer <token>
Content-Type: application/json

{"method":"MOCK"}
```

The mock gateway approves most amounts; an amount whose fractional part is `.99` simulates a decline, useful for exercising failure handling.

## Important implementation notes / production hardening

This project is an educational, runnable LLD baseline—not a production ticketing platform. Before production:
1. Move schema management to Flyway migrations and set `ddl-auto: validate`.
2. Replace the demo startup seeder with controlled admin/catalog ingestion for movies, theatres, screens, seats, shows and show-seat inventory.
3. Add a durable payment provider integration with signed webhook verification, idempotent callback processing, refund workflow and reconciliation.
4. Add an outbox for notifications and booking/payment events.
5. Improve booking expiry to atomically release any still-held inventory and reconcile payment status.
6. Use a single atomic Redis Lua script for multi-seat acquisition if strict all-or-nothing locking is required across concurrent lock requests. Current implementation rolls back locks acquired by this request when a later seat cannot be locked.
7. Ensure lock TTL and booking expiry policy are aligned; extend/renew locks for long payment flows.
8. Add rate limiting, audit logging, observability, PII protection, secrets management and role-based admin APIs.
9. For real payment integrations, do not hold a DB transaction open while making network calls. Use a payment state machine and asynchronous callback/outbox workflow.
10. Add integration tests with Testcontainers for PostgreSQL/Redis and concurrency stress tests.

## Design patterns

- Strategy: `PaymentStrategy` and `MockPaymentStrategy`
- Factory: `PaymentFactory`
- Repository: Spring Data repositories
- Dependency Injection / Singleton scope: Spring-managed components
- State transitions: `BookingStatus`, `PaymentStatus`
- Observer-style extension point: production notification/outbox can subscribe to booking-confirmed events

## Tests

Tests are under `src/test/java`. Run with `gradle test`.
