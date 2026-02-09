package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EchoServiceTest {

  private final EchoService service = new EchoService();

  @Test
  void echo_returnsSameValue() {
    assertEquals("hello", service.echo("hello"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"hello", "world", "Spring Boot", "12345", "a"})
  void echo_returnsExactInput(String input) {
    assertEquals(input, service.echo(input));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void echo_handlesNullAndEmpty(String input) {
    assertEquals(input, service.echo(input));
  }

  @ParameterizedTest
  @MethodSource("specialStrings")
  void echo_handlesSpecialCharacters(String input) {
    assertEquals(input, service.echo(input));
  }

  static Stream<String> specialStrings() {
    return Stream.of(
      "Привет мир",
      "你好世界",
      "こんにちは",
      "\uD83D\uDE00\uD83D\uDE80\uD83C\uDF1F",
      "<script>alert('xss')</script>",
      "<img src=x onerror=alert(1)>",
      "Robert'); DROP TABLE notes;--",
      "line1\nline2\nline3",
      "\t\ttabs\there",
      "   leading and trailing   ",
      "special: @#$%^&*()_+-={}[]|\\:\";<>?,./"
    );
  }
}
