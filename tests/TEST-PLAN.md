# QA Test Plan

## Smoke
1. Gateway `/actuator/health` returns UP.
2. Customer list endpoint responds.
3. Venue list endpoint responds.
4. Event list endpoint responds.
5. Eureka shows registered services.

## Functional
- Register a customer.
- Reject duplicate customer email/mobile.
- Create venue.
- Reject an event whose seats exceed venue capacity.
- Search/list events.
- Book available seats.
- Verify available seats decrease.
- Attempt concurrent booking for the last seat; only one booking may reserve it.
- Pay using supported payment mode.
- Simulate payment failure and verify booking cancellation/seat release.
- Cancel before event and verify refund flow.
- Reject cancellation after event.
- Verify notification generation.

## API/negative
- Missing required fields.
- Invalid email/mobile.
- Unknown customer/event/venue IDs.
- Zero/negative seat count.
- Booking a completed/cancelled event.
- Payment service unavailable.
- Notification service unavailable.

## Deployment
- Public gateway URL works over HTTPS.
- No API exposes localhost URLs.
- Secrets are stored in deployment environment variables.
- Restarting a service does not expose credentials or break discovery.
- Health endpoints are available to the platform health checker.

## Performance/concurrency
Use JMeter/Postman/Newman for:
- 50+ concurrent booking requests against one remaining seat.
- Verify no negative available-seat count.
- Verify no duplicate successful reservation for the same seat capacity.
