package com.GesCom.repository;

import com.GesCom.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long>,
        JpaSpecificationExecutor<Proveedor> {

    boolean existsByRif(String rif);
    Optional<Proveedor> findByProveedorIdAndEmpresa_EmpresaId(Long proveedorId, Long empresaId);
    List<Proveedor> findAllByEmpresa_EmpresaId(Long empresaId);
}
