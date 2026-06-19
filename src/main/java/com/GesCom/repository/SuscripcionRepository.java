package com.GesCom.repository;

import com.GesCom.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long > {
    Optional<Suscripcion> findByEmpresa_EmpresaIdAndEstado(Long empresaId, String estado);
    List<Suscripcion> findAllByEmpresa_EmpresaIdAndEstado(Long empresaId, String estado);

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = :estado")
    long countByEstado(String estado);

    List<Suscripcion> findByEstadoAndFechaVenceBefore(String estado, LocalDate fecha);
}
