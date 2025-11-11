package com.emocional.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta que representa una entrada de diario después de ser creada y analizada.
 * Se utiliza para devolver datos limpios al cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DiaryEntryResponse {

	private Long id;
    private Long userId; // ID del usuario al que pertenece la entrada

    // Datos del Check-in (mapeados desde la entidad)
    private String entryText; // Contenido del diario
    private LocalDateTime entryDate; // Fecha de creación
    private Integer moodRating; // Nivel de ánimo (e.g., 1-10)
    private Integer stressLevel; // Nivel de estrés (e.g., 1-10)
    private Integer sleepHours;  // Horas de sueño
    private String mainWorry; // Principal preocupación (Texto)

    // Resultados del Análisis de IA
    private String detectedEmotion; // Emoción principal detectada
    private Integer emotionalIntensity; // Intensidad emocional (1-10)
    // El DTO usa List<String> para una mejor representación JSON, 
    // aunque la entidad pueda almacenarlo como String. El Mapper se encarga de la conversión.
    private List<String> keyWords; 
    private String aiSummary; // Resumen conciso
}