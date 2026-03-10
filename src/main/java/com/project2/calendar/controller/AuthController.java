package com.project2.calendar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

  @GetMapping("/me")
  public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
    String supabaseUserId = jwt.getSubject();
    String email = jwt.getClaimAsString("email");

    return ResponseEntity.ok(Map.of(
      "sub", supabaseUserId,
      "email", email
    ));
  }
}