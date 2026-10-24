package org.sabana.proyectofinal_bd1_back.util.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("handleDatabaseIntegrityConflictException maneja DuplicateKeyException retornando 409 Conflict")
    void handleDuplicateKeyException_Retorna409Conflict() {
        when(request.getRequestURI()).thenReturn("/api/pedidos");
        DuplicateKeyException ex = new DuplicateKeyException("duplicate key value violates unique constraint");

        ResponseEntity<ErrorResponseDTO> response = handler.handleDatabaseIntegrityConflictException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Conflicto de integridad en base de datos"));
        assertEquals("/api/pedidos", response.getBody().getPath());
    }

    @Test
    @DisplayName("handleDatabaseIntegrityConflictException maneja DataIntegrityViolationException retornando 409 Conflict")
    void handleDataIntegrityViolationException_Retorna409Conflict() {
        when(request.getRequestURI()).thenReturn("/api/pedidos");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("data integrity violation");

        ResponseEntity<ErrorResponseDTO> response = handler.handleDatabaseIntegrityConflictException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
    }

}
