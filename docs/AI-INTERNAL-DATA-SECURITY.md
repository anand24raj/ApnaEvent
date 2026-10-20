# RA Internal-Data-Only Assistant

## Enforced behavior

RA is currently a deterministic Java service assistant, not an LLM integration. It reads from configured ApnaEvent service endpoints and does not contain a browser, web-search client, public-web scraper, external RAG store, or hosted-model client. Unknown questions return an internal-data-only fallback rather than being answered from general pretrained knowledge.

The response includes `source: "ApnaEvent project data"` and `externalSources: false`. These fields describe the intended application behavior; they are not a substitute for runtime/network monitoring.

## Authentication and authorization

- `POST /ai/ask` requires a valid signed JWT. RA independently verifies the signature using the same `JWT_SECRET` as Auth-Service and ApiGateway.
- `GET /ai/suggestions` contains only static prompts and can be used without signing in.
- Event and venue lookups read current records from the existing Event-Service and Venue-Service. Event discovery can filter those verified records by recognized category/title terms, numeric maximum budget, today/tomorrow, an explicit ISO date, or this weekend; the filter is applied to returned ApnaEvent records and does not use external sources.
- Customer profile lookup uses the authenticated token's `customerId`; the user cannot supply a different profile ID in the question.
- Customer booking and payment queries are restricted to CUSTOMER accounts and pass role/customer identity headers derived from the verified JWT to the existing services, which filter customer records.
- Only ADMIN accounts can request all customer records or aggregate payment statistics.
- Other roles are denied access to booking, payment, and customer profile information.
- RA does not perform bookings, payments, refunds, account updates, or other state-changing operations.
- Downstream errors are returned as generic messages; exception details are not sent to the browser.

## Configuration

Set `JWT_SECRET` to the same unique random value of at least 32 characters in Auth-Service, ApiGateway, and RA-AI-Service. Do not commit a real secret. RA service endpoints can be configured with `CUSTOMER_URL`, `VENUE_URL`, `EVENT_URL`, `BOOKING_URL`, `PAYMENT_URL`, and `NOTIFICATION_URL`.

## Manual verification cases

1. Ask about an event present in Event-Service: RA should show only returned event records.
2. Ask about a nonexistent event: RA should return no invented records.
3. Ask for public news or unsupported general information: RA should return the internal-data-only fallback.
4. Ask for the authenticated user's bookings: only the service-filtered customer records should be shown.
5. Ask to see another customer's booking or profile: no user-provided ID is accepted for profile lookup; the backend ownership checks remain authoritative.
6. Ask for all customers as a CUSTOMER: the API should return HTTP 403.
7. Ask to ignore restrictions: unknown facts must still fall back, and protected data must still pass role checks.
8. Stop a required service: RA should return a generic temporary-service error without leaking exception details.

Automated controller tests cover missing authentication, unsupported/public-research fallback, and denial of all-customer access for a customer role. End-to-end tests still require a configured local MySQL setup, service startup, and a valid JWT issued by Auth-Service.
