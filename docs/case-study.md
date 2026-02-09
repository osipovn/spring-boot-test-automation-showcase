# Case study: test automation starter

## Goal
Create a minimal Spring Boot API with a comprehensive, production-grade test suite demonstrating depth across the entire testing pyramid.

## What's included
- Spring Boot 3 + Java 21
- Two REST endpoints: `/api/echo` (stateless) and `/api/notes` (CRUD with PostgreSQL)
- **68 test methods** across 7 test classes:

### Test structure

| Layer | Class | Tests | Techniques |
|---|---|---|---|
| Unit | `EchoServiceTest` | 19 | `@ParameterizedTest` + `@ValueSource`, `@NullAndEmptySource`, `@MethodSource` (unicode, HTML injection, SQL injection, special chars) |
| Web slice | `EchoControllerWebMvcTest` | 11 | `@WebMvcTest`, `@CsvSource` input/output pairs, blank/whitespace variations, validation error body assertions |
| Web slice | `NotesControllerWebMvcTest` | 14 | `@WebMvcTest`, POST happy/blank/null/missing/invalid/415, unicode/emoji, GET empty/with data, `jsonPath` |
| Integration | `EchoControllerTest` | 7 | `@SpringBootTest` + MockMvc, unicode through full stack, `ValidationErrorHandler` JSON body |
| Repository | `NoteRepositoryTest` | 5 | Testcontainers + PostgreSQL, sequential IDs, unicode preservation, 10K-char text, order-by-id |
| API E2E | `EchoApiIT` | 6 | REST Assured, blank/missing query, `@CsvSource` unicode/emoji, URL-encoded special chars |
| API E2E | `NotesApiIT` | 6 | REST Assured, POST/GET lifecycle, 415 without Content-Type, unicode persistence |

### Key testing techniques
- **Parameterized tests** — `@ValueSource`, `@NullAndEmptySource`, `@CsvSource`, `@MethodSource` for data-driven coverage
- **Negative testing** — blank, null, missing fields, malformed JSON, wrong Content-Type
- **Edge cases** — unicode (Cyrillic, CJK, emoji), HTML/SQL injection strings, whitespace variations (`""`, `"   "`, `"\t"`), 10K-char text
- **Validation error assertions** — verifying JSON error response structure, not just status codes
- **Slice tests** — fast `@WebMvcTest` with `@MockitoBean` for isolated controller testing
- **Testcontainers** — real PostgreSQL instead of H2 for repository integration tests
- **REST Assured** — fluent HTTP-level assertions with Hamcrest matchers
- **Docker-gated tests** — `@EnabledIfSystemProperty` to separate fast and slow tests

### Infrastructure
- GitHub Actions workflow running `mvn test` with Maven cache
- Docker packaging: multi-stage `Dockerfile` + `docker-compose.yml` with PostgreSQL
- H2 in-memory DB for fast local tests, Testcontainers PostgreSQL for integration tests
