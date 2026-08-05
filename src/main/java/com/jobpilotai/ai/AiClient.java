package com.jobpilotai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.service.SettingsService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Handles communication with Gemini API (or falls back to offline heuristics).
 */
public class AiClient {

    private static AiClient instance;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private AiClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        mapper = new ObjectMapper();
    }

    public static synchronized AiClient getInstance() {
        if (instance == null) {
            instance = new AiClient();
        }
        return instance;
    }

    /**
     * Asks the AI to extract structured JSON from a raw resume text.
     */
    public String extractResumeData(String rawText) {
        String prompt = "Extract the following details from this resume text into JSON format: " +
                "name, email, skills (comma separated), education, experience, projects, certifications. " +
                "Return ONLY valid JSON.\n\nResume Text:\n" + rawText;
        return callGemini(prompt);
    }

    /**
     * Asks the AI to extract structured JSON from a job description.
     */
    public String extractJobDescription(String rawText) {
        String prompt = "Extract the following from this job description into JSON format: " +
                "jobTitle, company, requiredSkills, preferredSkills, education, experience. " +
                "Return ONLY valid JSON.\n\nJob Description:\n" + rawText;
        return callGemini(prompt);
    }

    /**
     * Asks the AI to generate a cover letter.
     */
    public String generateCoverLetter(String profileData, String resumeData, String jobData) {
        String prompt = "Write a professional, compelling cover letter based on the following context. " +
                "Return ONLY the text of the cover letter.\n\n" +
                "Applicant Profile: " + profileData + "\n" +
                "Resume: " + resumeData + "\n" +
                "Job Description: " + jobData;
        return callGemini(prompt);
    }

    /**
     * Internal generic call to Google Gemini REST API.
     */
    private String callGemini(String prompt) {
        SettingsService settings = SettingsService.getInstance();
        
        // If AI is disabled or key is missing, return fallback heuristics string.
        String apiKey = settings.getGeminiApiKey();
        if (!settings.isAiEnabled() || apiKey == null || apiKey.trim().isEmpty()) {
            AppLogger.warn("AI is disabled or API key missing. Returning offline heuristic fallback.");
            return offlineFallback(prompt);
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;
            
            // Construct the JSON body for Gemini
            String requestBody = mapper.createObjectNode()
                .set("contents", mapper.createArrayNode()
                    .add(mapper.createObjectNode()
                        .set("parts", mapper.createArrayNode()
                            .add(mapper.createObjectNode().put("text", prompt))
                        )
                    )
                ).toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        String result = parts.get(0).path("text").asText();
                        // Strip markdown formatting if AI decided to wrap it in ```json
                        return result.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
                    }
                }
            } else {
                AppLogger.error("Gemini API returned error: " + response.statusCode() + " - " + response.body());
                try {
                    JsonNode errorNode = mapper.readTree(response.body()).path("error");
                    if (!errorNode.isMissingNode()) {
                        String errMsg = errorNode.path("message").asText();
                        if (prompt.contains("JSON format")) {
                            return "{ \"error\": \"API Error (" + response.statusCode() + "): " + errMsg + "\" }";
                        }
                        return "API Error (" + response.statusCode() + "): " + errMsg;
                    }
                } catch (Exception parseEx) {
                    // Ignore parse errors on the error response itself
                }
                if (prompt.contains("JSON format")) {
                    return "{ \"error\": \"Gemini API failed with status " + response.statusCode() + "\" }";
                }
                return "Gemini API failed with status " + response.statusCode();
            }

        } catch (Exception e) {
            AppLogger.error("Failed to call Gemini API", e);
        }

        return offlineFallback(prompt);
    }

    /**
     * Fallback method if API is unavailable or disabled.
     */
    private String offlineFallback(String prompt) {
        // Very rudimentary fallback
        if (prompt.contains("JSON format")) {
            return "{ \"error\": \"Offline mode enabled. AI extraction requires Gemini API Key.\" }";
        }
        return "Offline Mode: Cannot generate complex content without an API key. Please add a Gemini API Key in Settings and enable AI features.";
    }
}
