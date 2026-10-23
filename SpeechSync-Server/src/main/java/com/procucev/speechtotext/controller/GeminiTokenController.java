package com.procucev.speechtotext.controller;

import com.procucev.speechtotext.service.GeminiTokenService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "http://localhost:4200")
public class GeminiTokenController {

    private final GeminiTokenService tokenService;

    public GeminiTokenController(GeminiTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public Map<String, String> createToken() {
        return Map.of("token", tokenService.createToken());
    }
}