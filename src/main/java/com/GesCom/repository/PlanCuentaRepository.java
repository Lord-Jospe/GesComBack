package com.GesCom.repository;

import com.GesCom.model.PlanCuenta;
import com.GesCom.enums.TipoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanCuentaRepository extends JpaRepository<PlanCuenta, Long> {
    List<PlanCuenta> findByEmpresa_EmpresaIdAndIsActiveTrueOrderByCodigo(Long empresaId);
    List<PlanCuenta> findByEmpresa_EmpresaId(Long empresaId);
    Optional<PlanCuenta> findByCuentaIdAndEmpresa_EmpresaId(Long id, Long empresaId);
    List<PlanCuenta> findByEmpresa_EmpresaIdAndTipoCuenta(Long empresaId, TipoCuenta tipoCuenta);
    boolean existsByEmpresa_EmpresaIdAndCodigo(Long empresaId, String codigo);
}
