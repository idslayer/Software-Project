package io.logchain.bundler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VerifierController {
    @GetMapping("/verifier")
    public String verifierPage() {
        return "verifier";
    }
}

