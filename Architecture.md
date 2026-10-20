# ApnaEvent — System Architecture

## 1. Purpose and truth policy

ApnaEvent is a Java 21 / Spring Boot microservices application for event and venue discovery, customer accounts, bookings, payments, notifications, administration, and a project-aware assistant.

This document describes the architecture visible in this repository. A service being present in source code does not by itself prove that it starts successfully or is production-ready. Runtime behavior must be verified with builds and integration tests.

## 2. Technology baseline

| Area | Repository evidence | Notes |
|---|---|---|
| Language | Java | `java.version` is set to 21 in the service POMs |
| Framework | Spring Boot | Service POMs currently declare Spring Boot `4.1.1` |
| Build | Maven | Each service has a Maven POM and wrapper scripts |
| Service discovery | Netflix Eureka | `EurekaServer` plus Eureka client configuration |
| Central configuration | Spring Cloud Config | `Config-server`, native profile by default; Git profile also present |
| API entry point | Spring Cloud Gateway Server WebMVC | `ApiGateway` on port 8080 by default |
| Persistence | MySQL + Spring Data JPA | Separate database names are configured per business service |
| Synchronous service calls | OpenFeign and REST clients | Inspect the client contract before changing any endpoint |
| Async events | Apache Kafka | Booking/payment notifications use event publishers/listeners |
| Resilience | Resilience4j | Booking/payment orchestration configuration exists |
| Monitoring | Actuator, Micrometer/Prometheus, Spring Boot Admin | Endpoints and registration are configured per service |
| Browser UI | HTML, CSS, JavaScript | Static resources live under each service's `src/main/resources/static` |
| CI | GitHub Actions | Current workflow packages each service with tests skipped |

## 3. Services and default ports

Ports are defaults from repository documentation/configuration and can be overridden by environment variables where configured.

| Service directory | Default port | Responsibility |
|---|---:|---|
| `Config-server` | 1245 | Centralized configuration |
| `EurekaServer` | 8761 | Service registry and discovery |
| `ApiGateway` | 8080 | Browser/API entry point, routing, JWT validation |
| `Auth-Service` | 1251 | Registration, login, JWT issuance, initial administrator seeding |
| `Customer-Service` | 1241 | Customer profiles |
| `Venue-Service` | 1236 | Venue records and capacity |
| `Event-Service` | 1235 | Event records, search, event lifecycle and seat inventory |
| `Booking-Service` | 1237 | Booking lifecycle and payment/seat orchestration |
| `Paymnt-Service` | 1239 | Payment records and refund workflow |
| `Notification-Service` | 1240 | Notifications, Kafka consumption, email integration |
| `RA-AI-Service` | 1260 | Project-aware assistant based on local ApnaEvent data/configuration |
| `AdminServer` | 1111 | Spring Boot Admin and dashboard aggregation |

The existing directory spelling `Paymnt-Service` and application/service identifier `PAYMNT-SERVICE` are established contracts. Do not rename them without an approved migration plan.

## 4. High-level request flow

```text
Browser / API client
        |
        v
   ApiGateway :8080
        |
        +--> Auth-Service
        +--> Customer-Service --> MySQL customer database
        +--> Venue-Service ----> MySQL venue database
        +--> Event-Service ----> MySQL event database
        +--> Booking-Service --> MySQL booking database
        |         +--> Event / Customer / Payment clients
        |         +--> Kafka booking events
        +--> Paymnt-Service ---> MySQL payment database
        |         +--> Kafka payment events
        +--> Notification-Service --> MySQL notification database / mail
        +--> RA-AI-Service

Config-server and Eureka support configuration and discovery.
AdminServer, Actuator and Prometheus provide operational visibility.
```

## 5. Architectural boundaries

