package com.emocional.diary.dto;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO estandarizado para todas las respuestas de error de la API del diario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String message;
    private int status;
    private String error;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.timestamp = LocalDateTime.now();
    }
}

