package com.GesCom.repository;

import com.GesCom.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByEmpresa_EmpresaId(Long empresaId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIsActiveTrue(String email);
}
