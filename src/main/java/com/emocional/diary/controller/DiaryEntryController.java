package com.emocional.diary.controller;

import com.emocional.diary.dto.DiaryEntryRequest; // DTO para la creación (asumiendo que DiaryCreateRequest es ahora DiaryEntryRequest)
import com.emocional.diary.dto.DiaryEntryResponse;

import com.emocional.diary.service.DiaryEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; // Usamos el patrón de acceso directo para getCurrentUserId
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import jakarta.validation.Valid;

/**
 * Controlador REST para manejar las operaciones del diario emocional (crear y consultar entradas).
 * Requiere un JWT válido en cada petición.
 * El Controller interactúa SOLAMENTE con DTOs y delega todo el mapeo al Service.
 */
@RestController
@RequestMapping("/api/v1/diary")
@RequiredArgsConstructor
@Slf4j
public class DiaryEntryController {

    private final DiaryEntryService diaryEntryService;
    // Se elimina la inyección de DiaryEntryMapper aquí.

    /**
     * Extrae el ID del usuario (Long) del Principal de Spring Security.
     * Se asume que el JwtAuthenticationFilter fue refactorizado para inyectar el Long userId.
     * Si el Principal es un String, se intenta parsear a Long.
     * @return El ID del usuario como Long.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        
        // Asumiendo que el JWT Filter configuró el Principal como el Long ID directamente (opción ideal)
        if (authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        
        // O si el JWT Filter configuró el Principal como un String (el ID o el email)
        String principalName = authentication.getName();
        try {
            return Long.parseLong(principalName);
        } catch (NumberFormatException e) {
            log.error("❌ Error al parsear el Long userId desde el Principal: {}", principalName);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Token inválido: el ID de usuario ('sub' o 'userId' del JWT) no es un número válido.", e);
        }
    }
    
    /**
     * Endpoint para crear una nueva entrada de diario.
     * @param request DTO con el contenido del diario y el check-in del usuario.
     * @return 201 Created con la entrada analizada completa (DTO).
     */
    @PostMapping
    public ResponseEntity<DiaryEntryResponse> createDiaryEntry(
        @Valid @RequestBody DiaryEntryRequest request) { // Usamos DiaryEntryRequest para simplificar
            
        Long userId = getCurrentUserId();
        
        // El servicio lanzará IllegalStateException, ExternalServiceException, o IllegalArgumentException.
        // El @ControllerAdvice las interceptará automáticamente.
        DiaryEntryResponse response = diaryEntryService.createEntry(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
        
//        try {
//            // El servicio DEBE devolver el DTO ya mapeado.
//            DiaryEntryResponse response = diaryEntryService.createEntry(userId, request);
//            return new ResponseEntity<>(response, HttpStatus.CREATED);
//        } catch (IllegalStateException e) {
//            // 409 Conflict: Usado para la regla de "Solo una entrada por día"
//            log.warn("Conflicto de entrada (409) para usuario {}: {}", userId, e.getMessage());
//            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
//        } catch (ExternalServiceException e) {
//            // 503 Service Unavailable: Fallo en la comunicación con OpenAI/Gemini
//            log.error("Fallo de servicio externo (503) para usuario {}: {}", userId, e.getMessage());
//            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de análisis de IA no está disponible.", e);
//        } catch (IllegalArgumentException e) {
//             // 400 Bad Request: DTO inválido o datos de negocio incorrectos
//             log.error("Error de argumento (400) para usuario {}: {}", userId, e.getMessage());
//             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
    }
    
    /**
     * GET /api/v1/diary: Lista todas las entradas del usuario autenticado ordenado por fecha.
     * El servicio devuelve la lista de DTOs, el Controller la retorna directamente.
     */
    @GetMapping
    public ResponseEntity<List<DiaryEntryResponse>> getAllDiaryEntries() {
        Long userId = getCurrentUserId();
        
        // El servicio DEBE devolver la lista de DTOs ya mapeada.
        List<DiaryEntryResponse> response = diaryEntryService.getAllEntriesByUserId(userId);

        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/diary/{id}: Obtiene una entrada de diario especifica por su ID, asegurando la propiedad.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiaryEntryResponse> getDiaryEntryById(@PathVariable("id") Long entryId) {

        Long userId = getCurrentUserId();

        // El servicio DEBE devolver el DTO o lanzar la excepción NOT_FOUND.
        DiaryEntryResponse response = diaryEntryService.getEntryById(userId, entryId)
                 .orElseThrow(() -> new ResponseStatusException(
                         HttpStatus.NOT_FOUND,
                         "Entrada de diario no encontrada o no pertenece al usuario."
                 ));

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/diary/{id}: Actualiza una entrada de diario existente.
     * Vuelve a analizar con IA si el contenido cambió.
     * @param entryId El ID de la entrada a actualizar.
     * @param request DTO con los nuevos datos de la entrada.
     * @return 200 OK con la entrada actualizada (DTO).
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiaryEntryResponse> updateDiaryEntry(
        @PathVariable("id") Long entryId,
        @Valid @RequestBody DiaryEntryRequest request) {

        Long userId = getCurrentUserId();

        // El servicio lanzará excepciones que el @ControllerAdvice interceptará.
        DiaryEntryResponse response = diaryEntryService.updateEntry(userId, entryId, request);
        return ResponseEntity.ok(response);
    }
}
