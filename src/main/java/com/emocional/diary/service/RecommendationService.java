package com.emocional.diary.service;

import com.emocional.diary.dto.RecommendationResponse;

import java.util.List;

public interface RecommendationService {
    List<RecommendationResponse> getRecommendations(Long userId);
}
