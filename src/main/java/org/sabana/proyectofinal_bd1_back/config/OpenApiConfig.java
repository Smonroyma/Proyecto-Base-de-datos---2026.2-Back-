package org.sabana.proyectofinal_bd1_back.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de OpenAPI / Swagger para documentar y habilitar autenticación JWT.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Savana Data Coffee - API Backend",
                version = "1.0.0",
                description = "Plataforma de servicios backend para gestión de cafetería, comandas y 2FA/OTP vía correo HTML.",
                contact = @Contact(name = "Savana Data Coffee Team", email = "soporte@savanacoffee.com")
        ),
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Ingresa el token JWT de acceso para interactuar con los endpoints protegidos."
)
public class OpenApiConfig {
}
