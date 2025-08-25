// src/main/java/com/bookingservice/web/AuthController.java
package com.bookingservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        log.info("User details: {}", user.getAttributes());
        return Map.of(
            "name", user.getAttribute("name"),
            "email", user.getAttribute("email"),
            "picture", user.getAttribute("picture"),
            "appUserId", user.getAttribute("appUserId")
        );

    }
}
