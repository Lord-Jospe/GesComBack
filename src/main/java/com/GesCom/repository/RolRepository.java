package com.GesCom.repository;

import com.GesCom.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findById(Long rolId);

    boolean existsById(Long rolId);

    Optional<Rol> findByRol(String name);
}
