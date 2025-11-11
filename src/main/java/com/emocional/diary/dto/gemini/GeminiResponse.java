package com.emocional.diary.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GeminiResponse {
    private List<Candidate> candidates;
    
    @JsonProperty("promptFeedback")
    private PromptFeedback promptFeedback;

    @Data
    public static class Candidate {
        private Content content;
        
        @JsonProperty("finishReason")
        private String finishReason;
    }

    @Data
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class PromptFeedback {
        @JsonProperty("blockReason")
        private String blockReason;
    }
}