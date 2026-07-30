# Ledger Engine — Implementation Roadmap

## Phase 1 — Fix & Polish (foundation)

- [ ] **1.1** Fix parent POM version (`pom.xml`: `4.1.0` → `3.3.4` or latest 3.x)
- [ ] **1.2** Fix invalid artifact name (`spring-boot-starter-webmvc` → `spring-boot-starter-web`, same for test)
- [ ] **1.3** Fix `ObjectMapper` import (`tools.jackson` → `com.fasterxml.jackson`)
- [ ] **1.4** Remove dead exception classes (`AccountNotFound.java`, `InsufficientFundsException.java`)
- [ ] **1.5** Switch `OutboxScheduler` to use `fetchPendingEvents()` (SKIP LOCKED) instead of `findByStatus(PENDING)`
- [ ] **1.6** Add GET endpoints:
  - `GET /api/v1/accounts/{id}` — account details + calculated balance
  - `GET /api/v1/accounts/{id}/entries` — paginated account entries
  - `GET /api/v1/transactions/{referenceId}` — journal entry + entries
- [ ] **1.7** Write meaningful tests:
  - Unit test for `TransferService.executeTransfer` (idempotency, insufficient balance, success)
  - Unit test for `OutboxScheduler.processPendingOutboxEvents`
  - Integration test for `TransferController` (full POST → verify DB state)
  - Integration test for new GET endpoints

## Phase 2 — Make It Shine

- [ ] **2.1** Implement Redis-based idempotency guard (first-level cache before DB lookup)
- [ ] **2.2** Add OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`)
- [ ] **2.3** Write `Dockerfile` for the app (multi-stage, JAR)
- [ ] **2.4** Add app service to `docker-compose.yml`
- [ ] **2.5** Set up GitHub Actions CI (build → test)
- [ ] **2.6** Add Spring Boot Actuator (`/health`, `/info`, `/metrics`)

## Phase 3 — Portfolio-Ready

- [ ] **3.1** Build a simple frontend (React/Vue) to visualize accounts & transfers
- [ ] **3.2** Add a RabbitMQ consumer that handles `TRANSFER_COMPLETED` events
- [ ] **3.3** Write a proper README with architecture diagram (Mermaid) + setup guide + story
- [ ] **3.4** Add integration tests with Testcontainers (PostgreSQL + RabbitMQ + Redis)
- [ ] **3.5** Deploy demo to Railway / Render / Fly.io with live URL
