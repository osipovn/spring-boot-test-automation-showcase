package com.example.demo.api;

import jakarta.validation.constraints.NotBlank;

public record CreateNoteRequest(
  @NotBlank String body
) {}
