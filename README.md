# Spring Boot Test Automation Starter — Showcase

This repository is a **showcase**:
**"Spring Boot Test Automation Starter (JUnit5 + Integration Tests + CI Setup)"**.

## Run locally

```bash
mvn test

# integration tests (docker + testcontainers)
mvn test -DRUN_DOCKER_TESTS=true

mvn spring-boot:run
# then:
# curl "http://localhost:8080/api/echo?q=hello"
```

## Docker

```bash
# build and start (app + PostgreSQL)
docker compose up -d

# check the app
curl "http://localhost:8080/api/echo?q=hello"

# stop
docker compose down
```

## What this repo demonstrates

- Spring Boot 3 + Java 21
- A small REST API
  - `GET /api/echo?q=...` — echo with input validation
  - `POST /api/notes` and `GET /api/notes` — CRUD with PostgreSQL persistence
- **68 test methods** across 7 test classes covering every layer of the testing pyramid:

### Testing pyramid

| Layer | Class | What it tests | Key techniques |
|---|---|---|---|
| **Unit** | `EchoServiceTest` | Pure service logic | `@ParameterizedTest`, `@ValueSource`, `@NullAndEmptySource`, `@MethodSource` |
| **Web slice** | `EchoControllerWebMvcTest` | Echo controller in isolation | `@WebMvcTest`, `@MockitoBean`, `@CsvSource`, validation error body assertions |
| **Web slice** | `NotesControllerWebMvcTest` | Notes controller in isolation | `@WebMvcTest`, `@MockitoBean`, `jsonPath`, Content-Type checks, blank/null/missing/invalid JSON |
| **Integration** | `EchoControllerTest` | Full Spring context + MockMvc | `@SpringBootTest`, unicode through full stack, `ValidationErrorHandler` body check |
| **Repository** | `NoteRepositoryTest` | JDBC repo against real Postgres | Testcontainers, sequential IDs, unicode, 10K-char text, ordering |
| **API (E2E)** | `EchoApiIT` | Echo endpoint via REST Assured | URL-encoded special chars, unicode/emoji, validation error body |
| **API (E2E)** | `NotesApiIT` | Notes endpoints via REST Assured | POST/GET lifecycle, 415 without Content-Type, unicode persistence |

### Testing techniques demonstrated

- **Parameterized tests** — `@ValueSource`, `@NullAndEmptySource`, `@CsvSource`, `@MethodSource`
- **Negative testing** — blank, null, missing fields, invalid JSON, wrong Content-Type
- **Edge cases** — unicode (Cyrillic, CJK, emoji), HTML/SQL injection strings, 10K-char text, whitespace variations
- **Validation error body** — asserting JSON structure of error responses (`$.error`, `$.message`)
- **Slice tests** — fast `@WebMvcTest` with mocked dependencies
- **Testcontainers** — real PostgreSQL for repository and API integration tests
- **REST Assured** — fluent API-level assertions with Hamcrest matchers

- Docker packaging (multi-stage `Dockerfile` + `docker-compose.yml` with PostgreSQL)
- CI that runs tests on every push/PR

See `docs/case-study.md` for the case-study writeup.
