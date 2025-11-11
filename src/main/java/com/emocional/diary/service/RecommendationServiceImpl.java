package com.emocional.diary.service;

import com.emocional.diary.dto.RecommendationResponse;
import com.emocional.diary.dto.gemini.GeminiRecommendationResponse;
import com.emocional.diary.model.DiaryEntry;
import com.emocional.diary.repository.DiaryEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final GeminiService geminiService;
    private final DiaryEntryRepository diaryEntryRepository;

    @Override
    public List<RecommendationResponse> getRecommendations(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7); // Last 7 days

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<DiaryEntry> recentEntries = diaryEntryRepository.findByUserIdAndCreatedAtBetween(userId, startInstant, endInstant);

        String userContext = buildUserContextFromEntries(recentEntries);

        String prompt = "Eres un experto en bienestar mental. Genera 3 recomendaciones de bienestar mental personalizadas para un usuario. " +
                        "Cada recomendación debe tener un título, una descripción (máx. 30 palabras), una categoría (ej. 'Bienestar', 'Actividad Física', 'Relaciones') y una prioridad ('high', 'medium', 'low'). " +
                        "Devuelve la respuesta en formato JSON como un array de objetos con la clave 'recommendations'. " +
                        "Ejemplo: { \"recommendations\": [ { \"title\": \"...\", \"description\": \"...\", \"category\": \"...\", \"priority\": \"...\" } ] }\n\n" +
                        "Contexto del usuario: " + userContext;

        GeminiRecommendationResponse geminiResponse = geminiService.generateRecommendation(prompt).block();

        if (geminiResponse != null && geminiResponse.getRecommendations() != null) {
            return geminiResponse.getRecommendations().stream()
                    .map(rec -> RecommendationResponse.builder()
                            .id(UUID.randomUUID().toString())
                            .title(rec.getTitle())
                            .description(rec.getDescription())
                            .category(rec.getCategory())
                            .priority(rec.getPriority() != null ? rec.getPriority() : "medium") // Default to medium if not provided
                            .build())
                    .collect(Collectors.toList());
        }
        
        return List.of(); // Return empty list if Gemini fails
    }

    private String buildUserContextFromEntries(List<DiaryEntry> entries) {
        if (entries.isEmpty()) {
            return "El usuario no tiene entradas recientes. Sugiere recomendaciones generales para mejorar el bienestar mental.";
        }

        double avgMood = entries.stream().mapToInt(DiaryEntry::getUserMoodRating).average().orElse(3.0);
        double avgStress = entries.stream().mapToInt(DiaryEntry::getUserStressLevel).average().orElse(5.0);
        String mainWorry = entries.stream()
                .filter(entry -> entry.getMainWorry() != null && !entry.getMainWorry().isEmpty())
                .collect(Collectors.groupingBy(DiaryEntry::getMainWorry, Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("ninguna preocupación específica");

        StringBuilder context = new StringBuilder();
        context.append("Basado en tus entradas recientes (últimos 7 días):");
        context.append(String.format("\n- Tu ánimo promedio ha sido de %.1f/5.", avgMood));
        context.append(String.format("\n- Tu nivel de estrés promedio ha sido de %.1f/10.", avgStress));
        context.append(String.format("\n- Tu principal preocupación ha sido: %s.", mainWorry));

        // Add AI summaries if available
        String aiSummaries = entries.stream()
                .filter(entry -> entry.getAiSummary() != null && !entry.getAiSummary().isEmpty())
                .map(entry -> "(" + entry.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate() + "): " + entry.getAiSummary())
                .collect(Collectors.joining("; "));

        if (!aiSummaries.isEmpty()) {
            context.append("\n- Resúmenes de IA de tus entradas: ").append(aiSummaries);
        }

        return context.toString();
    }
}
