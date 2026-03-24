package com.project2.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Auth", description = "Retrieve the currently authenticated user's info from their JWT")
public class AuthController {

  @Operation(summary = "Get current user", description = "Returns the Supabase user ID and email from the authenticated user's JWT.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User info returned successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @GetMapping("/api/me")
  public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
    String supabaseUserId = jwt.getSubject();
    String email = jwt.getClaimAsString("email");

    return ResponseEntity.ok(Map.of(
      "sub", supabaseUserId,
      "email", email
    ));
  }
}