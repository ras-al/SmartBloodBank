package com.example.smartbloodbank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiService.class);

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void initialize() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            LOGGER.warn("Gemini Service: Environment variable 'GEMINI_API_KEY' is not set. AI features will be disabled.");
        } else {
            LOGGER.info("Gemini Service Initialized with API Key.");
        }
    }

    public static String getCampaignSuggestion(String bloodType, String location) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "AI Service is not configured. Please set the 'GEMINI_API_KEY' environment variable.";
        }

        try {
            String prompt = String.format(
                    "Data suggests a low supply of %s blood in %s. " +
                            "Generate a creative campaign title and a short, motivational promotional message. " +
                            "Format the response as: 'Title: [Your Title]\\nMessage: [Your Message]'",
                    bloodType, location
            );

            String jsonPayload = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                if (textNode.isMissingNode()) {
                    LOGGER.error("AI response is missing the expected text field. Response: {}", response.body());
                    return "AI service returned an unexpected response format.";
                }
                return textNode.asText();
            } else {
                LOGGER.error("Failed to get AI suggestion. Status: {}, Response: {}", response.statusCode(), response.body());
                return "Error from AI Service: " + response.body();
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to generate AI suggestion", e);
            Thread.currentThread().interrupt();
            return "Could not generate AI suggestion at this time.";
        }
    }
}