package com.emocional.diary.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SleepStressDataItem {
    private LocalDate date;
    private double sleep;   // Horas de sueño
    private double stress;  // Nivel de estrés
}
