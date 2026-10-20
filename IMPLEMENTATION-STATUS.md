﻿# ApnaEvent Platform — Implementation Status

## Runtime platform

- Eureka Server: enabled and used for service discovery.
- Config Server: native configuration service with a friendly status page.
- API Gateway: single browser/API entry point on port 8080 with JWT and role checks.
- Spring Boot Admin: client registration enabled for the application services.
- MySQL: one database/schema per business service using persistent local MySQL storage.
- Kafka: booking/registration notification events.
- Prometheus: metrics endpoint.

## Business services

- Customer Service: create, list, view, update and delete customer records.
- Venue Service: create, list, view, update and deactivate venue records.
- Event Service: create, list, view, update/cancel events and manage seats.
- Booking Service: create/list/view/update/delete/cancel bookings and coordinate payment/seat reservation.
- Payment Service: process/list payments, status, revenue and refunds.
- Notification Service: create, query and mark notifications as read.

## Browser UI

- Gateway dashboard and service pages use external JS and CSS files.
- Forms submit to real Gateway endpoints and show server-side errors.
- Lists load live database records rather than demo records.
- RA is a small assistant popup, not a full-screen application.

## RA-AI-Service

RA-AI-Service is a separate Spring Boot project. It reads only the local ApnaEvent services listed in its configuration and provides project-data answers and suggestions. It does not call an external website or an external LLM.

## Initial administrator account

- Username defaults to `admin` unless `AUTH_USERNAME` is set.
- Set `AUTH_PASSWORD` to a strong, unique value before the first Auth-Service startup.
- The service refuses to create the initial administrator when the password is blank and can rotate an existing account whose stored hash matches an empty password when `AUTH_PASSWORD` is supplied.
- There is no supported hard-coded default administrator password.


