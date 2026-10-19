package org.sabana.proyectofinal_bd1_back.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.sabana.proyectofinal_bd1_back.model.entity.Administrador;
import org.sabana.proyectofinal_bd1_back.model.entity.Staff;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Proveedor de utilidades para generación, firma HMAC-SHA256 y validación de tokens JWT.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:SavanaDataCoffeeSuperSecretSecurityKey2026Secure256BitsLongEnough}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Genera un token JWT firmado para el miembro de Staff con claims de correo, rol (ADMINISTRADOR o EMPLEADO) e idUsuario.
     */
    public String generateToken(Staff staff) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String rol = staff.getRol() != null && !staff.getRol().isBlank()
                ? staff.getRol().trim().toUpperCase()
                : (staff instanceof Administrador ? "ADMINISTRADOR" : "EMPLEADO");

        return Jwts.builder()
                .subject(staff.getCorreo())
                .claim("idUsuario", staff.getIdUsuario())
                .claim("nombres", staff.getNombres())
                .claim("apellidos", staff.getApellidos())
                .claim("nombre", staff.getNombreCompleto())
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Extrae el correo (subject) del token JWT.
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el rol operativo del token JWT.
     */
    public String getRolFromToken(String token) {
        return getClaims(token).get("rol", String.class);
    }

    /**
     * Extrae el idUsuario del token JWT.
     */
    public Long getIdUsuarioFromToken(String token) {
        Number id = getClaims(token).get("idUsuario", Number.class);
        return id != null ? id.longValue() : null;
    }

    /**
     * Valida la firma y tiempo de vigencia del token JWT.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT no válido: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
