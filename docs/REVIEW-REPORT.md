> **Update (2026-10-12):** The CI workflow and project build scripts have been updated to run Maven tests rather than skip them. Actual CI results must be checked after the changes are pushed; this environment could not download Maven to execute the tests.

# ApnaEvent Source Review Report

## Scope

Reviewed the uploaded source archive, including the 12 service directories, Maven POMs/wrapper configuration, application properties, static HTML/CSS/JavaScript, PowerShell helper scripts, existing tests, CI workflow, README, and implementation notes.

## Changes included

- Added root-level `Architecture.md`, `rules.md`, `design.md`, `tasks.md`, and `memory.md` based on repository evidence.
- Prevented initial administrator seeding with a blank password; added safe rotation for an existing configured account whose stored hash matches an empty password; added focused tests for these cases.
- Removed the hard-coded administrator password fallback from `tests/smoke-test.ps1`; the smoke test now requires configured credentials and verifies they are an ADMIN account before calling administrative endpoints.
- Updated `.env.example`, `README.md`, and `IMPLEMENTATION-STATUS.md` to clarify environment configuration and remove the fixed development-password claim.
- Made the startup script derive its root from its own location, removed an unused hard-coded root from the stop script, made verification fail if a service wrapper is missing, and aligned the root build script with the documented build script.
- Excluded generated Maven `target/` directories and runtime logs from this source delivery. The original uploaded ZIP remains unchanged.

## Checks performed

| Check | Result | Notes |
|---|---|---|
| ZIP extraction and source inventory | PASS | 12 service directories found |
| JavaScript syntax (`node --check`) | PASS | All source `.js` files checked |
| Shell syntax (`bash -n tests/smoke-test.sh`) | PASS | Shell smoke-test script parsed |
| Maven compile/package | BLOCKED | Maven is not installed; Maven Wrapper distribution download failed in this environment |
| Java/JUnit tests | NOT RUN | Maven/dependencies could not be made available |
| PowerShell script execution | NOT RUN | PowerShell is not installed in this environment |
| Full application/integration tests | NOT RUN | MySQL, Kafka, Eureka, and all services were not started together |

## Important limitations

This is a source review and a limited set of focused fixes, not a claim that the application is production-ready. The new Java tests and PowerShell changes still need to be executed on the developer's Windows environment with Java 21, Maven distribution access, and the required services.

The GitHub Actions workflow currently runs Maven packaging with `-DskipTests`, so a green CI run would not establish that unit or integration tests pass.

## Recommended next verification

1. Configure a unique `JWT_SECRET` of at least 32 characters and set `AUTH_PASSWORD` and database credentials in the local environment.
2. Run `scripts/verify-apnaevent.ps1` on Windows PowerShell.
3. Run Auth-Service tests and the relevant service test suites.
4. Start MySQL and Kafka, then start the services and verify Eureka registration and Gateway routes.
5. Test registration/login, role and ownership restrictions, event/venue management, concurrent seat booking, payment idempotency, payment failure/compensation, and notification delivery.
6. Review logs for unexpected errors and sensitive information.
