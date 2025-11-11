package com.emocional.diary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationResponse {
    private String id;
    private String title;
    private String description;
    private String category;
    private String priority;
}
