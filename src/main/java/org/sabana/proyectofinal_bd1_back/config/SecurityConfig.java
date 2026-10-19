package org.sabana.proyectofinal_bd1_back.config;

import jakarta.servlet.http.HttpServletResponse;
import org.sabana.proyectofinal_bd1_back.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Configuración central de seguridad Spring Security V2 desacoplada de JPA y basada en JWT.
 * Implementa la matriz estricta de roles y permisos (Público/Cliente, ADMINISTRADOR, EMPLEADO).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("""
                                    {
                                        "timestamp": "%s",
                                        "status": 401,
                                        "error": "Unauthorized",
                                        "message": "Acceso no autorizado: Token JWT no proporcionado o inválido",
                                        "path": "%s"
                                    }
                                    """.formatted(LocalDateTime.now(), request.getRequestURI()));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("""
                                    {
                                        "timestamp": "%s",
                                        "status": 403,
                                        "error": "Forbidden",
                                        "message": "Acceso denegado: No tiene permisos para realizar esta operación",
                                        "path": "%s"
                                    }
                                    """.formatted(LocalDateTime.now(), request.getRequestURI()));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Rutas Públicas (Cliente sin cuenta, Login/2FA Staff, Documentación y Consola H2)
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/verificar-2fa",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservas/disponibles", "/api/reservas/correo/**").permitAll()
                        .requestMatchers(request -> "GET".equalsIgnoreCase(request.getMethod())
                                && "/api/reservas".equals(request.getServletPath())
                                && request.getParameter("correo") != null
                                && !request.getParameter("correo").isBlank()).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservas").permitAll()

                        // 2. Operaciones Exclusivas de EMPLEADO (El Administrador tiene PROHIBIDO 403 tomar pedidos o procesar pagos)
                        .requestMatchers(HttpMethod.POST, "/api/pedidos", "/api/pedidos/**").hasRole("EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/**").hasRole("EMPLEADO")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").hasRole("EMPLEADO")
                        .requestMatchers(HttpMethod.POST, "/api/pagos", "/api/pagos/**").hasRole("EMPLEADO")

                        // 3. Operaciones Exclusivas de ADMINISTRADOR
                        .requestMatchers(
                                "/api/auth/registro",
                                "/api/usuarios/**",
                                "/api/empleados/**",
                                "/api/staff/**",
                                "/api/dashboard/**"
                        ).hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**", "/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/mesas", "/api/mesas/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/mesas/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/metodos-pago/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/metodos-pago/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/metodos-pago/**").hasRole("ADMINISTRADOR")

                        // 4. Operaciones Compartidas entre ADMINISTRADOR y EMPLEADO
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.GET, "/api/mesas/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.PUT, "/api/mesas/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH, "/api/mesas/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.GET, "/api/pagos/**", "/api/facturas/**", "/api/metodos-pago/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.GET, "/api/reservas/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.PUT, "/api/reservas/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservas/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                        .requestMatchers(HttpMethod.POST, "/api/clientes", "/api/clientes/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