- Keep business logic in the owning service; do not access another service's database directly.
- Each business service owns its data/schema. Cross-service reads/writes use the existing HTTP/Feign contracts or asynchronous events.
- Keep API Gateway as the browser/API entry point. Do not bypass it for public UI flows unless an existing documented requirement calls for it.
- Treat downstream identity headers as trusted only when they are set by the gateway and direct service access is appropriately restricted in the deployment environment.
- Use the existing Kafka event contracts for asynchronous notification flows. Do not change event names or payload fields without reviewing both producers and consumers.
- Seat reservation is intended to use an atomic database update. Preserve this concurrency guarantee when changing event/booking code.
- Booking/payment compensation and idempotency are business-critical. Changes must include failure-path tests and a documented rollback/compensation strategy.
- RA-AI-Service is described by the repository as a project-data assistant; do not silently add external LLM or internet calls.

## 6. Authentication and authorization

- Auth-Service issues JWTs; ApiGateway validates them.
- Public registration creates CUSTOMER accounts only; privileged roles must not be self-assigned through public registration.
- Role checks and ownership checks must be enforced server-side, not only by hiding UI navigation.
- Customer-scoped booking, profile, payment, and notification operations must use the authenticated identity rather than trusting a user-supplied customer ID.
- Venue/event manager ownership must be checked on every protected mutation.
- `JWT_SECRET` must be configured with a unique random value of at least 32 characters and the same value must be used by Auth-Service and ApiGateway.
- `AUTH_PASSWORD` must be configured before the initial administrator account is created. Never reintroduce a blank or hard-coded default administrator password.

## 7. Configuration and secrets

- `.env.example` is a reference only; Spring Boot does not automatically load it.
- Supply environment variables through a local shell/IDE run configuration or a trusted secret manager/loader.
- Never commit `.env`, production credentials, real JWT secrets, mail passwords, database passwords, or private keys.
- Keep configuration changes compatible with both native and Git-backed Config Server profiles.
- Avoid printing tokens, passwords, or sensitive personal data in logs.

## 8. UI architecture

- Static HTML, CSS, and JavaScript are stored in each service's `src/main/resources/static` directory.
- Gateway routes expose service APIs and UI prefixes such as `/event-ui/`, `/venue-ui/`, `/booking-ui/`, and `/auth-ui/`.
- Shared UI behavior is implemented in each service's `assets/app-shell.js` where present; `app.js` and page-specific scripts implement data loading and page interactions.
- Reuse existing assets and patterns. Do not introduce a new frontend framework without approval.
- `design.md` documents the current visual baseline and UI constraints.

## 9. Local dependencies and startup

Expected local dependencies include Java 21, Maven (or a functioning Maven Wrapper with download access), MySQL 8+, and Kafka. Eureka and Config Server should be started before dependent services; Gateway should be started after the service registry and core services.

See `README.md`, `scripts/start-all.ps1`, and `scripts/health-check.ps1` for the current startup workflow. A successful port check is not proof that an application is healthy or that its business flows work.

## 10. Testing and verification

- Unit tests should cover business rules and edge cases.
- Integration tests may require MySQL, Kafka, Eureka, Config Server, and other services.
- `tests/TEST-PLAN.md` lists functional, API-negative, deployment, and concurrency checks.
- The current GitHub Actions workflow runs package/compile validation with tests skipped. Do not describe it as full functional test coverage.
- Record exact commands and results. If the environment cannot download Maven or dependencies, state that the build was not verified.

## 11. Change procedure

Before changing a route, DTO, database field, event payload, role policy, port, or service identifier:

1. Find every producer, consumer, client, UI caller, and test that depends on it.
2. Describe compatibility and data-migration risks.
3. Keep the change small and update relevant tests.
4. Obtain approval for breaking or destructive changes.
5. Update this document only after the change is approved and verified.

## 12. Known verification boundary

Repository inspection confirms the source layout and configuration described above. It does not prove that all services compile, start, connect to external dependencies, or pass end-to-end tests. See `memory.md` and `tasks.md` for the current verification status.
