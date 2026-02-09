package com.example.demo.repo;

import com.example.demo.model.Note;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NoteRepository {

  private final JdbcTemplate jdbc;

  private final RowMapper<Note> mapper = (rs, rowNum) -> new Note(
    rs.getLong("id"),
    rs.getString("body")
  );

  public NoteRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Note create(String body) {
    Long id = jdbc.queryForObject("insert into notes(body) values (?) returning id", Long.class, body);
    return new Note(id == null ? 0L : id, body);
  }

  public List<Note> findAll() {
    return jdbc.query("select id, body from notes order by id", mapper);
  }
}
