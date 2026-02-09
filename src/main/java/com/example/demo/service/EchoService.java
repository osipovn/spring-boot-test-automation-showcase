package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class EchoService {
  public String echo(String q) {
    return q;
  }
}
