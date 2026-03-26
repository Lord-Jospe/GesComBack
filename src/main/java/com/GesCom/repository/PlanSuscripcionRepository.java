package com.GesCom.repository;

import com.GesCom.model.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Integer> {

    Optional<PlanSuscripcion> findByNombre(String nombre);
}
