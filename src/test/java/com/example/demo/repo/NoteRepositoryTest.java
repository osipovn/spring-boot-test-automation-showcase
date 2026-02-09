package com.example.demo.repo;

import com.example.demo.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("docker")
@EnabledIfSystemProperty(named = "RUN_DOCKER_TESTS", matches = "true")
@Testcontainers
@SpringBootTest
class NoteRepositoryTest {

  // Use AWS Public ECR mirror to avoid Docker Hub unauthenticated pull rate limits.
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

  @Autowired
  NoteRepository repo;

  @Autowired
  JdbcTemplate jdbc;

  @BeforeEach
  void cleanUp() {
    jdbc.execute("TRUNCATE TABLE notes RESTART IDENTITY");
  }

  @Test
  void createAndRead() {
    Note a = repo.create("hello");
    Note b = repo.create("world");

    List<Note> notes = repo.findAll();
    assertEquals(2, notes.size());
    assertEquals("hello", notes.get(0).body());
    assertEquals("world", notes.get(1).body());
    assertTrue(a.id() > 0);
    assertTrue(b.id() > 0);
  }

  @Test
  void create_sequentialIds() {
    Note a = repo.create("first");
    Note b = repo.create("second");
    Note c = repo.create("third");

    assertTrue(a.id() < b.id(), "IDs should be sequential: a < b");
    assertTrue(b.id() < c.id(), "IDs should be sequential: b < c");
  }

  @Test
  void create_unicodePreserved() {
    String unicode = "Привет мир 你好世界 \uD83D\uDE00\uD83D\uDE80";
    Note note = repo.create(unicode);

    List<Note> notes = repo.findAll();
    Note found = notes.stream().filter(n -> n.id() == note.id()).findFirst().orElseThrow();
    assertEquals(unicode, found.body());
  }

  @Test
  void create_longText() {
    String longText = "a".repeat(10_000);
    Note note = repo.create(longText);

    assertTrue(note.id() > 0);
    List<Note> notes = repo.findAll();
    Note found = notes.stream().filter(n -> n.id() == note.id()).findFirst().orElseThrow();
    assertEquals(10_000, found.body().length());
    assertEquals(longText, found.body());
  }

  @Test
  void findAll_orderedById() {
    Note z = repo.create("zzz");
    Note a = repo.create("aaa");
    Note m = repo.create("mmm");

    List<Note> notes = repo.findAll();

    // Find indices of our notes in the result
    int idxZ = -1, idxA = -1, idxM = -1;
    for (int i = 0; i < notes.size(); i++) {
      if (notes.get(i).id() == z.id()) idxZ = i;
      if (notes.get(i).id() == a.id()) idxA = i;
      if (notes.get(i).id() == m.id()) idxM = i;
    }

    assertTrue(idxZ < idxA, "z was created first, should appear before a (ordered by id, not alphabetically)");
    assertTrue(idxA < idxM, "a was created second, should appear before m (ordered by id, not alphabetically)");
  }
}
