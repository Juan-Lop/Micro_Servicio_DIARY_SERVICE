package com.emocional.diary.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GeminiRequest {
    private List<Content> contents;
    
    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;

    @Data
    @Builder
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @Builder
    public static class Part {
        private String text;
    }

    @Data
    @Builder
    public static class GenerationConfig {
        private Double temperature;
        
        @JsonProperty("maxOutputTokens")
        private Integer maxOutputTokens;
        
        @JsonProperty("topP")
        private Double topP;
        
        @JsonProperty("topK")
        private Integer topK;
    }
}