package com.emocional.diary.config;

import com.emocional.diary.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro personalizado para el DIARY SERVICE (CONSUMER).
 * Su única función es validar el JWT, extraer el ID del usuario (Long) 
 * y establecerlo como el Principal del contexto de seguridad.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    // Se elimina la dependencia a UserDetailsService.

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 1. Verificar si el token JWT está presente y bien formado
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token
        jwt = authHeader.substring(7);

        // 3. Validar el token
        if (jwtUtil.validateToken(jwt)) {
            try {
                // 4. EXTRAER DIRECTAMENTE EL ID (Long) del usuario desde el token
                Long userId = jwtUtil.extractUserId(jwt);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // 5. Crear objeto de autenticación con el Long userId como Principal
                    // ESTE ES EL CAMBIO CLAVE: userId (Long) como Principal.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId, // Principal: Long userId
                            null,   // Credenciales: nulas
                            Collections.emptyList() // Autoridades: vacías
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // 6. Colocar la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception e) {
                // Capturar errores durante la extracción del claim 'userId' o el casteo.
                System.err.println("Error processing JWT claims: " + e.getMessage());
                // Forzar 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return; 
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
