# ApnaEvent — AI Coding Rules and Quality Standards

## 1. Mandatory reading order

At the start of every task, read these project-root files before editing code:

1. `rules.md`
2. `Architecture.md`
3. `design.md` when UI is affected
4. `tasks.md`
5. `memory.md`

Then inspect the current repository state and the relevant source/configuration/tests. If a required file is missing, report it rather than claiming to have read it.

## 2. No-guessing policy

Never invent or assume business requirements, pages, user roles, API endpoints, request/response fields, database columns, environment variables, integrations, test results, or completed work. Use evidence from source code and approved requirements. Mark unresolved details `UNKNOWN` and ask a focused question when the answer affects correctness or security.

Do not turn a suggestion into an approved requirement. Do not claim an existing feature works simply because its files exist.

## 3. Source-of-truth order

1. Latest explicit user-approved requirement
2. Verified current implementation and configuration
3. Approved decisions in `Architecture.md` and `design.md`
4. Active task and progress notes in `tasks.md` and `memory.md`
5. Older notes or assumptions

If authoritative sources conflict, explain the conflict and ask before a change that alters behavior, architecture, security, data, or compatibility. Verify memory against the actual checkout because it can become stale.

## 4. Before coding

- Check `git status` and preserve all existing user changes.
- Inspect relevant code, callers, configuration, tests, and git diff.
- Identify the smallest affected file set and define acceptance criteria.
- Check for dependencies on endpoints, DTOs, database fields, Kafka events, roles, and service names.
- For risky or cross-service changes, present a short plan before editing.
- Never replace the whole application for a focused fix.

## 5. Java and Spring standards

- Follow the repository's Java 21 and existing Spring Boot conventions.
- Keep controllers focused on HTTP input/output; use existing service/repository boundaries.
- Use constructor injection and meaningful names.
- Validate request data at the server boundary.
- Handle expected exceptions consistently and do not expose stack traces to clients.
- Use transactions where multi-step persistence requires atomicity.
- Preserve idempotency, seat-reservation concurrency, authorization, and compensation behavior.
- Do not add or upgrade dependencies without explaining why and obtaining approval for material changes.
- Do not access another service's database directly.

## 6. Security and identity

- Never hard-code secrets or credentials.
- Never create an administrator with an empty or known default password.
- Do not trust role, customer ID, manager username, or other privileged identity values supplied by the browser.
- Enforce authorization and ownership checks server-side on every protected operation.
- Public registration must not grant privileged roles.
- Keep JWT validation and trusted identity propagation consistent between Auth-Service and ApiGateway.
- Do not log tokens, passwords, mail credentials, or sensitive personal data.
- Treat all user-provided input and external service responses as untrusted.
- Do not weaken security controls merely to make a test or UI flow pass.

## 7. API, database, and Kafka contracts

- Search all callers and consumers before changing an endpoint, DTO field, schema, event name, or payload.
- Keep HTTP status codes and error shapes consistent with existing conventions.
- Use safe parameterized database access and preserve foreign-key/data integrity.
- Never drop tables, delete real data, or apply destructive migrations without explicit approval.
- Preserve payment idempotency and booking/payment recovery paths.
- Do not silently change the established `Paymnt-Service` directory/service identifier.

## 8. Frontend and design

- Follow `design.md` and inspect current pages before editing.
- Reuse current HTML/CSS/JavaScript patterns and assets; do not introduce a new framework without approval.
- Do not change unrelated pages or reformat unrelated files.
- All visible controls must have a real action or be clearly disabled/unavailable.
- Handle loading, empty, success, and error states where relevant.
- Keep layouts responsive and keyboard accessible; use semantic HTML, labels, focus states, and readable contrast.
- Do not store privileged authorization decisions only in localStorage or UI navigation.
- Escape untrusted content before inserting it into HTML.

## 9. Testing and evidence

After a meaningful change, run the most relevant available checks:

1. Syntax/static validation
2. Unit tests for changed behavior
3. Service build/compile
4. Relevant integration/smoke tests when dependencies are available
5. Regression checks for affected flows

Record the exact commands and actual outcomes. Never claim a test or build passed unless it ran successfully. Distinguish `PASS`, `FAIL`, `BLOCKED`, and `NOT RUN`. A port being open is not proof that a business flow works.

## 10. Git and file safety

- Inspect `git status` before editing and the final diff afterward.
- Preserve user changes and avoid unrelated file churn.
- Do not force-push, reset, delete branches, or discard user work without explicit approval.
- Do not commit `.env`, credentials, logs containing sensitive data, generated targets, or archives.
- Keep changes small and logically grouped. Do not claim that changes were committed or pushed unless those operations occurred.

## 11. Task and memory maintenance

- Work on one small task at a time.
- Update `tasks.md` only when status actually changes and use the documented status values.
- Mark a task `DONE` only after its acceptance criteria are verified.
- Update `memory.md` with actual files changed, commands run, outcomes, remaining issues, and next action.
- Update `Architecture.md` or `design.md` only when an approved/verified change warrants it.
- Never fabricate progress or test results.

## 12. Completion response

Report what changed, why, which files changed, tests/commands actually executed, their results, and any remaining risks or limitations. If the environment lacks Maven, dependencies, services, or network access, state the limitation explicitly and do not imply a successful build.
