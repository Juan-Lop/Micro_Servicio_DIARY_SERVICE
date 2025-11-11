package com.emocional.diary.controller;

import com.emocional.diary.dto.WeeklyStatsResponse;
import com.emocional.diary.dto.RecommendationResponse;
import com.emocional.diary.service.StatsService;
import com.emocional.diary.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;
    private final RecommendationService recommendationService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        
        if (authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        
        String principalName = authentication.getName();
        try {
            return Long.parseLong(principalName);
        } catch (NumberFormatException e) {
        	log.error("❌ Error al parsear el Long userId desde el Principal: {}", principalName);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Token inválido: el ID de usuario ('sub' o 'userId' del JWT) no es un número válido.", e);
        }
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyStatsResponse> getWeeklyStats() {
        Long userId = getCurrentUserId();
        WeeklyStatsResponse weeklyStats = statsService.getWeeklyStats(userId);
        return ResponseEntity.ok(weeklyStats);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationResponse>> getRecommendations() {
        Long userId = getCurrentUserId();
        List<RecommendationResponse> recommendations = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }
}
