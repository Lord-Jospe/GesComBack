package com.GesCom.service;

public interface PasswordResetService {

    /**
     * Genera un token de recuperación y envía el enlace al correo del usuario.
     */
    void solicitarRecuperacion(String email);

    /**
     * Valida el token y cambia la contraseña del usuario.
     */
    void resetearContrasena(String token, String nuevaContrasena);
}
