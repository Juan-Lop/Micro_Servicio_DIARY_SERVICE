package com.emocional.diary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorryDistributionItem {
    private String category;  // Nombre de la preocupación (ej: "Trabajo", "Estudios")
    private int count;        // Número de veces que aparece
}
