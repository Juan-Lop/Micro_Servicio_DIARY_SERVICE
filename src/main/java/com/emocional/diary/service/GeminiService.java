package com.emocional.diary.service;

import com.emocional.diary.dto.gemini.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    @Value("${google.ai.gemini.url}")
    private String geminiUrl;

    public Mono<GeminiAnalysisResponse> analyzeSentiment(String diaryContent) {
        String prompt = buildSystemPrompt() + "\n\nTEXTO DEL DIARIO A ANALIZAR:\n" + diaryContent;
        log.info("Enviando análisis de sentimientos a Gemini...");
        GeminiRequest requestBody = createGeminiRequest(prompt);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/v1/models/gemini-2.5-flash:generateContent").queryParam("key", apiKey).build())
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .flatMap(this::parseContentFromResponse)
                .doOnSuccess(response -> log.info("✅ Análisis completado - Emoción: {}", response.getEmotion()))
                .doOnError(error -> log.error("❌ Error en la llamada a Gemini para análisis: {}", error.getMessage()));
    }

    public Mono<GeminiRecommendationResponse> generateRecommendation(String promptText) {
        String prompt = buildRecommendationPrompt(promptText);
        log.info("Enviando solicitud de recomendación a Gemini...");
        GeminiRequest requestBody = createGeminiRequest(prompt);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/v1/models/gemini-2.5-flash:generateContent").queryParam("key", apiKey).build())
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .flatMap(this::parseRecommendationFromResponse)
                .doOnSuccess(response -> log.info("✅ Recomendación generada con éxito."))
                .doOnError(error -> log.error("❌ Error en la llamada a Gemini para recomendación: {}", error.getMessage()));
    }

    private GeminiRequest createGeminiRequest(String prompt) {
        return GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .parts(List.of(GeminiRequest.Part.builder().text(prompt).build()))
                                .build()
                ))
                .generationConfig(GeminiRequest.GenerationConfig.builder()
                        .temperature(0.5)
                        .maxOutputTokens(2048)
                        .topP(0.8)
                        .topK(40)
                        .build())
                .build();
    }

    private String buildSystemPrompt() {
        return """
        Eres un psicólogo especializado en análisis emocional. Analiza el texto del diario y devuelve SOLO un objeto JSON con este formato exacto:
        {"emotion": "emoción_principal", "intensity": número_1_a_10, "summary": "resumen_muy_corto", "keywords": ["palabra1", "palabra2"]}
        Reglas: "emotion" una palabra, "intensity" un número, "summary" máx 15 palabras, "keywords" exactamente 2.
        IMPORTANTE: Tu respuesta debe ser SOLO el JSON, sin texto adicional, sin explicaciones, sin markdown.
        """;
    }

    private String buildRecommendationPrompt(String userContext) {
        return "Eres un experto en bienestar mental. Genera 3 recomendaciones personalizadas. " +
               "Cada una debe tener: title, description (máx 30 palabras), category ('Bienestar', 'Actividad Física', etc.), y priority ('high', 'medium', 'low'). " +
               "Devuelve la respuesta en formato JSON como un array de objetos con la clave 'recommendations'. " +
               "Ejemplo: { \"recommendations\": [ { \"title\": \"...\", \"description\": \"...\", \"category\": \"...\", \"priority\": \"...\" } ] }\n\n" +
               "Contexto del usuario: " + userContext;
    }

    private Optional<String> extractCleanJson(GeminiResponse apiResponse) {
        if (apiResponse == null) {
            log.error("❌ La respuesta de la API de Gemini es completamente nula.");
            return Optional.empty();
        }

        if (apiResponse.getCandidates() == null || apiResponse.getCandidates().isEmpty()) {
            log.error("❌ Gemini no devolvió 'candidates' en la respuesta. Finish Reason: {}", 
                Optional.ofNullable(apiResponse.getPromptFeedback()).map(f -> f.getBlockReason()).orElse("N/A"));
            return Optional.empty();
        }

        GeminiResponse.Candidate firstCandidate = apiResponse.getCandidates().get(0);
        if (firstCandidate.getContent() == null) {
            log.error("❌ El primer candidato de la respuesta no tiene 'content'. Finish Reason: {}", firstCandidate.getFinishReason());
            return Optional.empty();
        }

        if (firstCandidate.getContent().getParts() == null || firstCandidate.getContent().getParts().isEmpty()) {
            log.error("❌ El contenido del candidato no tiene 'parts'. Finish Reason: {}", firstCandidate.getFinishReason());
            return Optional.empty();
        }

        String rawText = firstCandidate.getContent().getParts().get(0).getText();
        if (rawText == null || rawText.isBlank()) {
            log.error("❌ El texto dentro de 'parts' está vacío o es nulo.");
            return Optional.empty();
        }

        log.debug("Respuesta cruda de Gemini: {}", rawText);
        String cleanJson = rawText.trim().replace("```json", "").replace("```", "").trim();
        log.debug("JSON limpio: {}", cleanJson);
        return Optional.of(cleanJson);
    }

    private Mono<GeminiAnalysisResponse> parseContentFromResponse(GeminiResponse apiResponse) {
        return extractCleanJson(apiResponse)
                .map(cleanJson -> {
                    try {
                        GeminiAnalysisResponse parsed = objectMapper.readValue(cleanJson, GeminiAnalysisResponse.class);
                        if (parsed.getEmotion() == null || parsed.getIntensity() == null) {
                            log.error("❌ JSON de análisis incompleto: {}", cleanJson);
                            return Mono.<GeminiAnalysisResponse>error(new RuntimeException("Respuesta de análisis de Gemini incompleta"));
                        }
                        return Mono.just(parsed);
                    } catch (JsonProcessingException e) {
                        log.error("❌ Error parseando JSON de análisis: {}", e.getMessage());
                        return Mono.<GeminiAnalysisResponse>error(new RuntimeException("Error procesando respuesta de análisis de Gemini", e));
                    }
                })
                .orElseGet(() -> Mono.error(new RuntimeException("No se pudo extraer contenido JSON válido de la respuesta de Gemini para análisis.")));
    }

    private Mono<GeminiRecommendationResponse> parseRecommendationFromResponse(GeminiResponse apiResponse) {
        return extractCleanJson(apiResponse)
                .map(cleanJson -> {
                    try {
                        GeminiRecommendationResponse parsed = objectMapper.readValue(cleanJson, GeminiRecommendationResponse.class);
                        if (parsed.getRecommendations() == null || parsed.getRecommendations().isEmpty()) {
                            log.error("❌ JSON de recomendación incompleto: {}", cleanJson);
                            return Mono.<GeminiRecommendationResponse>error(new RuntimeException("Respuesta de recomendación de Gemini incompleta"));
                        }
                        return Mono.just(parsed);
                    } catch (JsonProcessingException e) {
                        log.error("❌ Error parseando JSON de recomendación: {}", e.getMessage());
                        return Mono.<GeminiRecommendationResponse>error(new RuntimeException("Error procesando respuesta de recomendación de Gemini", e));
                    }
                })
                .orElseGet(() -> Mono.error(new RuntimeException("No se pudo extraer contenido JSON válido de la respuesta de Gemini para recomendación.")));
    }
}
