package com.emocional.diary.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.function.Function;

/**
 * Utilidad JWT para el DIARY SERVICE (CONSUMER).
 * Se encarga de VALIDAR el token y de extraer el ID del usuario (Long).
 * Utiliza la misma clave secreta que el Auth Service.
 */
@Component
@Slf4j
public class JwtUtil {

    // Clave secreta COMPARTIDA con el Auth Service
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    /**
     * Valida si la firma del token es correcta y si no ha expirado.
     * Si falla, lanzará una excepción (que se captura aquí o en el filtro).
     */
    public boolean validateToken(String token) {
        try {
            // Intenta parsear los claims. Si el token es inválido o expirado, lanza una excepción.
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            // Imprimimos el error solo para depuración; el filtro de seguridad manejará el 401.
            log.error("❌ Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el ID del usuario (Long) del token JWT.
     * Intenta múltiples estrategias para manejar diferentes formatos de token.
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);

            // Estrategia 1: Buscar claim "userId" como Long
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                log.info("✅ JWT Claim 'userId': {} (tipo: {})", userIdObj, userIdObj.getClass().getSimpleName());
                return convertToLong(userIdObj);
            }

            // Estrategia 2: Buscar claim "sub" (subject)
            String subject = claims.getSubject();
            if (subject != null && !subject.isEmpty()) {
                log.info("✅ JWT Claim 'sub': {}", subject);
                return convertToLong(subject);
            }

            // Estrategia 3: Buscar claim "id"
            Object idObj = claims.get("id");
            if (idObj != null) {
                log.info("✅ JWT Claim 'id': {} (tipo: {})", idObj, idObj.getClass().getSimpleName());
                return convertToLong(idObj);
            }

            log.error("❌ No se encontró userId en ningún claim conocido. Claims disponibles: {}", claims.keySet());
            return null;

        } catch (Exception e) {
            log.error("❌ Error extrayendo userId del token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convierte un objeto a Long, manejando diferentes tipos.
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.error("❌ No se pudo convertir '{}' a Long", value);
                return null;
            }
        }

        log.error("❌ Tipo no soportado para userId: {}", value.getClass().getSimpleName());
        return null;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
