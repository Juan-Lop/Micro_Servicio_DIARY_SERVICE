package com.emocional.diary.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.emocional.diary.dto.ErrorResponse;


/**
 * Clase centralizada para el manejo global de excepciones en el DIARY-SERVICE.
 * Mapea las excepciones del servicio a respuestas HTTP estandarizadas (ErrorResponse).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja la IllegalStateException.
     * Esto se utiliza para errores de reglas de negocio, como la doble entrada de diario (409 Conflict).
     *
     * @param ex La excepción lanzada: "Solo se permite una entrada de diario por día."
     * @return ResponseEntity con el DTO ErrorResponse y estado HTTP 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), status);
        
        // Logueamos la advertencia, pero devolvemos una respuesta amigable al cliente
        System.err.println("Conflicto de Regla de Negocio: " + ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, status);
    }
    /**
     * Maneja la ResponseStatusException (que lanza el código 404)
     * Esto se usa cuando un recurso no se encuentra (ej. Entrada de diario no encontrada).
     * * @param ex La excepción lanzada: 404 NOT_FOUND "Entrada de diario no encontrada..."
     * @return ResponseEntity con el DTO ErrorResponse y estado HTTP 404 Not Found.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        
        // Usamos el mensaje del ResponseStatusException
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        
        ErrorResponse errorResponse = new ErrorResponse(message, status);
        
        System.err.println("Error HTTP " + status.value() + ": " + message);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    

    /**
     * Manejador de excepciones genérico (catch-all) para cualquier excepción no prevista.
     * (Internal Server Error - 500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        ErrorResponse errorResponse = new ErrorResponse(
                "Ocurrió un error inesperado en el servidor.",
                status
        );
        // Es fundamental loguear la traza completa de una excepción 500
        ex.printStackTrace();
        return new ResponseEntity<>(errorResponse, status);
    }
}
