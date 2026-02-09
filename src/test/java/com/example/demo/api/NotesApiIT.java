package com.example.demo.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named = "RUN_DOCKER_TESTS", matches = "true")
class NotesApiIT {

  private static final DockerImageName POSTGRES_IMAGE = DockerImageName
    .parse("public.ecr.aws/docker/library/postgres:16-alpine")
    .asCompatibleSubstituteFor("postgres");

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.sql.init.mode", () -> "always");
  }

  @LocalServerPort
  int port;

  @BeforeEach
  void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void createThenList() {
    given()
      .contentType("application/json")
      .body("{\"body\":\"hello\"}")
    .when()
      .post("/api/notes")
    .then()
      .statusCode(200)
      .body("id", greaterThan(0))
      .body("body", equalTo("hello"));

    given()
    .when()
      .get("/api/notes")
    .then()
      .statusCode(200)
      .body("size()", greaterThanOrEqualTo(1))
      .body("body", hasItem("hello"));
  }

  @Test
  void post_blankBody_returns400() {
    given()
      .contentType("application/json")
      .body("{\"body\":\"\"}")
    .when()
      .post("/api/notes")
    .then()
      .statusCode(400);
  }

  @Test
  void post_missingBodyField_returns400() {
    given()
      .contentType("application/json")
      .body("{}")
    .when()
      .post("/api/notes")
    .then()
      .statusCode(400);
  }

  @Test
  void post_noContentType_returns415() {
    given()
      .body("{\"body\":\"hello\"}")
    .when()
      .post("/api/notes")
    .then()
      .statusCode(415);
  }

  @Test
  void get_returns200AndArray() {
    given()
    .when()
      .get("/api/notes")
    .then()
      .statusCode(200)
      .body("$", instanceOf(java.util.List.class));
  }

  @Test
  void post_unicodeBody_savedCorrectly() {
    String unicodeBody = "Привет \uD83C\uDF1F 你好";

    given()
      .contentType("application/json")
      .body("{\"body\":\"" + unicodeBody + "\"}")
    .when()
      .post("/api/notes")
    .then()
      .statusCode(200)
      .body("body", equalTo(unicodeBody));
  }
}
