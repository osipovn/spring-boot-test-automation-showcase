package com.example.demo;

import com.example.demo.service.EchoService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class EchoController {

  private final EchoService echoService;

  public EchoController(EchoService echoService) {
    this.echoService = echoService;
  }

  @GetMapping("/api/echo")
  public String echo(@RequestParam("q") @NotBlank String q) {
    return echoService.echo(q);
  }
}
