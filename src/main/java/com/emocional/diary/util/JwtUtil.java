package com.emocional.diary.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
            System.err.println("Token validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extrae el ID del usuario (Long) del claim 'userId' del token.
     */
    public Long extractUserId(String token) {
        // Asume que el Auth Service usa el claim 'userId'
        return extractClaim(token, claims -> claims.get("userId", Long.class));
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
