package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.AuthResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.LoginRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.Verificar2FARequestDTO;

/**
 * Servicio para autenticación, registro y verificación en dos pasos (2FA).
 */
public interface AuthService {

    AuthResponseDTO registrar(UsuarioRequestDTO requestDTO);

    AuthResponseDTO login(LoginRequestDTO requestDTO);

    AuthResponseDTO verificar2FA(Verificar2FARequestDTO requestDTO);

}
