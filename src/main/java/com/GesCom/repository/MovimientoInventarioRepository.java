package com.GesCom.repository;

import com.GesCom.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByProducto_ProductoIdOrderByCreatedAtDesc(Long productoId);
    List<MovimientoInventario> findByProducto_Empresa_EmpresaIdOrderByCreatedAtDesc(Long empresaId);
}
