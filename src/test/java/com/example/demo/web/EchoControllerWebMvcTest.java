package com.example.demo.web;

import com.example.demo.EchoController;
import com.example.demo.error.ValidationErrorHandler;
import com.example.demo.service.EchoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EchoController.class)
@Import(ValidationErrorHandler.class)
class EchoControllerWebMvcTest {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  EchoService echoService;

  @Test
  void echo_returnsValue_fastSliceTest() throws Exception {
    when(echoService.echo("hello")).thenReturn("hello");

    mvc.perform(get("/api/echo").queryParam("q", "hello"))
      .andExpect(status().isOk())
      .andExpect(content().string("hello"));

    verify(echoService).echo("hello");
  }

  @Test
  void echo_blankQuery_returns400() throws Exception {
    mvc.perform(get("/api/echo").queryParam("q", ""))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(echoService);
  }

  @Test
  void echo_blankQuery_returnsValidationErrorBody() throws Exception {
    mvc.perform(get("/api/echo").queryParam("q", ""))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error").value("validation_error"))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void echo_missingQueryParam_returns400() throws Exception {
    mvc.perform(get("/api/echo"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(echoService);
  }

  @ParameterizedTest
  @CsvSource({
    "hello, hello",
    "Spring Boot, Spring Boot",
    "12345, 12345",
    "a, a"
  })
  void echo_inputOutputPairs(String input, String expected) throws Exception {
    when(echoService.echo(input)).thenReturn(input);

    mvc.perform(get("/api/echo").queryParam("q", input))
      .andExpect(status().isOk())
      .andExpect(content().string(expected));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t"})
  void echo_blankVariations_returns400(String blankValue) throws Exception {
    mvc.perform(get("/api/echo").queryParam("q", blankValue))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(echoService);
  }
}
