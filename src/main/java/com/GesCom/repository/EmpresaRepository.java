package com.GesCom.repository;

import com.GesCom.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByRif(String rif);
    Optional<Empresa> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    boolean existsByRif(String rif);
}
