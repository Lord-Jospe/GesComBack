package com.GesCom.service.Impl;

import com.GesCom.dto.request.*;
import com.GesCom.dto.response.*;
import com.GesCom.enums.EstadoTransaccion;
import com.GesCom.enums.TipoTransaccion;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.service.TransaccionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;
    private final TasaBcvRepository tasaBcvRepository;
    private final PagoRepository pagoRepository;

    private static final BigDecimal IGTF_RATE = new BigDecimal("3.00");

    @Override
    @Transactional
    public TransaccionResponse crear(CrearTransaccionRequest request, Long empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Cliente cliente = resolverCliente(request, empresaId);
        Proveedor proveedor = resolverProveedor(request, empresaId);

        // ─── 1. Obtener tasa BCV del día ───────────────────────────
        BigDecimal tasaBcv = tasaBcvRepository
                .findByEmpresa_EmpresaIdAndFecha(empresaId, request.fecha())
                .map(TasaBcv::getTasa)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay tasa BCV registrada para la fecha " + request.fecha() +
                        ". Regístrela en /api/exchange-rate"));

        // ─── 2. Calcular líneas ────────────────────────────────────
        BigDecimal subtotal = BigDecimal.ZERO;
        List<TransaccionLinea> lineas = new ArrayList<>();

        for (AgregarLineaRequest lr : request.lineas()) {
            BigDecimal precio = lr.precioUnitario();
            BigDecimal cantidad = lr.cantidad();
            BigDecimal brutoLinea = precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

            // Descuento por línea
            BigDecimal descMonto = BigDecimal.ZERO;
            BigDecimal descPct = lr.descuentoPorcentaje();
            if (descPct != null && descPct.compareTo(BigDecimal.ZERO) > 0) {
                descMonto = brutoLinea.multiply(descPct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else if (lr.descuentoMonto() != null) {
                descMonto = lr.descuentoMonto();
            }

            BigDecimal subLinea = brutoLinea.subtract(descMonto).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(subLinea);

            lineas.add(TransaccionLinea.builder()
                    .productoId(lr.productoId())
                    .descripcion(lr.descripcion())
                    .cantidad(cantidad)
                    .precioUnitario(precio)
                    .descuentoPorcentaje(descPct)
                    .descuentoMonto(descMonto)
                    .subtotalLinea(subLinea)
                    .build());
        }

        // ─── 3. Descuento global ───────────────────────────────────
        BigDecimal descGlobalMonto = BigDecimal.ZERO;
        BigDecimal descGlobalPct = request.descuentoGlobalPorcentaje();
        if (descGlobalPct != null && descGlobalPct.compareTo(BigDecimal.ZERO) > 0) {
            descGlobalMonto = subtotal.multiply(descGlobalPct)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if (request.descuentoGlobalMonto() != null) {
            descGlobalMonto = request.descuentoGlobalMonto();
        }

        BigDecimal baseImponible = subtotal.subtract(descGlobalMonto)
                .setScale(2, RoundingMode.HALF_UP);

        // ─── 4. IVA ────────────────────────────────────────────────
        BigDecimal ivaMonto = BigDecimal.ZERO;
        BigDecimal ivaPct = empresa.getIvaPorcentaje() != null
                ? empresa.getIvaPorcentaje()
                : new BigDecimal("16.00");

        if (empresa.isIvaActivo()) {
            ivaMonto = baseImponible.multiply(ivaPct)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        // ─── 5. IGTF (solo si está activo Y la moneda es USD) ─────
        boolean igtfAplica = false;
        BigDecimal igtfMonto = BigDecimal.ZERO;

        if (empresa.isIgtfActivo() && "USD".equals(request.moneda())) {
            igtfAplica = true;
            igtfMonto = baseImponible.add(ivaMonto)
                    .multiply(IGTF_RATE)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        // ─── 6. Total ──────────────────────────────────────────────
        BigDecimal total = baseImponible.add(ivaMonto).add(igtfMonto)
                .setScale(2, RoundingMode.HALF_UP);

        // ─── 7. Conversión a la otra moneda ────────────────────────
        BigDecimal totalUsd, totalVes;
        if ("USD".equals(request.moneda())) {
            totalUsd = total;
            totalVes = total.multiply(tasaBcv).setScale(2, RoundingMode.HALF_UP);
        } else {
            totalVes = total;
            totalUsd = total.divide(tasaBcv, 2, RoundingMode.HALF_UP);
        }

        // ─── 8. Número de factura (solo INGRESO) ───────────────────
        String numeroFactura = null;
        if (request.tipo() == TipoTransaccion.INGRESO) {
            numeroFactura = generarNumeroFactura(empresa);
        }

        // ─── 9. Guardar ────────────────────────────────────────────
        Transaccion transaccion = Transaccion.builder()
                .empresa(empresa)
                .tipo(request.tipo())
                .cliente(cliente)
                .proveedor(proveedor)
                .fecha(request.fecha())
                .moneda(request.moneda())
                .tasaBcvUsada(tasaBcv)
                .subtotal(subtotal)
                .ivaPorcentaje(empresa.isIvaActivo() ? ivaPct : BigDecimal.ZERO)
                .ivaMonto(ivaMonto)
                .igtfAplica(igtfAplica)
                .igtfMonto(igtfMonto)
                .descuentoGlobalPorcentaje(descGlobalPct)
                .descuentoGlobalMonto(descGlobalMonto)
                .total(total)
                .totalUsd(totalUsd)
                .totalVes(totalVes)
                .metodoPago(request.metodoPago())
                .estado(EstadoTransaccion.PENDIENTE)
                .notas(request.notas())
                .numeroFactura(numeroFactura)
                .build();

        // Asignar líneas
        for (TransaccionLinea linea : lineas) {
            linea.setTransaccion(transaccion);
        }
        transaccion.setLineas(lineas);

        transaccionRepository.save(transaccion);

        log.info("Transacción creada: id={}, tipo={}, total={} {}, factura={}",
                transaccion.getTransaccionId(), request.tipo(), total,
                request.moneda(), numeroFactura);

        return toResponse(transaccion);
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionResponse obtenerPorId(Long id, Long empresaId) {
        return toResponse(buscar(id, empresaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> listar(Long empresaId, FiltroTransaccionRequest filtro) {
        // Si no hay filtros, devolver todas ordenadas por fecha descendente
        if (filtro == null || filtro.tipo() == null && filtro.estado() == null
                && filtro.clienteId() == null && filtro.proveedorId() == null) {
            return transaccionRepository
                    .findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId)
                    .stream().map(this::toResponse).toList();
        }

        if (filtro.tipo() != null && filtro.estado() != null) {
            return transaccionRepository
                    .findByEmpresa_EmpresaIdAndTipoAndEstadoOrderByFechaAsc(
                            empresaId, filtro.tipo(), filtro.estado())
                    .stream().map(this::toResponse).toList();
        }

        if (filtro.tipo() != null) {
            return transaccionRepository
                    .findByEmpresa_EmpresaIdAndTipoOrderByFechaDesc(empresaId, filtro.tipo())
                    .stream().map(this::toResponse).toList();
        }

        if (filtro.estado() != null) {
            return transaccionRepository
                    .findByEmpresa_EmpresaIdAndEstadoOrderByFechaAsc(empresaId, filtro.estado())
                    .stream().map(this::toResponse).toList();
        }

        return transaccionRepository
                .findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public TransaccionResponse editar(Long id, EditarTransaccionRequest request, Long empresaId) {
        Transaccion t = buscar(id, empresaId);

        if (t.getEstado() == EstadoTransaccion.ANULADA) {
            throw new IllegalStateException("No se puede editar una transacción anulada");
        }
        if (t.getEstado() == EstadoTransaccion.PAGADA && tienePagosRegistrados(t)) {
            throw new IllegalStateException("No se puede editar una transacción con pagos registrados");
        }

        if (request.fecha() != null) t.setFecha(request.fecha());
        if (request.metodoPago() != null) t.setMetodoPago(request.metodoPago());
        if (request.descuentoGlobalPorcentaje() != null) t.setDescuentoGlobalPorcentaje(request.descuentoGlobalPorcentaje());
        if (request.descuentoGlobalMonto() != null) t.setDescuentoGlobalMonto(request.descuentoGlobalMonto());
        if (request.notas() != null) t.setNotas(request.notas());

        return toResponse(transaccionRepository.save(t));
    }

    @Override
    @Transactional
    public void anular(Long id, String motivo, Long empresaId) {
        Transaccion t = buscar(id, empresaId);

        if (t.getEstado() == EstadoTransaccion.ANULADA) {
            throw new IllegalStateException("La transacción ya está anulada");
        }

        t.setEstado(EstadoTransaccion.ANULADA);
        t.setMotivoAnulacion(motivo);
        transaccionRepository.save(t);

        log.info("Transacción {} anulada. Motivo: {}", id, motivo);
    }

    @Override
    @Transactional
    public PagoResponse registrarPago(Long transaccionId, RegistrarPagoRequest request, Long empresaId) {
        Transaccion t = buscar(transaccionId, empresaId);

        if (t.getEstado() == EstadoTransaccion.ANULADA) {
            throw new IllegalStateException("No se puede pagar una transacción anulada");
        }
        if (t.getEstado() == EstadoTransaccion.PAGADA) {
            throw new IllegalStateException("Esta transacción ya está totalmente pagada");
        }

        // Calcular saldo pendiente
        BigDecimal totalPagado = pagoRepository.sumMontoByTransaccionId(transaccionId);
        BigDecimal pendiente = t.getTotal().subtract(totalPagado);

        if (request.monto().compareTo(pendiente) > 0) {
            throw new IllegalArgumentException(
                    "El monto del pago ($" + request.monto() + ") excede el saldo pendiente ($" + pendiente + ")");
        }

        Pago pago = pagoRepository.save(Pago.builder()
                .transaccion(t)
                .monto(request.monto())
                .fecha(request.fecha())
                .metodoPago(request.metodoPago())
                .referencia(request.referencia())
                .notas(request.notas())
                .build());

        // Actualizar estado de la transacción
        BigDecimal nuevoTotalPagado = totalPagado.add(request.monto());
        if (nuevoTotalPagado.compareTo(t.getTotal()) >= 0) {
            t.setEstado(EstadoTransaccion.PAGADA);
        } else {
            t.setEstado(EstadoTransaccion.PARCIAL);
        }
        transaccionRepository.save(t);

        log.info("Pago registrado: transacción={}, monto={}, estado={}",
                transaccionId, request.monto(), t.getEstado());

        return toPagoResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> historialPagos(Long transaccionId, Long empresaId) {
        buscar(transaccionId, empresaId); // verifica que pertenece a la empresa
        return pagoRepository.findByTransaccion_TransaccionIdOrderByFechaDesc(transaccionId)
                .stream().map(this::toPagoResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> cuentasPorCobrar(Long empresaId) {
        return transaccionRepository
                .findByEmpresa_EmpresaIdAndTipoAndEstadoOrderByFechaAsc(
                        empresaId, TipoTransaccion.INGRESO, EstadoTransaccion.PENDIENTE)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> cuentasPorPagar(Long empresaId) {
        return transaccionRepository
                .findByEmpresa_EmpresaIdAndTipoAndEstadoOrderByFechaAsc(
                        empresaId, TipoTransaccion.EGRESO, EstadoTransaccion.PENDIENTE)
                .stream().map(this::toResponse).toList();
    }

    // ─── Métodos privados ──────────────────────────────────────────

    private Cliente resolverCliente(CrearTransaccionRequest request, Long empresaId) {
        if (request.tipo() == TipoTransaccion.INGRESO) {
            if (request.clienteId() == null) {
                throw new IllegalArgumentException("INGRESO requiere un cliente");
            }
            return clienteRepository.findByClienteIdAndEmpresa_EmpresaId(request.clienteId(), empresaId)
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        }
        return null;
    }

    private Proveedor resolverProveedor(CrearTransaccionRequest request, Long empresaId) {
        if (request.tipo() == TipoTransaccion.EGRESO) {
            if (request.proveedorId() == null) {
                throw new IllegalArgumentException("EGRESO requiere un proveedor");
            }
            return proveedorRepository.findByProveedorIdAndEmpresa_EmpresaId(request.proveedorId(), empresaId)
                    .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        }
        return null;
    }

    private String generarNumeroFactura(Empresa empresa) {
        String prefijo = empresa.getFacturaPrefijo() != null ? empresa.getFacturaPrefijo() : "";
        int siguiente = empresa.getFacturaSiguienteNumero() != null
                ? empresa.getFacturaSiguienteNumero() : 1;

        String numero = prefijo + String.format("%05d", siguiente);

        // Incrementar para la próxima
        empresa.setFacturaSiguienteNumero(siguiente + 1);
        empresaRepository.save(empresa);

        return numero;
    }

    private boolean tienePagosRegistrados(Transaccion t) {
        return pagoRepository.existsByTransaccion_TransaccionId(t.getTransaccionId());
    }

    private PagoResponse toPagoResponse(Pago p) {
        return new PagoResponse(
                p.getPagoId(),
                p.getTransaccion().getTransaccionId(),
                p.getMonto(),
                p.getFecha(),
                p.getMetodoPago().name(),
                p.getReferencia(),
                p.getNotas(),
                p.getCreatedAt()
        );
    }

    private Transaccion buscar(Long id, Long empresaId) {
        return transaccionRepository
                .findByTransaccionIdAndEmpresa_EmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));
    }

    private TransaccionResponse toResponse(Transaccion t) {
        List<TransaccionLineaResponse> lineasResp = t.getLineas() != null
                ? t.getLineas().stream().map(l -> new TransaccionLineaResponse(
                        l.getLineaId(),
                        l.getProductoId(),
                        l.getDescripcion(),
                        l.getCantidad(),
                        l.getPrecioUnitario(),
                        l.getDescuentoPorcentaje(),
                        l.getDescuentoMonto(),
                        l.getSubtotalLinea()))
                .toList()
                : List.of();

        return new TransaccionResponse(
                t.getTransaccionId(),
                t.getEmpresa().getEmpresaId(),
                t.getTipo().name(),
                t.getCliente() != null ? t.getCliente().getClienteId() : null,
                t.getCliente() != null ? t.getCliente().getNombre() : null,
                t.getProveedor() != null ? t.getProveedor().getProveedorId() : null,
                t.getProveedor() != null ? t.getProveedor().getNombre() : null,
                t.getNumeroFactura(),
                t.getFecha(),
                t.getMoneda(),
                t.getTasaBcvUsada(),
                t.getSubtotal(),
                t.getIvaPorcentaje(),
                t.getIvaMonto(),
                t.isIgtfAplica(),
                t.getIgtfMonto(),
                t.getDescuentoGlobalPorcentaje(),
                t.getDescuentoGlobalMonto(),
                t.getTotal(),
                t.getTotalUsd(),
                t.getTotalVes(),
                t.getMetodoPago().name(),
                t.getEstado().name(),
                t.getMotivoAnulacion(),
                t.getNotas(),
                lineasResp,
                t.getCreatedAt()
        );
    }
}
