package com.GesCom.repository;

import com.GesCom.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByEmpresa_EmpresaId(Long empresaId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIsActiveTrue(String email);

    @Query("SELECT u FROM Usuario u WHERE u.empresa.empresaId = :empresaId " +
           "AND (:soloActivos = false OR u.isActive = true) " +
           "AND (:rolId IS NULL OR u.rol.rolId = :rolId) " +
           "AND (:busqueda IS NULL OR LOWER(CONCAT(u.primerNombre,' ',u.primerApellido,' ',u.email)) LIKE LOWER(CONCAT('%',:busqueda,'%')))")
    Page<Usuario> findPaginado(Long empresaId, Boolean soloActivos, Integer rolId, String busqueda, Pageable pageable);
}
