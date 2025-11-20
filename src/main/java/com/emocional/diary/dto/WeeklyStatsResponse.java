package com.emocional.diary.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WeeklyStatsResponse {
    private double averageStress;
    private double previousWeekStress;
    private double averageSleep;
    private String mainWorry;
    private List<StressHistoryItem> stressHistory;

    // Datos para gráfica de "Correlación Sueño vs. Estrés"
    private List<SleepStressDataItem> sleepStressData;

    // Datos para gráfica de "Distribución de Preocupaciones"
    private List<WorryDistributionItem> worriesDistribution;
}
