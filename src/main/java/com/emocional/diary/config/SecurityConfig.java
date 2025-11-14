package com.emocional.diary.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad para el DIARY SERVICE (CONSUMER).
 * Se enfoca en validar el JWT y usar una política de sesión Stateless.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${cors.allowed.origins:http://localhost:5174,http://localhost:3000,http://localhost:8081}")
    private String allowedOrigins;

    // Se elimina la dependencia a UserDetailsService, ya que el Auth Service se encarga de eso.

    /**
     * Define la cadena de filtros de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Permite acceso sin autenticación para endpoints de salud, error y Swagger UI.
                        .requestMatchers("/actuator/health", "/error",
                                         "/v3/api-docs/**",
                                         "/swagger-ui/**",
                                         "/swagger-ui.html").permitAll()
                        // Todas las demás rutas, incluyendo las de /api/v1/checkin, requieren autenticación JWT.
                        .anyRequest().authenticated()
                )
                // Usar política de sesión STATELESS para JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Añadir el filtro de JWT antes del filtro estándar de autenticación
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    /**
     * Configuración detallada de CORS para el microservicio.
     * Los orígenes permitidos se configuran mediante la variable de entorno CORS_ALLOWED_ORIGINS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Define orígenes permitidos desde variable de entorno
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Define métodos permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Define encabezados permitidos (CRUCIAL para 'Authorization')
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Permite enviar cookies/encabezados de autenticación
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
