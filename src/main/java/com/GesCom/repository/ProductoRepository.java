package com.GesCom.repository;

import com.GesCom.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByEmpresa_EmpresaId(Long empresaId);
    Page<Producto> findByEmpresa_EmpresaId(Long empresaId, Pageable pageable);
    Optional<Producto> findByProductoIdAndEmpresa_EmpresaId(Long id, Long empresaId);

    @Query("SELECT p FROM Producto p WHERE p.empresa.empresaId = :empresaId AND p.stockActual <= p.stockMinimo AND p.isActive = true")
    List<Producto> findStockCritico(Long empresaId);

    @Query("SELECT COALESCE(SUM(p.stockActual * p.costoUnitario), 0) FROM Producto p WHERE p.empresa.empresaId = :empresaId AND p.isActive = true")
    java.math.BigDecimal valorTotalInventario(Long empresaId);
}
