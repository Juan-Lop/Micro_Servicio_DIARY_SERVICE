package com.emocional.diary.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryEntryRequest {
	
	 /**
     * Contenido textual de la entrada del diario. Mínimo 50 caracteres para un análisis significativo.
     */
    @NotBlank(message = "El contenido del diario (entryText) no puede estar vacío.")
    @Size(min = 50, max = 5000, message = "El contenido debe tener entre 50 y 5000 caracteres.")
    private String entryText;

    /**
     * Calificación numérica que el usuario asigna a su estado de ánimo (ej: 1 a 10).
     */
    @NotNull(message = "La calificación de ánimo (moodRating) es obligatoria.")
    @Min(value = 1, message = "La calificación de ánimo debe ser al menos 1.")
    @Max(value = 10, message = "La calificación de ánimo no debe exceder 10.")
    private Integer moodRating;

    /**
     * Nivel de estrés del usuario (ej: 1 a 10).
     */
    @NotNull(message = "El nivel de estrés (stressLevel) es obligatorio.")
    @Min(value = 1, message = "El nivel de estrés debe ser al menos 1.")
    @Max(value = 10, message = "El nivel de estrés no debe exceder 10.")
    private Integer stressLevel;

    /**
     * Horas de sueño reportadas por el usuario.
     */
    @NotNull(message = "Las horas de sueño (sleepHours) son obligatorias.")
    @Min(value = 1, message = "Las horas de sueño deben ser al menos 1.")
    @Max(value = 16, message = "Las horas de sueño no deben exceder 16.")
    private Integer sleepHours;

    /**
     * Principal preocupación o tema del día del usuario (Texto).
     */
    @NotBlank(message = "La principal preocupación (mainWorry) no puede estar vacía.")
    @Size(min = 5, max = 255, message = "La preocupación debe tener entre 5 y 255 caracteres.")
    private String mainWorry;
    
   

}
