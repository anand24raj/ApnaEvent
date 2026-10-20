# ApnaEvent – Full Architecture Update

## Scope
Updated the existing 12-service ApnaEvent source without replacing the architecture or frontend stack.

## Key fixes
- Login no longer accepts or trusts a role selector.
- Public registration creates CUSTOMER accounts only. ADMIN and VENUE_MANAGER are not self-service roles.
- JWT now carries the authenticated customer ID when an account is linked to a customer profile.
- API Gateway forwards trusted authenticated identity/role/customer ID to downstream services for ownership checks.
- Customer profile, booking and notification access is constrained by the authenticated customer ID where applicable.
- Event and venue records now support manager ownership through `managerUsername`; venue-manager update/delete operations are checked against ownership.
- Payment endpoint contract is consistently `/paymnt/pay` and the Booking Feign client matches the Payment Controller.
- Payment records carry customer ID and are filtered for customers.
- Payment creation is idempotent per booking using a unique booking ID and an existing-payment check.
- Payment refund is restricted to ADMIN through the gateway.
- Payment statistics are restricted to ADMIN.
- Booking status mutation is restricted to ADMIN through the gateway; Payment-Service's internal Feign call remains service-to-service.
- Booking customer ownership checks were added for create/view/update/delete/cancel operations.
- Seat reservation remains an atomic database update in Event-Service, preventing two successful reservations of the same seats.
- Payment failure handling releases seats and persists `PAYMENT_FAILED` booking state using a transaction rule that prevents rollback of the failure record.
- If payment succeeds but later booking persistence fails, Booking-Service attempts a compensating payment refund and releases seats.
- Friendly API errors replace raw technical exception messages for unexpected booking/payment failures while technical details are logged server-side.
- Booking detail endpoints now return HTTP 200 instead of HTTP 201.
- Role-based navigation was consolidated into the external `app-shell.js` used by the service UIs.
- Login/register pages no longer expose role selection.
- Added authenticated profile page at `/auth-ui/profile.html`.
- Customer booking UI uses the authenticated customer account instead of exposing a global customer selector.
- Removed unnecessary role-sensitive navigation such as customer access to customer management and payment administration.
- Updated booking UI terminology to product-oriented wording.

## Important endpoint contract
Payment Controller:
`POST /paymnt/pay`

Booking Feign:
`POST /paymnt/pay`

Gateway route:
`Path=/paymnt/** -> lb://PAYMNT-SERVICE`

There is no intentional `/pay/pay` endpoint in the updated source.

## Verification performed in this environment
- Archive extracted successfully.
- Java source brace-balance check passed.
- All frontend JavaScript files passed `node --check` syntax validation.
- Source-only fresh archive was created after the updates.

## Build limitation
The environment does not have Maven installed locally. The project Maven Wrapper exists, but downloading Maven 3.9.16 from Maven Central was blocked by the execution environment's network policy. Therefore a full 12-service Maven compile/startup test could not honestly be claimed here.

Run the supplied `scripts/verify-apnaevent.ps1` on the Windows development machine after extracting the ZIP to the existing `C:\Users\anand\ApnaEvent` project location.

## Remaining validation required on the developer machine
1. Start Eureka and Config Server.
2. Start all services.
3. Confirm all services register in Eureka.
4. Confirm Gateway routes resolve.
5. Create a new CUSTOMER account and sign in.
6. Verify customer JWT contains a customer ID.
7. Browse public events/venues without login.
8. Create a booking as the customer.
9. Confirm payment and booking status.
10. Confirm payment record and notification.
11. Repeat payment request for the same booking and verify no second payment is created.
12. Run two simultaneous requests for the same final seat and verify only one succeeds.
13. Verify customer cannot access event/venue/customer administration endpoints.
14. Verify manager ownership restrictions using two different manager accounts.
15. Verify ADMIN dashboard and administrative records.
