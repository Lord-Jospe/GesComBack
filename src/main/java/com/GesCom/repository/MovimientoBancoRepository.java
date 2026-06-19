package com.GesCom.repository;

import com.GesCom.model.MovimientoBanco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoBancoRepository extends JpaRepository<MovimientoBanco, Long> {
    List<MovimientoBanco> findByEmpresa_EmpresaIdOrderByFechaDesc(Long empresaId);
    List<MovimientoBanco> findByEmpresa_EmpresaIdAndConciliadoOrderByFechaDesc(Long empresaId, boolean conciliado);
    Optional<MovimientoBanco> findByMovimientoBancoIdAndEmpresa_EmpresaId(Long id, Long empresaId);
}
