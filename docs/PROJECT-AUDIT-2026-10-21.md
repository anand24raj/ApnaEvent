# ApnaEvent source audit — 2026-10-21

## What was inspected

- Inventory of the uploaded archive and all 12 Maven service directories.
- Root and per-service configuration, Maven POMs, test source inventory, CI workflow, static HTML/CSS/JavaScript assets, helper scripts, README and existing audit notes.
- Existing test classes, environment-variable references, and obvious placeholder/secret patterns in source/configuration.
- Eventbrite's public product material as a reference for event discovery, filtering, event detail, registration/checkout, organizer tools and responsive navigation. ApnaEvent should use its own branding and implementation, not copy Eventbrite's proprietary assets or source.

## Changes in this delivery

1. Updated `.github/workflows/ci.yml` to provision a disposable MySQL 8.4 service, wait for its health check, set the `DB_HOST`, `DB_PORT`, `DB_USERNAME`, and `DB_PASSWORD` variables used by the applications, and provide CI-only JWT/admin credentials required by application startup.
2. CI runs `mvn clean verify`, so Maven compiles the service, executes tests, and verifies the package rather than only compiling or skipping tests.
3. Added least-privilege `contents: read` workflow permissions, a timeout, and cancellation of superseded runs.
4. Preserved the existing 12-service matrix and the default Spring configuration; no test profile or Hibernate dialect override was added.

## Why the Hibernate error occurs

The application JDBC URL is built from `DB_HOST` and `DB_PORT`, but the previous workflow did not start MySQL or provide the database environment. Hibernate could not connect to JDBC to read metadata. Setting a dialect alone would hide the missing database and would not make JPA tests work. The updated workflow provides MySQL and the expected variables.

## Static review findings / follow-up

- The project contains 12 independent Spring Boot Maven projects rather than a single root Maven reactor.
- Each JPA service uses MySQL and a service-specific default database name. The CI root account can create these databases through the existing `createDatabaseIfNotExist=true` JDBC option.
- There are automated tests in all services, but this environment does not have Maven installed and network access to download Maven dependencies is unavailable. Java/JUnit tests therefore could not be executed here; CI must be run after committing this change.
- Full end-to-end verification still requires a coordinated runtime test of Eureka, Config Server, MySQL, Kafka, Gateway, authorization/ownership, booking seat concurrency, payment/refund behavior, and notification/email delivery.
- The archive included `.git` repository metadata and runtime log files. This delivery excludes `.git`, logs, IDE workspace metadata, and generated build output from the portable source ZIP. Keep your actual local repository folder intact and copy the changed files into it; do not replace your working repository with this portable ZIP if you need its Git history.
- This is a source/configuration audit, not a guarantee that every application flow is bug-free or production-ready. Test results and browser-based functional checks remain required.

## Suggested Eventbrite-inspired UX backlog

- Clear event search with city/location, date, category, online/in-person and price filters.
- Event cards with consistent image ratio, date, venue/location, price and a clear details action.
- Dedicated event details and a short, validated registration/booking flow with visible booking state.
- Organizer dashboard for event lifecycle, attendee list/check-in, refund status and basic analytics.
- Saved/favorite events, empty/loading/error states, responsive mobile navigation and accessible focus/labels.

Build these on the existing API contracts and authorization rules; do not expose admin or organizer actions to customers and do not claim payment/refund success unless the backend confirms it.
