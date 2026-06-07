package com.GesCom.repository;

import com.GesCom.model.Nomina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NominaRepository extends JpaRepository<Nomina, Long> {
    List<Nomina> findByEmpresa_EmpresaIdOrderByCreatedAtDesc(Long empresaId);
    List<Nomina> findByUsuario_UsuarioIdOrderByPeriodoInicioDesc(Long usuarioId);
    Optional<Nomina> findByNominaIdAndEmpresa_EmpresaId(Long id, Long empresaId);
}
