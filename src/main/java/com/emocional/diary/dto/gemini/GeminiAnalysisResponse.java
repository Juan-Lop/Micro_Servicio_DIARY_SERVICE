package com.emocional.diary.dto.gemini;

import lombok.Data;
import java.util.List;

@Data
public class GeminiAnalysisResponse {
    private String emotion;
    private Integer intensity;
    private String summary;
    private List<String> keywords;
}