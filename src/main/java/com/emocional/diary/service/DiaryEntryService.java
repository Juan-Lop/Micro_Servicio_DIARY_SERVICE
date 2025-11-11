package com.emocional.diary.service;

import java.util.List;
import java.util.Optional;


import com.emocional.diary.dto.DiaryEntryRequest;
import com.emocional.diary.dto.DiaryEntryResponse;




/**
 * Interfaz para el servicio de lógica de negocio del diario.
 * Define las operaciones CRUD y el flujo de negocio (que incluye el análisis de IA).
 * Esto facilita el testing y la adhesión al Clean Code.
 */
public interface DiaryEntryService {
    
	/**
     * Crea una nueva entrada de diario.
     * Realiza validación de límite diario, llama al servicio Gemini para análisis y guarda la entrada.
     * @param userId El ID del usuario autenticado.
     * @param request El DTO con los datos de la entrada.
     * @return El DTO de respuesta con los datos de la entrada guardada y analizada.
     */
    DiaryEntryResponse createEntry(Long userId, DiaryEntryRequest request);

    /**
     * Obtiene una entrada de diario por su ID, asegurando que pertenece al usuario.
     * @param userId El ID del usuario autenticado.
     * @param entryId El ID de la entrada.
     * @return Un Optional que contiene la entrada si se encuentra y pertenece al usuario.
     */
    Optional<DiaryEntryResponse> getEntryById(Long userId, Long entryId);

    /**
     * Obtiene todas las entradas de diario para un usuario, ordenadas por fecha descendente.
     * @param userId El ID del usuario autenticado.
     * @return Una lista de DTOs de entrada de diario.
     */
    List<DiaryEntryResponse> getAllEntriesByUserId(Long userId);

    /**
     * Actualiza una entrada de diario existente.
     * Verifica que la entrada pertenece al usuario y vuelve a analizar con IA si el contenido cambió.
     * @param userId El ID del usuario autenticado.
     * @param entryId El ID de la entrada a actualizar.
     * @param request El DTO con los nuevos datos de la entrada.
     * @return El DTO de respuesta con los datos actualizados.
     */
    DiaryEntryResponse updateEntry(Long userId, Long entryId, DiaryEntryRequest request);

}
