package com.example.smartbloodbank.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class GeminiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiService.class);
    private static GenerativeModel model;

    public static void initialize() {
        String projectId = "donorlink-app-4149e";
        String location = "us-central1";
        String modelName = "gemini-1.5-flash-001";

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            model = new GenerativeModel(modelName, vertexAI);
            LOGGER.info("Gemini Service Initialized successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Gemini Service. AI features will be disabled.", e);
        }
    }

    public static String getCampaignSuggestion(String bloodType, String location) {
        if (model == null) {
            LOGGER.warn("Gemini AI Service is not available.");
            return "AI Service is not available.";
        }
        try {
            String prompt = String.format(
                    "Data suggests a low supply of %s blood in %s. " +
                            "Generate a creative campaign title and a short, motivational promotional message. " +
                            "Format the response as: 'Title: [Your Title]\\nMessage: [Your Message]'",
                    bloodType, location
            );
            GenerateContentResponse response = model.generateContent(prompt);
            return ResponseHandler.getText(response);
        } catch (IOException e) {
            LOGGER.error("Failed to generate AI suggestion", e);
            return "Could not generate AI suggestion at this time.";
        }
    }
}