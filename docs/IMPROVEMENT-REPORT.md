# ApnaEvent Improvement Report

## Scope of this delivery

This update preserves the existing microservices and repository structure. It focuses on the highest-risk assistant authorization and browser-safety gaps plus a contact form that uses the existing Notification-Service SMTP configuration.

## Implemented changes

- RA verifies JWT signatures itself using the shared `JWT_SECRET`.
- RA customer, booking, and payment queries now apply role checks. Customer-scoped booking/payment queries derive identity from the verified token; customer IDs are not accepted from the user's natural-language prompt.
- Only administrators can request all customer records and payment revenue/failed-payment aggregates.
- Unsupported questions, including public-web research requests, use an explicit internal-data-only fallback.
- RA service failures return generic messages instead of exception details.
- RA browser rendering uses DOM text nodes for service data rather than interpolating database fields into `innerHTML`, reducing stored/reflected XSS risk.
- RA controls now prevent duplicate submissions while a request is pending, announce status updates accessibly, support Escape to close, and respect reduced-motion preferences.
- Added a responsive Contact Us page and a Notification-Service endpoint with server-side validation. The endpoint reports success only after the configured mail sender accepts the message; it explicitly does not guarantee delivery.
- Made email configuration optional at service startup so the application can return a clear configuration error instead of failing solely because `MAIL_PASSWORD` is absent.
- Added documentation for AI authorization and verification scenarios.
- Improved natural-language event discovery using internal event records, with supported category/title terms, numeric maximum budget, and date filters for today, tomorrow, this weekend, or an explicit ISO date. No external source is called.
- Added consistent page descriptions and Open Graph title/description/type metadata to all 29 HTML pages, plus missing viewport metadata. No canonical URL was invented because the production domain is not configured.
- Added the existing configured email, GitHub, LinkedIn, and portfolio destinations to the Contact Us footer. The project does not contain a verified contract/contractor destination, so none was invented.
- Contact submissions now validate the enquiry category against the supported options and sanitize subject line breaks before creating an email.

## Files added

- `ApiGateway/src/main/resources/static/contact.html`
- Additional SEO metadata in existing HTML pages
- `Notification-Service/src/main/java/com/notification/controller/ContactController.java`
- `RA-AI-Service/src/test/java/com/raai/RaAiControllerTest.java`
- `docs/AI-INTERNAL-DATA-SECURITY.md`
- `docs/IMPROVEMENT-REPORT.md`

## Files modified

- `RA-AI-Service/src/main/java/com/raai/RaAiController.java` (internal natural-language event filters)
- `RA-AI-Service/src/main/java/com/raai/config/GlobalExceptionHandler.java`
- `RA-AI-Service/pom.xml`
- `RA-AI-Service/src/main/resources/application.properties`
- `Config-server/src/main/resources/config/RA-AI-Service.properties`
- `ApiGateway/src/main/resources/application.properties`
- `ApiGateway/src/main/java/com/gateway/security/JwtAuthenticationFilter.java`
- `ApiGateway/src/main/java/com/gateway/security/SecurityConfig.java`
- `ApiGateway/src/main/resources/static/index.html`
- `ApiGateway/src/main/resources/static/assets/dashboard.js`
- `Auth-Service/src/main/resources/static/profile.html`
- `scripts/verify-apnaevent.ps1`
- `.github/workflows/ci.yml`
- `README.md`
- `docs/REVIEW-REPORT.md`
- All existing `assets/ra-assistant.js` and `assets/ra-assistant.css` copies
- `Notification-Service/src/main/resources/application.properties`
- `Notification-Service/src/main/java/com/notification/controller/ContactController.java`
- `Notification-Service/src/test/java/com/notification/controller/ContactControllerTest.java`

## Configuration required

- `JWT_SECRET`: same random secret (minimum 32 characters) for Auth-Service, ApiGateway, and RA-AI-Service.
- `DB_USERNAME` and `DB_PASSWORD`: existing service-specific MySQL configuration.
- `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM`: required to submit contact enquiries through SMTP.
- Existing service dependencies (MySQL, Eureka, Config Server, Kafka for the existing event flows) must be available for the related features.

## Verification performed in this environment

- JavaScript syntax check: passed for all 37 JavaScript files found in the project.
- Maven test/build: not completed. The Maven wrapper could not download Maven from Maven Central in this environment, so Java compilation and automated JUnit execution remain unverified.
- Browser/device visual tests, full microservice startup, database integration, authenticated booking/payment integration, and live SMTP delivery were not available here and must be run in the configured local environment.
- No changes were pushed to a remote Git repository.


## Static checks completed for this delivery

- `node --check`: 37 JavaScript files checked; 0 syntax errors.
- Maven POM XML parsing: 12 POM files parsed; 0 XML errors.
- HTML scan: 29 HTML files scanned; no literal `href="#"` links remain.
- Maven test execution was attempted for `RA-AI-Service`, but the wrapper could not download Maven from Maven Central in this environment. Do not interpret this as a passing Java build or passing JUnit tests.
- No runtime logs are included in the delivered ZIP. `.git` and repository history/configuration are preserved.

## Follow-up cleanup pass (2026-10-12)

- Updated generated sign-out links to use the login route instead of a `#` placeholder; the existing click handler still performs sign-out.
- Changed the default sender address to empty so a fabricated `noreply@example.com` address is not presented as a configured contact destination. Set `MAIL_FROM` explicitly before enabling mail submission.
- Kept the existing `.git` directory and repository metadata in the package.
- Re-ran static checks: 37 JavaScript files passed `node --check`; 12 Maven POMs parsed as XML; 29 HTML files were scanned with no literal `href="#"` remaining. A source scan found no known public web-search client identifiers among the checked source/config files.
- Maven execution was retried offline and remained blocked because the Maven wrapper distribution is not available locally and network retrieval is unavailable. No Java build or JUnit pass is claimed.


## Follow-up implementation pass (2026-10-12)

- Added internal-data-only event filtering for category/title, numeric maximum ticket price, today/tomorrow, this weekend, and explicit ISO dates. The feature filters only Event-Service records and returns an honest no-results message when no records match.
- Added focused JUnit cases for event filter parsing and unsupported contact categories. These tests are added but remain unexecuted because Maven cannot be downloaded in this environment.
- Added missing description/viewport/Open Graph metadata across all 29 HTML pages and made the Contact Us footer consistent with existing project contact links.
- Tightened Contact Us server validation for allowed categories and sanitized subject line breaks.
- Static checks re-run after these changes are documented separately in the final delivery report.

## Final static verification pass (2026-10-12)

- JavaScript syntax: 37 files checked with `node --check`; 0 syntax failures.
- Maven project descriptors: all 12 `pom.xml` files parse successfully as XML.
- HTML: all 29 pages contain a title, viewport metadata, description, Open Graph title and description; no literal `href="#"` or JavaScript placeholder hrefs were found.
- Budget parsing smoke check: the Java regex pattern extracted 1500 from `under ₹1500`, 1200 from `below 1200 rupees`, 999 from `₹999`, and 1500 from `under 1500` in JShell.
- Added `RA-AI-Service/mvnw.cmd`, which was missing even though the Windows verification script expects it for every service.
- Maven/JUnit: attempted `RA-AI-Service/mvnw test`; the wrapper failed because Maven Central could not be reached. No Java compilation or JUnit pass is claimed.
- Runtime, browser/device, MySQL-backed integration, booking/payment, and SMTP delivery checks remain unverified and require the developer's local environment.
