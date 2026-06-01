package com.GesCom.repository;

import com.GesCom.enums.EstadoTransaccion;
import com.GesCom.enums.TipoTransaccion;
import com.GesCom.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long>,
        JpaSpecificationExecutor<Transaccion> {

    Optional<Transaccion> findByTransaccionIdAndEmpresa_EmpresaId(Long id, Long empresaId);

    List<Transaccion> findByEmpresa_EmpresaIdOrderByFechaDesc(Long empresaId);

    List<Transaccion> findByEmpresa_EmpresaIdAndTipoOrderByFechaDesc(Long empresaId, TipoTransaccion tipo);

    List<Transaccion> findByEmpresa_EmpresaIdAndEstadoOrderByFechaAsc(Long empresaId, EstadoTransaccion estado);

    List<Transaccion> findByEmpresa_EmpresaIdAndTipoAndEstadoOrderByFechaAsc(
            Long empresaId, TipoTransaccion tipo, EstadoTransaccion estado);

    List<Transaccion> findByEmpresa_EmpresaIdAndCliente_ClienteIdAndEstado(
            Long empresaId, Long clienteId, EstadoTransaccion estado);

    List<Transaccion> findByEmpresa_EmpresaIdAndProveedor_ProveedorIdAndEstado(
            Long empresaId, Long proveedorId, EstadoTransaccion estado);

    boolean existsByEmpresa_EmpresaIdAndNumeroFactura(Long empresaId, String numeroFactura);
}
