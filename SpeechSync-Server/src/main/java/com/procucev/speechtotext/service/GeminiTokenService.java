package com.procucev.speechtotext.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GeminiTokenService {

    private final RestClient restClient;
    private final String apiKey;

    public GeminiTokenService(
            @Value("${gemini.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    public String createToken() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini API key is not configured");
        }

        Instant now = Instant.now();

        Map<String, Object> request = Map.of(
                "uses", 1,
                "expireTime",
                now.plus(30, ChronoUnit.MINUTES).toString(),
                "newSessionExpireTime",
                now.plus(1, ChronoUnit.MINUTES).toString()
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/auth_tokens")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response == null
                    || !(response.get("name") instanceof String token)
                    || token.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini did not return a valid token");
            }

            return token;

        } catch (RestClientResponseException ex) {
            System.err.println(
                    "Gemini token API error: "
                    + ex.getResponseBodyAsString());

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Gemini token creation failed: "
                    + ex.getStatusCode(),
                    ex);
        }
    }
}