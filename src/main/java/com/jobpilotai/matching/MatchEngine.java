package com.jobpilotai.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilotai.ai.AiClient;
import com.jobpilotai.logs.AppLogger;

/**
 * Calculates a match score between a parsed resume and a parsed job description.
 */
public class MatchEngine {

    public static class MatchResult {
        public int overallScore;
        public String technicalMatch;
        public String experienceMatch;
        public String educationMatch;
        public String summary;

        public MatchResult(int overallScore, String technicalMatch, String experienceMatch, String educationMatch, String summary) {
            this.overallScore = overallScore;
            this.technicalMatch = technicalMatch;
            this.experienceMatch = experienceMatch;
            this.educationMatch = educationMatch;
            this.summary = summary;
        }
    }

    /**
     * Calculates compatibility using AI heuristics.
     */
    public static MatchResult calculateMatch(String resumeJson, String jobJson) {
        // Fallback or rudimentary calculation without wasting tokens, 
        // OR we can pass it back to Gemini for a full analysis.
        // For Enterprise quality, we pass it to Gemini for a scoring summary.

        String prompt = "Act as an expert technical recruiter. Analyze this Resume against this Job Description.\n" +
                "Provide a JSON response with:\n" +
                "overallScore (integer 0-100)\n" +
                "technicalMatch (short explanation of skill overlap)\n" +
                "experienceMatch (short explanation of experience match)\n" +
                "educationMatch (short explanation of education match)\n" +
                "summary (overall verdict: Excellent, Good, Average, or Needs Improvement, plus one sentence explanation).\n\n" +
                "Resume JSON:\n" + resumeJson + "\n\n" +
                "Job Description JSON:\n" + jobJson;

        try {
            String result = AiClient.getInstance().extractResumeData(prompt); // We reuse the extraction endpoint logic
            
            // Re-parse the result
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(result);
            
            int score = root.path("overallScore").asInt(50);
            String tech = root.path("technicalMatch").asText("Not provided");
            String exp = root.path("experienceMatch").asText("Not provided");
            String edu = root.path("educationMatch").asText("Not provided");
            String summary = root.path("summary").asText("No summary provided");

            return new MatchResult(score, tech, exp, edu, summary);
        } catch (Exception e) {
            AppLogger.error("Failed to calculate AI match", e);
            return new MatchResult(0, "Error", "Error", "Error", "Failed to calculate match.");
        }
    }
}
