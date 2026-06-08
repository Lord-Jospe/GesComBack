package com.GesCom.repository;

import com.GesCom.model.TasaBcv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TasaBcvRepository extends JpaRepository<TasaBcv, Long> {

    Optional<TasaBcv> findTopByEmpresa_EmpresaIdOrderByFechaHoraDesc(Long empresaId);

    Optional<TasaBcv> findTopByEmpresa_EmpresaIdAndFechaHoraLessThanEqualOrderByFechaHoraDesc(
            Long empresaId, LocalDateTime fechaHora);

    List<TasaBcv> findByEmpresa_EmpresaIdOrderByFechaHoraDesc(Long empresaId);

    // Para transacciones: obtener la tasa más reciente hasta una fecha dada (ignora hora)
    Optional<TasaBcv> findTopByEmpresa_EmpresaIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long empresaId, LocalDateTime inicioDia, LocalDateTime finDia);
}
