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
        return callAi(prompt);
    }

    /**
     * Asks the AI to extract structured JSON from a job description.
     */
    public String extractJobDescription(String rawText) {
        String prompt = "Extract the following from this job description into JSON format: " +
                "jobTitle, company, requiredSkills, preferredSkills, education, experience. " +
                "Return ONLY valid JSON.\n\nJob Description:\n" + rawText;
        return callAi(prompt);
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
        return callAi(prompt);
    }

    private int currentGeminiIndex = 0;
    private int currentGroqIndex = 0;

    /**
     * Tries Gemini keys in round-robin, then Groq keys in round-robin.
     */
    private String callAi(String prompt) {
        SettingsService settings = SettingsService.getInstance();
        if (!settings.isAiEnabled()) {
            AppLogger.warn("AI is disabled. Returning offline fallback.");
            return offlineFallback(prompt);
        }

        String geminiRaw = settings.getGeminiApiKeys();
        String groqRaw = settings.getGroqApiKeys();
        
        String[] geminiKeys = geminiRaw != null && !geminiRaw.trim().isEmpty() ? geminiRaw.split("\\r?\\n") : new String[0];
        String[] groqKeys = groqRaw != null && !groqRaw.trim().isEmpty() ? groqRaw.split("\\r?\\n") : new String[0];

        if (geminiKeys.length == 0 && groqKeys.length == 0) {
            AppLogger.warn("No AI keys configured. Returning offline fallback.");
            return offlineFallback(prompt);
        }

        // Try Gemini keys
        for (int i = 0; i < geminiKeys.length; i++) {
            if (currentGeminiIndex >= geminiKeys.length) currentGeminiIndex = 0;
            String key = geminiKeys[currentGeminiIndex].trim();
            if (!key.isEmpty()) {
                AppLogger.info("Trying Gemini API key (index " + currentGeminiIndex + ")");
                String result = callGeminiInternal(prompt, key);
                if (result != null && !result.startsWith("API Error")) {
                    return result;
                }
                AppLogger.warn("Gemini key " + currentGeminiIndex + " failed. Rotating...");
            }
            currentGeminiIndex++;
        }

        // Try Groq keys
        for (int i = 0; i < groqKeys.length; i++) {
            if (currentGroqIndex >= groqKeys.length) currentGroqIndex = 0;
            String key = groqKeys[currentGroqIndex].trim();
            if (!key.isEmpty()) {
                AppLogger.info("Trying Groq API key (index " + currentGroqIndex + ")");
                String result = callGroqInternal(prompt, key);
                if (result != null && !result.startsWith("API Error")) {
                    return result;
                }
                AppLogger.warn("Groq key " + currentGroqIndex + " failed. Rotating...");
            }
            currentGroqIndex++;
        }

        AppLogger.error("All AI API keys failed (Gemini and Groq). Returning fallback.");
        return offlineFallback(prompt);
    }

    /**
     * Internal generic call to Google Gemini REST API.
     */
    private String callGeminiInternal(String prompt, String apiKey) {

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
        return "API Error: Unknown failure";
    }

    /**
     * Internal generic call to Groq REST API.
     */
    private String callGroqInternal(String prompt, String apiKey) {
        try {
            String url = "https://api.groq.com/openai/v1/chat/completions";
            String requestBody = mapper.createObjectNode()
                .put("model", "llama3-8b-8192")
                .set("messages", mapper.createArrayNode()
                    .add(mapper.createObjectNode()
                        .put("role", "user")
                        .put("content", prompt)
                    )
                ).toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    String result = choices.get(0).path("message").path("content").asText();
                    return result.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
                }
            } else {
                AppLogger.error("Groq API returned error: " + response.statusCode() + " - " + response.body());
                return "API Error (" + response.statusCode() + ")";
            }
        } catch (Exception e) {
            AppLogger.error("Failed to call Groq API", e);
        }
        return "API Error: Unknown failure";
    }

    public String extractJobSearchUrl(String resumeJson) {
        String prompt = "You are an expert tech recruiter. Based on the following resume JSON, generate exactly ONE highly optimized LinkedIn job search URL. " +
                        "The URL must contain keywords relevant to the candidate's core skills and title. " +
                        "CRITICAL: You MUST append '&f_AL=true' to the URL to filter for Easy Apply jobs. " +
                        "Return ONLY the raw URL string starting with https://www.linkedin.com/jobs/search/?keywords= . Do not include markdown, explanation, or quotes.\n\n" +
                        "Resume JSON:\n" + resumeJson;
                        
        String result = callAi(prompt);
        if (result != null) {
            return result.trim().replace("\"", "");
        }
        return null;
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
