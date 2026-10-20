# GitHub Push and Live Deployment Workflow

## 1. First push

Create an empty GitHub repository and keep this project as the source repository.

```powershell
cd C:\Users\anand\eclipse-workspace-Project

git init
git branch -M main
git add .
git commit -m "Initial ApnaEvent microservices platform"
git remote add origin https://github.com/<your-account>/<your-repository>.git
git push -u origin main
```

Do not commit `.env`, database passwords, JWT secrets, SMTP passwords, or private configuration repositories.

## 2. What happens when code changes

Use this workflow for every change:

```powershell
git status
git add .
git commit -m "Explain the change"
git push origin main
```

GitHub receives the new commit and the CI workflow builds the affected repository state.

## 3. Deployment

GitHub itself stores the source. A live Spring Boot deployment requires a runtime provider. Connect the repository to Railway, Render, or another Java-capable platform.

For a real microservices deployment, provision:

- MySQL databases/schemas for the business services
- Kafka
- Eureka Server
- Config Server
- Auth Service
- Customer Service
- Venue Service
- Event Service
- Booking Service
- Payment Service
- Notification Service
- RA-AI-Service
- AdminServer
- API Gateway

Set provider environment variables for database credentials, Eureka URL, Config Server URL, JWT secret, Kafka bootstrap servers, Admin Server URL, service URLs used by RA/Admin, and SMTP credentials if email is enabled.

## 4. If only one service changes

Rebuild/redeploy that service and any infrastructure/configuration that changed. If a shared API contract changes, redeploy the caller and callee together and run the smoke tests.

Examples:

- UI JavaScript change -> redeploy the service hosting that UI.
- Event API change -> redeploy Event Service and verify Booking/RA callers.
- Booking DTO/Feign contract change -> redeploy Booking and the changed target service.
- Kafka event schema change -> redeploy all producers/consumers that use that event.
- Gateway route/JWT change -> redeploy API Gateway.
- Config change -> update the configuration source and restart/redeploy affected services.
- Database/entity change -> deploy the migration/schema change before relying on the new application code.

## 5. Verification after deployment

At minimum verify:

1. Gateway health.
2. Eureka registrations.
3. Config Server health.
4. Customer registration/login.
5. Event creation and venue-capacity validation.
6. Last-seat concurrent booking behavior.
7. Payment success/failure and Payment Service outage fallback.
8. Cancellation and refund.
9. Kafka notification delivery.
10. RA assistant through the Gateway.
11. Admin dashboard.
12. Actuator/Prometheus metrics.

## 6. Rollback

If a deployment breaks production, roll back to the previous known-good Git commit/version. Do not manually edit production source files; fix the repository, commit the fix, and redeploy through the normal pipeline.

