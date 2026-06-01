package com.GesCom.service.Impl;

import com.GesCom.model.PasswordResetToken;
import com.GesCom.model.Usuario;
import com.GesCom.repository.PasswordResetTokenRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.service.PasswordResetService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    @Transactional
    public void solicitarRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con ese correo"));

        if (!usuario.isActive()) {
            throw new IllegalStateException("El usuario está desactivado. Contacte al administrador.");
        }

        // Invalidar tokens anteriores del usuario
        tokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getUsuarioId().equals(usuario.getUsuarioId()) && !t.isUsado())
                .forEach(t -> t.setUsado(true));

        // Generar token con expiración de 30 minutos
        String codigo = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .usuario(usuario)
                .token(codigo)
                .expiraEn(LocalDateTime.now().plusMinutes(30))
                .usado(false)
                .build();
        tokenRepository.save(resetToken);

        // Enviar correo
        String enlace = "http://localhost:5173/reset-password?token=" + codigo;
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(mailFrom);
            mensaje.setTo(usuario.getEmail());
            mensaje.setSubject("GesCom — Recuperación de contraseña");
            mensaje.setText("Hola " + usuario.getPrimerNombre() + ",\n\n"
                    + "Has solicitado restablecer tu contraseña en GesCom.\n"
                    + "Usa el siguiente enlace (válido por 30 minutos):\n\n"
                    + enlace + "\n\n"
                    + "Si no solicitaste este cambio, ignora este mensaje.\n\n"
                    + "— Equipo GesCom");
            mailSender.send(mensaje);
            log.info("Correo de recuperación enviado a {}", usuario.getEmail());
        } catch (Exception e) {
            // En desarrollo, si no hay SMTP configurado, mostramos el enlace en logs
            log.warn("No se pudo enviar el correo. Enlace de recuperación: {}", enlace);
            log.warn("Error: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetearContrasena(String token, String nuevaContrasena) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsadoFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o ya usado"));

        if (resetToken.getExpiraEn().isBefore(LocalDateTime.now())) {
            resetToken.setUsado(true);
            tokenRepository.save(resetToken);
            throw new IllegalArgumentException("El token ha expirado. Solicite uno nuevo.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);

        // Marcar token como usado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        log.info("Contraseña actualizada para el usuario {}", usuario.getEmail());
    }
}
