# ApnaEvent — Session Memory

## Project identity

- Project: ApnaEvent / Smart Event & Venue Management Platform
- Main stack: Java 21, Spring Boot microservices, Maven, MySQL, Eureka, Spring Cloud Config, Spring Cloud Gateway, Kafka, static HTML/CSS/JavaScript UI.
- Services: Config-server, EurekaServer, ApiGateway, Auth-Service, Customer-Service, Venue-Service, Event-Service, Booking-Service, Paymnt-Service, Notification-Service, RA-AI-Service, AdminServer.
- This file records repository evidence, not assumptions about production health.

## Current status

- Source ZIP extracted and inspected.
- Service directories, POMs, properties, static resources, tests, scripts, CI workflow, README, implementation notes, and existing CSS conventions reviewed.
- All JavaScript files passed `node --check` in the inspection environment.
- Maven build verification is BLOCKED: Maven is not installed; the wrapper download of Maven 3.9.16 from Maven Central failed in this environment.
- Full application startup/integration tests have not been run. MySQL, Kafka, Eureka, and all services were not brought up together here.

## Changes made in this working copy

- Added `Architecture.md` documenting the observed microservice layout, service ports, dependencies, security boundaries, and verification limits.
- Added `rules.md` with no-guessing, security, coding, testing, Git, and documentation-maintenance rules.
- Added `design.md` based on the existing CSS font stack, colors, layouts, components, and accessibility expectations.
- Added `tasks.md` with verified checks, blocked build verification, and next integration tasks.
- Added this `memory.md`.
- Updated `Auth-Service/src/main/java/com/auth/config/AuthDataInitializer.java` to refuse blank initial passwords and rotate an existing configured account whose password hash matches an empty password when a strong `AUTH_PASSWORD` is supplied; an already-existing safe admin does not need to be reseeded.
- Added `Auth-Service/src/test/java/com/auth/AuthDataInitializerTest.java` for the blank-password and existing-admin paths. These tests have NOT been executed in this environment.
- Updated `.env.example` comments/placeholders to clarify that it is not auto-loaded and that the JWT secret must be unique and at least 32 characters.
- Updated `README.md` and `IMPLEMENTATION-STATUS.md` to remove the claim of a fixed `admin/admin123` development login and document initial administrator setup.
- Updated `scripts/start-all.ps1` to derive the project root from the script location and print the actual log directory.
- Removed the unused hard-coded root variable from `scripts/stop-all.ps1`.
- Updated `scripts/verify-apnaevent.ps1` to fail if a service wrapper is missing rather than silently skipping it.
- Updated `tests/smoke-test.ps1` to require configured `AUTH_USERNAME` and `AUTH_PASSWORD` and removed the hard-coded `admin123` fallback and unused role-selector field.
- Updated root `build-all.ps1` to delegate to `scripts/build-all.ps1`.

## Important cautions

- The current GitHub Actions workflow uses `-DskipTests`; it validates packaging/compilation only, not test behavior.
- `.env.example` is a reference and is not automatically imported by Spring Boot.
- Do not reintroduce an empty or well-known administrator password.
- Do not claim build/test success until the commands have actually run.
- The archive contained generated `target/` output and runtime `logs/`; these should not be included in a clean source ZIP.
- Existing `.git` metadata is preserved in the working copy; do not reset or rewrite Git history.

## Next actions

1. Run Maven compile/package and Auth-Service tests on a machine with Java 21 and working Maven distribution/dependency access.
2. Run the Windows PowerShell start/stop/verification scripts from the extracted project path.
3. Start MySQL and Kafka, then verify service discovery, gateway routing, registration/login, role checks, booking/payment/refund, notifications, and concurrent seat booking.
4. Review all service logs for errors and sensitive data.
5. Update `tasks.md` and this file with actual results after those checks.
