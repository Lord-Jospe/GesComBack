package com.GesCom.repository;

import com.GesCom.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long > {
    Optional<Suscripcion> findByEmpresa_EmpresaIdAndEstado(Long empresaId, String estado);

    // Para el vencimiento automático
    List<Suscripcion> findByEstadoAndFechaVenceBefore(String estado, LocalDate fecha);
}
