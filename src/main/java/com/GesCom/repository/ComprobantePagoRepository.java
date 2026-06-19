package com.GesCom.repository;

import com.GesCom.model.ComprobantePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Long> {
    List<ComprobantePago> findByEmpresa_EmpresaIdOrderByCreatedAtDesc(Long empresaId);
    List<ComprobantePago> findByEstadoOrderByCreatedAtDesc(String estado);
    long countByEstado(String estado);
}
