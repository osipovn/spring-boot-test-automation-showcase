package com.example.demo.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
class EchoApiIT {

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
  void echo_ok() {
    given()
      .queryParam("q", "hello")
    .when()
      .get("/api/echo")
    .then()
      .statusCode(200)
      .body(equalTo("hello"));
  }

  @Test
  void echo_blankQuery_returns400WithValidationError() {
    given()
      .queryParam("q", "")
    .when()
      .get("/api/echo")
    .then()
      .statusCode(400)
      .body("error", equalTo("validation_error"))
      .body("message", notNullValue());
  }

  @Test
  void echo_missingQuery_returns400() {
    given()
    .when()
      .get("/api/echo")
    .then()
      .statusCode(400);
  }

  @ParameterizedTest
  @CsvSource({
    "hello, hello",
    "Привет, Привет",
    "你好, 你好",
    "\uD83D\uDE00\uD83D\uDE80, \uD83D\uDE00\uD83D\uDE80"
  })
  void echo_unicodeAndEmoji(String input, String expected) {
    given()
      .queryParam("q", input)
    .when()
      .get("/api/echo")
    .then()
      .statusCode(200)
      .body(equalTo(expected));
  }

  @Test
  void echo_specialCharsUrlEncoded() {
    given()
      .queryParam("q", "a&b=c?d#e")
    .when()
      .get("/api/echo")
    .then()
      .statusCode(200)
      .body(equalTo("a&b=c?d#e"));
  }
}
