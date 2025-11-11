package com.emocional.diary.service;

import com.emocional.diary.dto.WeeklyStatsResponse;

public interface StatsService {
    WeeklyStatsResponse getWeeklyStats(Long userId);
}
