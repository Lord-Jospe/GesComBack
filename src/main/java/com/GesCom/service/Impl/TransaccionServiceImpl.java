package com.GesCom.service.Impl;

import com.GesCom.dto.request.*;
import com.GesCom.dto.response.*;
import com.GesCom.enums.EstadoTransaccion;
import com.GesCom.enums.TipoMovimientoInventario;
import com.GesCom.enums.TipoTransaccion;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.service.ContabilidadService;
import com.GesCom.service.SuscripcionService;
import com.GesCom.service.TransaccionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ContabilidadService contabilidadService;
    private final SuscripcionService suscripcionService;

    private static final BigDecimal IGTF_RATE = new BigDecimal("3.00");

    @Override
    @Transactional
    public TransaccionResponse crear(CrearTransaccionRequest request, Long empresaId) {

        suscripcionService.verificarLimiteTransacciones(empresaId);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Cliente cliente = resolverCliente(request, empresaId);
        Proveedor proveedor = resolverProveedor(request, empresaId);

        // ─── 1. Obtener tasa BCV más reciente hasta la fecha ───────
        java.time.LocalDateTime inicioDia = request.fecha().atStartOfDay();
        java.time.LocalDateTime finDia = request.fecha().atTime(23, 59, 59);
        BigDecimal tasaBcv = tasaBcvRepository
                .findTopByEmpresa_EmpresaIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                        empresaId, inicioDia, finDia)
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
                .estado(request.pendiente() != null && request.pendiente()
                        ? EstadoTransaccion.PENDIENTE
                        : EstadoTransaccion.PAGADA)
                .notas(request.notas())
                .numeroFactura(numeroFactura)
                .build();

        // Asignar líneas
        for (TransaccionLinea linea : lineas) {
            linea.setTransaccion(transaccion);
        }
        transaccion.setLineas(lineas);

        transaccionRepository.save(transaccion);

        // ─── 10. Actualizar inventario (si aplica) ──────────────────
        procesarInventario(transaccion, request.tipo());

        // ─── 11. Generar asiento contable automático (RF-47) ─────────
        contabilidadService.crearAsientoAutomatico(transaccion);

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
        // Obtener todas las transacciones de la empresa
        var stream = transaccionRepository
                .findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId)
                .stream();

        // Aplicar filtros
        if (filtro != null) {
            if (filtro.tipo() != null)
                stream = stream.filter(t -> t.getTipo() == filtro.tipo());
            if (filtro.estado() != null)
                stream = stream.filter(t -> t.getEstado() == filtro.estado());
            if (filtro.clienteId() != null)
                stream = stream.filter(t -> t.getCliente() != null
                        && t.getCliente().getClienteId().equals(filtro.clienteId()));
            if (filtro.proveedorId() != null)
                stream = stream.filter(t -> t.getProveedor() != null
                        && t.getProveedor().getProveedorId().equals(filtro.proveedorId()));
            if (filtro.fechaDesde() != null)
                stream = stream.filter(t -> !t.getFecha().isBefore(filtro.fechaDesde()));
            if (filtro.fechaHasta() != null)
                stream = stream.filter(t -> !t.getFecha().isAfter(filtro.fechaHasta()));
        }

        return stream.map(this::toResponse).toList();
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

        // Revertir movimientos de inventario
        revertirInventario(t);

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
    @Transactional
    public TransaccionResponse emitirNotaCredito(Long transaccionOrigenId, String motivo,
                                                  BigDecimal monto, Long empresaId) {
        Transaccion origen = buscar(transaccionOrigenId, empresaId);

        if (origen.getTipo() != TipoTransaccion.INGRESO) {
            throw new IllegalArgumentException("Solo se puede emitir nota de crédito sobre facturas de INGRESO");
        }
        if (origen.getEstado() == EstadoTransaccion.ANULADA) {
            throw new IllegalArgumentException("No se puede emitir nota de crédito sobre una factura anulada");
        }

        Transaccion nota = Transaccion.builder()
                .empresa(origen.getEmpresa())
                .tipo(TipoTransaccion.NOTA_CREDITO)
                .cliente(origen.getCliente())
                .fecha(LocalDate.now())
                .moneda(origen.getMoneda())
                .tasaBcvUsada(origen.getTasaBcvUsada())
                .subtotal(monto.negate())
                .ivaPorcentaje(origen.getIvaPorcentaje())
                .ivaMonto(origen.getIvaMonto() != null
                        ? origen.getIvaMonto().negate() : BigDecimal.ZERO)
                .igtfAplica(origen.isIgtfAplica())
                .igtfMonto(origen.getIgtfMonto() != null
                        ? origen.getIgtfMonto().negate() : BigDecimal.ZERO)
                .total(monto.negate())
                .totalUsd(origen.getTotalUsd() != null
                        ? origen.getTotalUsd().negate() : null)
                .totalVes(origen.getTotalVes() != null
                        ? origen.getTotalVes().negate() : null)
                .metodoPago(origen.getMetodoPago())
                .estado(EstadoTransaccion.PAGADA) // la nota de crédito se aplica de inmediato
                .notas("Nota de crédito — Factura " + origen.getNumeroFactura() + ". Motivo: " + motivo)
                .transaccionOrigenId(transaccionOrigenId)
                .build();

        transaccionRepository.save(nota);
        log.info("Nota de crédito emitida: id={}, factura={}, monto={}",
                nota.getTransaccionId(), origen.getNumeroFactura(), monto);

        return toResponse(nota);
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

    // ─── Inventario: procesar movimientos al crear transacción ────

    private void procesarInventario(Transaccion transaccion, TipoTransaccion tipoTransaccion) {
        // Solo aplica para INGRESO (venta → salida de stock) y EGRESO (compra → entrada de stock)
        if (tipoTransaccion != TipoTransaccion.INGRESO && tipoTransaccion != TipoTransaccion.EGRESO) {
            return;
        }

        for (TransaccionLinea linea : transaccion.getLineas()) {
            if (linea.getProductoId() == null) continue;

            // Producto DEBE existir si se especificó productoId. Sin excepciones.
            Producto producto = productoRepository.findById(linea.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto con ID " + linea.getProductoId() + " no encontrado. "
                            + "La línea «" + linea.getDescripcion() + "» referencia un producto que ya no existe."));

            TipoMovimientoInventario tipoMov = (tipoTransaccion == TipoTransaccion.INGRESO)
                    ? TipoMovimientoInventario.SALIDA   // venta → disminuye stock
                    : TipoMovimientoInventario.ENTRADA; // compra → aumenta stock

            // Validar stock suficiente para salidas
            if (tipoMov == TipoMovimientoInventario.SALIDA
                    && producto.getStockActual().subtract(linea.getCantidad()).compareTo(BigDecimal.ZERO) < 0
                    && !producto.isVentaBajoPedido()) {
                throw new IllegalStateException(
                        "Stock insuficiente para «" + producto.getNombre() + "». Disponible: "
                        + producto.getStockActual() + " " + producto.getUnidadMedida()
                        + ". Active 'Venta bajo pedido' para permitir stock negativo.");
            }

            // Actualizar stock del producto
            BigDecimal nuevoStock = (tipoMov == TipoMovimientoInventario.ENTRADA)
                    ? producto.getStockActual().add(linea.getCantidad())
                    : producto.getStockActual().subtract(linea.getCantidad());
            producto.setStockActual(nuevoStock);

            // Si es una compra y el producto no tenía costo, se actualiza
            if (tipoTransaccion == TipoTransaccion.EGRESO
                    && (producto.getCostoUnitario() == null
                        || producto.getCostoUnitario().compareTo(BigDecimal.ZERO) == 0)) {
                producto.setCostoUnitario(linea.getPrecioUnitario());
            }

            productoRepository.save(producto);

            // Determinar costo unitario del movimiento
            BigDecimal costoMov = (tipoTransaccion == TipoTransaccion.EGRESO)
                    ? linea.getPrecioUnitario()       // costo de adquisición
                    : producto.getCostoUnitario();    // costo registrado del producto

            // Registrar movimiento de inventario
            MovimientoInventario mov = MovimientoInventario.builder()
                    .producto(producto)
                    .tipo(tipoMov)
                    .cantidad(linea.getCantidad())
                    .costoUnitario(costoMov)
                    .motivo(tipoTransaccion == TipoTransaccion.INGRESO
                            ? "Venta — Factura " + (transaccion.getNumeroFactura() != null
                                ? transaccion.getNumeroFactura() : "#" + transaccion.getTransaccionId())
                            : "Compra — Transacción #" + transaccion.getTransaccionId())
                    .transaccionId(transaccion.getTransaccionId())
                    .build();
            movimientoInventarioRepository.save(mov);

            log.info("Inventario actualizado: producto={}, tipo={}, cant={}, nuevoStock={}",
                    producto.getNombre(), tipoMov, linea.getCantidad(), nuevoStock);
        }
    }

    // ─── Inventario: revertir movimientos al anular transacción ────

    private void revertirInventario(Transaccion transaccion) {
        List<MovimientoInventario> movsOriginales = movimientoInventarioRepository
                .findByTransaccionId(transaccion.getTransaccionId());

        if (movsOriginales.isEmpty()) return;

        for (MovimientoInventario original : movsOriginales) {
            Producto producto = original.getProducto();
            TipoMovimientoInventario tipoReverso = (original.getTipo() == TipoMovimientoInventario.ENTRADA)
                    ? TipoMovimientoInventario.SALIDA
                    : TipoMovimientoInventario.ENTRADA;

            // Revertir stock
            BigDecimal nuevoStock = (tipoReverso == TipoMovimientoInventario.ENTRADA)
                    ? producto.getStockActual().add(original.getCantidad())
                    : producto.getStockActual().subtract(original.getCantidad());
            producto.setStockActual(nuevoStock);
            productoRepository.save(producto);

            // Registrar movimiento de reversión
            MovimientoInventario reverso = MovimientoInventario.builder()
                    .producto(producto)
                    .tipo(tipoReverso)
                    .cantidad(original.getCantidad())
                    .costoUnitario(original.getCostoUnitario())
                    .motivo("Anulación transacción #" + transaccion.getTransaccionId()
                            + " — " + (transaccion.getMotivoAnulacion() != null
                                ? transaccion.getMotivoAnulacion() : "sin motivo"))
                    .transaccionId(transaccion.getTransaccionId())
                    .build();
            movimientoInventarioRepository.save(reverso);

            log.info("Inventario revertido: producto={}, tipo={}, cant={}, nuevoStock={}",
                    producto.getNombre(), tipoReverso, original.getCantidad(), nuevoStock);
        }
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

        // ─── Indicador de vencimiento ──────────────────────────
        long dias = 0;
        String indicador = null;
        BigDecimal saldoPendiente = BigDecimal.ZERO;

        if (t.getEstado() == EstadoTransaccion.PENDIENTE
                || t.getEstado() == EstadoTransaccion.PARCIAL) {
            dias = ChronoUnit.DAYS.between(t.getFecha(), LocalDate.now());
            if (dias <= 15) {
                indicador = "VERDE";
            } else if (dias <= 30) {
                indicador = "AMARILLO";
            } else {
                indicador = "ROJO";
            }
            BigDecimal totalPagado = pagoRepository.sumMontoByTransaccionId(t.getTransaccionId());
            saldoPendiente = t.getTotal().subtract(totalPagado != null ? totalPagado : BigDecimal.ZERO);
        }

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
                dias,
                indicador,
                saldoPendiente,
                t.getCreatedAt()
        );
    }
}
