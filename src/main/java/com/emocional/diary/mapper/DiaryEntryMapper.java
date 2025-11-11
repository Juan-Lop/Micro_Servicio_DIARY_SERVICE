package com.emocional.diary.mapper; // Crea este nuevo paquete

import com.emocional.diary.dto.DiaryEntryResponse;
import com.emocional.diary.model.DiaryEntry;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DiaryEntryMapper {

	/**
     * Convierte una entidad DiaryEntry a un DTO de respuesta, mapeando los nuevos nombres de campos.
     * @param entry La entidad fuente.
     * @return El DTO de respuesta.
     */
	
	public DiaryEntryResponse toResponseDto(DiaryEntry entry) {
		if(entry == null) {
			return null;
		}
		
		
		//Mapeo entre los campos del DTO (target) y la nueva Entidad (source)
		
		return DiaryEntryResponse.builder()
				.id(entry.getId())
				
				//Mapeo del ID de Usuario: Ahora es un Long directo en la entidad
				.userId(entry.getUserId())
				
				//Datos del Check-in
				.entryText(entry.getContent()) 
				.entryDate(entry.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()) 
				
				.moodRating(entry.getUserMoodRating())
				.stressLevel(entry.getUserStressLevel())
				.sleepHours(entry.getUserSleepHours())
				
				.mainWorry(entry.getMainWorry())
				
				// --- Mapeo de campos de la IA
				.detectedEmotion(entry.getAiEmotion())
				.emotionalIntensity(entry.getAiIntensity())
				.keyWords(entry.getAiKeywords())
				.aiSummary(entry.getAiSummary())
				.build();
				
				
				
				
				
		
	}
	
	/**
     * Convierte una lista de entidades DiaryEntry a una lista de DTOs de respuesta.
     * @param entries La lista de entidades fuente.
     * @return La lista de DTOs.
     */
    public List<DiaryEntryResponse> toResponseDtoList(List<DiaryEntry> entries) {
        if (entries == null) {
            return Collections.emptyList();
        }
        return entries.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
	
	
}