package com.example.demo.web;

import com.example.demo.api.NotesController;
import com.example.demo.model.Note;
import com.example.demo.repo.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotesController.class)
class NotesControllerWebMvcTest {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  NoteRepository noteRepository;

  @Test
  void post_happyPath_returnsNoteJson() throws Exception {
    when(noteRepository.create("hello")).thenReturn(new Note(1L, "hello"));

    mvc.perform(post("/api/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"body\":\"hello\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.body").value("hello"));

    verify(noteRepository).create("hello");
  }

  @Test
  void post_blankBody_returns400() throws Exception {
    mvc.perform(post("/api/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"body\":\"\"}"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(noteRepository);
  }

  @Test
  void post_nullBody_returns400() throws Exception {
    mvc.perform(post("/api/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"body\":null}"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(noteRepository);
  }

  @Test
  void post_missingField_returns400() throws Exception {
    mvc.perform(post("/api/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(noteRepository);
  }

  @Test
  void post_invalidJson_returns400() throws Exception {
    mvc.perform(post("/api/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{invalid"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(noteRepository);
  }

  @Test
  void post_noContentType_returns415() throws Exception {
    mvc.perform(post("/api/notes")
        .content("{\"body\":\"hello\"}"))
      .andExpect(status().isUnsupportedMediaType());

    verifyNoInteractions(noteRepository);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Привет мир",
    "你好世界",
    "\uD83D\uDE00\uD83D\uDE80\uD83C\uDF1F"
  })
  void post_unicodeAndEmoji_accepted(String body) throws Exception {
    when(noteRepository.create(body)).thenReturn(new Note(1L, body));

    mvc.perform(post("/api/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"body\":\"" + body + "\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.body").value(body));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"\"", "\"   \"", "\"\t\""})
  void post_blankVariations_returns400(String bodyValue) throws Exception {
    mvc.perform(post("/api/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"body\":" + bodyValue + "}"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(noteRepository);
  }

  @Test
  void get_emptyList_returnsEmptyArray() throws Exception {
    when(noteRepository.findAll()).thenReturn(Collections.emptyList());

    mvc.perform(get("/api/notes"))
      .andExpect(status().isOk())
      .andExpect(content().json("[]"));
  }

  @Test
  void get_withData_returnsJsonArray() throws Exception {
    when(noteRepository.findAll()).thenReturn(List.of(
      new Note(1L, "first"),
      new Note(2L, "second")
    ));

    mvc.perform(get("/api/notes"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].body").value("first"))
      .andExpect(jsonPath("$[1].id").value(2))
      .andExpect(jsonPath("$[1].body").value("second"));
  }
}
