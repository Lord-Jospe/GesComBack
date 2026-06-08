package com.GesCom.repository;

import com.GesCom.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>,
        JpaSpecificationExecutor<Cliente> {

    boolean existsByRifCedula(String rifCedula);
    Optional<Cliente> findByClienteIdAndEmpresa_EmpresaId(Long clienteId, Long empresaId);
    List<Cliente> findAllByEmpresa_EmpresaId(Long empresaId);
    Page<Cliente> findByEmpresa_EmpresaId(Long empresaId, Pageable pageable);
}
