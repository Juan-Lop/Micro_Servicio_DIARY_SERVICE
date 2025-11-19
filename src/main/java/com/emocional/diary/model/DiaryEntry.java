package com.emocional.diary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa una entrada de diario personal.
 * Contiene el texto libre del usuario y los resultados del análisis de IA,
 * además de los datos del check-in (estrés, ánimo, sueño).
 */
@Entity
@Table(name = "diary_entry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campo obligatorio para asociar la entrada al usuario (obtenido del JWT)
    @Column(nullable = false)
    private Long userId;

    // Contenido del diario (texto libre)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // --- Campos de Check-in Reportados por el Usuario ---
    
    // Nivel de estrés reportado por el usuario (escala 1-10)
    @Column(nullable = false)
    private Integer userStressLevel;
    
    // Nivel de ánimo reportado por el usuario (escala 1-10) - ¡Nuevo campo!
    @Column(nullable = false)
    private Integer userMoodRating;
    
    // Horas de sueño reportadas por el usuario - ¡Nuevo campo!
    @Column(nullable = false)
    private Integer userSleepHours;

    // Principal preocupación del usuario
    @Column(length = 500)
    private String mainWorry;

    // --- Campos de Análisis de IA (Resultado de OpenAI) ---

    // Emoción principal detectada por la IA (e.g., "ansiedad", "alegría")
    @Column(length = 100)
    private String aiEmotion;

    // Intensidad de la emoción detectada (1-10)
    private Integer aiIntensity;

    // Resumen conciso del estado emocional generado por la IA
    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    // Palabras clave extraídas, almacenadas como un array de texto
    @ElementCollection
    @CollectionTable(name = "diary_keywords", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "keyword")
    private List<String> aiKeywords;

    // Fecha y hora de creación de la entrada
    @Column(nullable = false)
    private java.time.Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.Instant.now();
    }
}
