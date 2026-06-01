package com.GesCom.repository;

import com.GesCom.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsadoFalse(String token);

    // Limpiar tokens vencidos — se puede llamar desde un @Scheduled
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiraEn < :ahora")
    void eliminarTokensVencidos(LocalDateTime ahora);
}
