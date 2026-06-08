package com.GesCom.repository;

import com.GesCom.model.AsientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsientoContableRepository extends JpaRepository<AsientoContable, Long> {
    List<AsientoContable> findByEmpresa_EmpresaIdOrderByFechaAscNumeroAsientoAsc(Long empresaId);
    List<AsientoContable> findByEmpresa_EmpresaIdAndFechaBetweenOrderByFechaAscNumeroAsientoAsc(
            Long empresaId, LocalDate desde, LocalDate hasta);
    Optional<AsientoContable> findByAsientoIdAndEmpresa_EmpresaId(Long id, Long empresaId);

    @Query("SELECT COALESCE(MAX(a.numeroAsiento), 0) FROM AsientoContable a WHERE a.empresa.empresaId = :empresaId")
    Integer maxNumeroAsiento(Long empresaId);
}
