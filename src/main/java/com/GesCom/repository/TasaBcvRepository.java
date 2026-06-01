package com.GesCom.repository;

import com.GesCom.model.TasaBcv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TasaBcvRepository extends JpaRepository<TasaBcv, Long> {

    Optional<TasaBcv> findByEmpresa_EmpresaIdAndFecha(Long empresaId, LocalDate fecha);

    List<TasaBcv> findByEmpresa_EmpresaIdOrderByFechaDesc(Long empresaId);

    Optional<TasaBcv> findTopByEmpresa_EmpresaIdOrderByFechaDesc(Long empresaId);
}
