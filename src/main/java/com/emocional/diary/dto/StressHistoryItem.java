package com.emocional.diary.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StressHistoryItem {
    private LocalDate date;
    private double value; // 'value' para coincidir con lo que espera el componente del frontend
}
