# Smart Event & Venue Management Platform

Production-style Spring Boot microservices platform for event discovery, venue management, seat booking, payment, cancellation/refund, notifications, administration, monitoring, and a project-aware RA assistant.

## Services

| Service | Port | Responsibility |
|---|---:|---|
| Config-server | 1245 | Centralized configuration; native config by default, Git-backed profile supported |
| EurekaServer | 8761 | Service discovery |
| ApiGateway | 8080 | Routing, JWT validation, correlation ID, UI entry point |
| Auth-Service | 1251 | Login, registration, JWT issuance |
| Customer-Service | 1241 | Customer profiles and registration |
| Venue-Service | 1236 | Venue CRUD and capacity |
| Event-Service | 1235 | Events, search, seat inventory, lifecycle |
| Booking-Service | 1237 | Booking workflow, seat reservation, payment orchestration |
| Paymnt-Service | 1239 | Payments and refunds |
| Notification-Service | 1240 | Kafka notifications and email delivery |
| AdminServer | 1111 | Spring Boot Admin + ApnaEvent dashboard aggregation |
| RA-AI-Service | 1260 | Project-data-only assistant used by the UIs |

## Architecture

```text
Client / ApnaEvent UI
        |
        v
   API Gateway
        |
        +--> Auth Service
        +--> Customer Service -----> MySQL customer_db
        +--> Venue Service --------> MySQL venue_db
        +--> Event Service --------> MySQL event_db
        +--> Booking Service ------> MySQL booking_db
        |        |
        |        +--> Customer / Event / Payment via OpenFeign
        |        +--> booking-events --> Notification Service
        +--> Payment Service ------> MySQL payment_db
        |        +--> notification-events --> Notification Service
        +--> Notification Service -> MySQL notification_db
        +--> RA-AI-Service --------> reads service APIs for project-grounded answers

Config Server + Eureka provide platform infrastructure.
Spring Boot Admin + Actuator + Micrometer/Prometheus provide observability.
```

## Key engineering decisions

- Each business service owns its own database/schema.
- OpenFeign is used for synchronous service-to-service calls.
- Kafka is used for asynchronous notification events.
- Seat reservation is an atomic database update so concurrent requests cannot reduce seats below zero.
- Booking -> Payment uses Resilience4j Circuit Breaker, Retry and TimeLimiter with a controlled fallback.
- Cancellation releases seats and can trigger a payment refund.
- Correlation IDs are generated/propagated through HTTP and Kafka and included in logs.
- REST errors follow the common fields `timestamp`, `status`, `error`, `message`, `path`, and `correlationId` where the service has a REST exception handler.
- RA does not call an external LLM or internet source; it answers from ApnaEvent service data and fixed project workflow knowledge.

## Local prerequisites

- Java 21
- Maven 3.9+
- MySQL 8+
- Apache Kafka
- Git
- Optional: Prometheus and a browser

Create the databases with `infra/mysql/init.sql`, or allow each service to create its configured schema with `createDatabaseIfNotExist=true`.

Set the required environment variables before starting the services. Use `.env.example` as the variable reference. Never commit real credentials.

## Required local environment configuration

The `.env.example` file is a reference; Spring Boot does not load it automatically.
Set the variables in your PowerShell session, IDE run configuration, or approved local environment loader before starting services.

At minimum, configure `JWT_SECRET` (a unique random value of at least 32 characters), `AUTH_PASSWORD`, and `DB_PASSWORD`. Use the same `JWT_SECRET` value for Auth-Service, ApiGateway, and RA-AI-Service. Configure `KAFKA_BOOTSTRAP_SERVERS` and mail settings when those integrations are used. Never use sample placeholders as real credentials or commit secrets.

For PowerShell, set values in the current session before running the startup script, for example:

```powershell
$env:AUTH_USERNAME = "admin"
$env:AUTH_PASSWORD = "<your-strong-unique-password>"
$env:JWT_SECRET = "<your-random-secret-of-at-least-32-characters>"
$env:DB_PASSWORD = "<your-local-mysql-password>"
```

Replace each placeholder with a real local value. The initial administrator is seeded only when it does not already exist. The service refuses to create it if `AUTH_PASSWORD` is blank and can rotate an existing configured account whose stored password hash matches an empty password when a replacement `AUTH_PASSWORD` is supplied.

## Start order

1. MySQL
2. Kafka
3. EurekaServer
4. Config-server
5. Auth-Service
6. Customer-Service
7. Venue-Service
8. Event-Service
9. Paymnt-Service
10. Booking-Service
11. Notification-Service
12. RA-AI-Service
13. AdminServer
14. ApiGateway

The services can also start in a different order because Eureka and the Config Server are used for discovery/configuration, but the order above makes troubleshooting easier.

## Build every service

From the repository root on Windows PowerShell:

```powershell
.\scripts\build-all.ps1
```

Or build an individual service:

```powershell
cd Booking-Service
mvn clean package
```

## Main URLs

- ApnaEvent: `http://localhost:8080/`
- Eureka: `http://localhost:8761/`
- Config Server: `http://localhost:1245/`
- Spring Boot Admin: `http://localhost:1111/`
- Prometheus: `http://localhost:9090/`
- Gateway OpenAPI UI: `http://localhost:8080/swagger-ui/index.html`
- Contact form endpoint: `POST http://localhost:8080/contact/submit` (JSON fields: `fullName`, `email`, optional `phone`, `category`, `subject`, `message`)

## Git-backed Config Server

Native classpath configuration is the default for an easy local setup. To use a separate Git configuration repository:

```powershell
$env:CONFIG_PROFILE="git"
$env:CONFIG_GIT_URI="https://github.com/<your-account>/<your-config-repo>.git"
$env:CONFIG_GIT_LABEL="main"
```

Start Config-server after setting those variables. Keep secrets out of the config repository; use environment variables or a secret manager for credentials.

## GitHub workflow

The repository contains `.github/workflows/ci.yml`. Every push/PR runs `mvn -B clean package` for all 12 Maven services, including their configured automated tests. Full end-to-end flows still require MySQL, Kafka, Eureka, Config Server, and any configured SMTP credentials.

For a source change:

```powershell
git status
git add .
git commit -m "Describe the change"
git push origin main
```

A source push updates GitHub immediately. A hosted deployment only changes after its configured deployment pipeline detects the new commit and builds/deploys the affected service(s).

## Deployment note

GitHub is the source-control repository; it is not the runtime host for Spring Boot microservices. For live deployment, use a service platform such as Railway, Render, or another provider and connect it to this GitHub repository. Each microservice needs its own deployable service plus managed MySQL/Kafka or equivalent infrastructure. The deployment environment must provide the same environment variables used by the applications.

See `docs/GITHUB-DEPLOYMENT.md` for the update/push/deploy workflow.




## Project Author

**Anand Raj**

- Email: anandraj24092002@gmail.com
- GitHub: https://github.com/anand24raj
- LinkedIn: https://linkedin.com/in/anandraj24/
- Portfolio: https://anandraj-porfolio.netlify.app/
