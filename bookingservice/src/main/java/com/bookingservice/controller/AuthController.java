// src/main/java/com/bookingservice/web/AuthController.java
package com.bookingservice.controller;

import com.bookingservice.dto.LoginRequest;
import com.bookingservice.service.CustomOidcUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final CustomOidcUserService userService;

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (user == null) {
            Map<String, Object> map = userService.loadAdmin(authHeader.substring(7).trim());
            if (map != null) return map;
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        log.info("User details: {}", user.getAttributes());
        return Map.of(
            "name", user.getAttribute("name"),
            "email", user.getAttribute("email"),
            "picture", user.getAttribute("picture"),
            "appUserId", user.getAttribute("appUserId"),
            "role", user.getAttribute("role")
        );

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        return ResponseEntity.ok(Map.of("token", token));

    }
}
