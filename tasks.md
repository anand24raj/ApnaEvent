# ApnaEvent — Task Board

## Status definitions

- `TODO` — not started
- `IN_PROGRESS` — currently being worked on
- `BLOCKED` — needs a dependency, environment, or decision
- `REVIEW` — implemented but awaiting verification
- `DONE` — acceptance criteria verified
- `DEFERRED` — intentionally postponed with reason recorded

A task is not `DONE` merely because code or documentation was generated.

## Phase 0 — Repository and baseline

### AE-001 — Inspect project structure
- Status: DONE
- Evidence: repository archive extracted; 12 Spring Boot service directories and static UI resources identified.
- Acceptance: service layout, technology evidence, and external dependencies recorded in `Architecture.md`.

### AE-002 — Check frontend JavaScript syntax
- Status: DONE
- Evidence: `node --check` was run for all `.js` files in the inspected source tree; no syntax errors were reported.
- Limitation: syntax validation does not prove browser behavior.

### AE-003 — Verify Maven build
- Status: BLOCKED
- Reason: system Maven is unavailable. Maven Wrapper attempted to download Maven 3.9.16 from Maven Central but the execution environment could not fetch the distribution.
- Next: run `scripts/verify-apnaevent.ps1` or the Maven Wrapper on a Windows development machine with network access and Java 21.
- Acceptance: record the actual compile/test results for all services; do not assume success.

### AE-004 — Establish integration baseline
- Status: TODO
- Work: run services with MySQL and Kafka, verify Eureka registration, gateway routes, authentication, booking/payment, notifications, and concurrency.
- Acceptance: record real endpoint/test evidence in the verification report.

## Phase 1 — Security and developer workflow

### AE-005 — Prevent blank initial administrator password
- Status: REVIEW
- Change: `AuthDataInitializer` refuses blank initial passwords and rotates an existing configured account whose hash matches an empty password, using `AUTH_PASSWORD`; it does not require the seed password when an existing account is already safe.
- Tests added: `AuthDataInitializerTest` covers blank-password rejection, empty-password rotation, and an already-existing safe administrator.
- Acceptance: run Auth-Service tests and verify startup with missing/valid `AUTH_PASSWORD` in a local environment.

### AE-006 — Remove machine-specific startup paths
- Status: REVIEW
- Change: `scripts/start-all.ps1` derives the project root from its own location; `scripts/stop-all.ps1` no longer declares the old absolute root; log output uses the calculated log directory.
- Acceptance: run start/stop scripts from a non-default project folder on Windows PowerShell.

### AE-007 — Make verification fail on missing wrapper
- Status: REVIEW
- Change: `scripts/verify-apnaevent.ps1` now fails instead of skipping a service whose wrapper is missing.
- Acceptance: verify normal run and missing-wrapper behavior in PowerShell.

### AE-008 — Align root build command
- Status: REVIEW
- Change: root `build-all.ps1` delegates to `scripts/build-all.ps1`, keeping documented build behavior consistent.
- Acceptance: run it on Windows with Maven available and check that failures propagate.

### AE-009 — Remove hard-coded smoke-test password fallback
- Status: REVIEW
- Change: `tests/smoke-test.ps1` now requires configured credentials and sends only the supported username/password fields.
- Acceptance: run the smoke test against a live local environment and verify both missing-credential and valid-credential paths.

### AE-010 — Add project guidance documents
- Status: REVIEW
- Files: `Architecture.md`, `rules.md`, `design.md`, `tasks.md`, `memory.md`.
- Acceptance: compare against source and update after runtime verification.

## Phase 2 — Functional/security verification

### AE-011 — Verify authentication and role enforcement
- Status: TODO
- Check: registration cannot assign privileged roles; JWT secret is consistent; customer/manager/admin authorization is enforced server-side.

### AE-012 — Verify booking/payment failure paths
- Status: TODO
- Check: seat reservation is atomic; duplicate payment is idempotent; payment failure releases seats; compensation/refund behavior is correct.

### AE-013 — Verify UI behavior and accessibility
- Status: TODO
- Check: key user flows in a browser at mobile/desktop sizes; keyboard focus; API errors; loading/empty states; no broken links/assets.

### AE-014 — Review logs and generated artifacts
- Status: TODO
- Check: generated `target/` directories and runtime logs are excluded from source delivery; logs do not expose credentials or personal data.

## Task rules

- Work in dependency order and one small task at a time.
- Record blockers rather than inventing workarounds.
- Keep unrelated changes out of a focused fix.
- Update this file and `memory.md` only to reflect real work and actual evidence.
