package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EchoControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  void echo_returnsValue() throws Exception {
    mvc.perform(get("/api/echo").queryParam("q", "hello"))
      .andExpect(status().isOk())
      .andExpect(content().string("hello"));
  }

  @Test
  void echo_requiresNonBlankQuery() throws Exception {
    mvc.perform(get("/api/echo").queryParam("q", ""))
      .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Привет мир", "你好世界", "こんにちは", "\uD83D\uDE00\uD83D\uDE80"})
  void echo_unicodeStrings_throughFullStack(String input) throws Exception {
    mvc.perform(get("/api/echo").queryParam("q", input))
      .andExpect(status().isOk())
      .andExpect(content().string(input));
  }

  @Test
  void echo_blankQuery_returnsValidationErrorBody() throws Exception {
    mvc.perform(get("/api/echo").queryParam("q", ""))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error").value("validation_error"))
      .andExpect(jsonPath("$.message").isString());
  }
}
