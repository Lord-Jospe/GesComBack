package com.GesCom.repository;

import com.GesCom.model.MovimientoInventario;
import com.GesCom.enums.TipoMovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByProducto_ProductoIdOrderByCreatedAtDesc(Long productoId);

    Page<MovimientoInventario> findByProducto_Empresa_EmpresaIdOrderByCreatedAtDesc(Long empresaId, Pageable pageable);
    Page<MovimientoInventario> findByProducto_Empresa_EmpresaIdAndTipoOrderByCreatedAtDesc(Long empresaId, TipoMovimientoInventario tipo, Pageable pageable);
    Page<MovimientoInventario> findByProducto_Empresa_EmpresaIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long empresaId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
    Page<MovimientoInventario> findByProducto_Empresa_EmpresaIdAndTipoAndCreatedAtBetweenOrderByCreatedAtDesc(Long empresaId, TipoMovimientoInventario tipo, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
