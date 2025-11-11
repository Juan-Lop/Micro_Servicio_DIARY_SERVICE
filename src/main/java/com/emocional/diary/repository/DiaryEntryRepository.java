package com.emocional.diary.repository;

import com.emocional.diary.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad DiaryEntry.
 */
public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    /**
     * Busca todas las entradas de diario de un usuario específico.
     * @param userId El ID del usuario propietario.
     * @return Lista de entradas de diario.
     */
    List<DiaryEntry> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    
    /**
     * **METODO DE SEGURIDAD CLAVE**
     * 
     * Busca una entrada po su ID y se asegura de que pertenezca al ID de usuario dado.
     * @param id ID de la entrada
     * @param userId ID del usuario propietario.
     * @return Optional con la entrada si ambos condiciones se cumplen
     */
    
    Optional<DiaryEntry> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Busca la última entrada de diario para un usuario en una fecha específica (solo día).
     * Usado para validar el límite de una entrada por día.
     * @param userId ID del usuario.
     * @param startOfDay El inicio del día (e.g., 2025-10-19 00:00:00).
     * @param endOfDay El final del día (e.g., 2025-10-19 23:59:59.999...).
     * @return Un Optional que contiene la entrada si existe.
     */
    
    @Query("SELECT de FROM DiaryEntry de WHERE de.userId = :userId AND de.createdAt >= :startOfDay AND de.createdAt <= :endOfDay")
    Optional<DiaryEntry> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                                  @Param("startOfDay") java.time.Instant startOfDay, 
                                                  @Param("endOfDay") java.time.Instant endOfDay);
    
    @Query("SELECT e.mainWorry FROM DiaryEntry e WHERE e.userId = :userId AND e.mainWorry IS NOT NULL AND TRIM(e.mainWorry) <> '' AND e.mainWorry <> 'Ninguna' GROUP BY e.mainWorry ORDER BY COUNT(e.mainWorry) DESC")
    List<String> findMostFrequentMainWorry(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    List<DiaryEntry> findByUserIdAndCreatedAtBetween(Long userId, java.time.Instant start, java.time.Instant end);

}
