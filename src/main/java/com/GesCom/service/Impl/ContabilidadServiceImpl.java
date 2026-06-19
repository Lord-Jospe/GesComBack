package com.GesCom.service.Impl;

import com.GesCom.dto.request.CrearAsientoRequest;
import com.GesCom.dto.response.*;
import com.GesCom.enums.TipoCuenta;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.service.ContabilidadService;
import com.GesCom.service.SuscripcionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContabilidadServiceImpl implements ContabilidadService {

    private final PlanCuentaRepository planCuentaRepository;
    private final AsientoContableRepository asientoRepository;
    private final EmpresaRepository empresaRepository;
    private final SuscripcionService suscripcionService;

    // ─── Plan de cuentas ──────────────────────────────────────────

    @Override @Transactional(readOnly = true)
    public List<PlanCuentaResponse> obtenerPlanCuentas(Long empresaId) {
        return planCuentaRepository.findByEmpresa_EmpresaIdAndIsActiveTrueOrderByCodigo(empresaId)
                .stream().map(this::toPlanResponse).toList();
    }

    @Override @Transactional
    public PlanCuentaResponse crearCuenta(TipoCuenta tipoCuenta, String codigo, String nombre, Long cuentaPadreId, Long empresaId) {
        if (planCuentaRepository.existsByEmpresa_EmpresaIdAndCodigo(empresaId, codigo)) {
            throw new IllegalArgumentException("Ya existe una cuenta con el código " + codigo);
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        PlanCuenta c = planCuentaRepository.save(PlanCuenta.builder()
                .empresa(empresa).codigo(codigo).nombre(nombre)
                .tipoCuenta(tipoCuenta).cuentaPadreId(cuentaPadreId).isActive(true).build());
        log.info("Cuenta creada: {} - {}", codigo, nombre);
        return toPlanResponse(c);
    }

    @Override @Transactional
    public void desactivarCuenta(Long cuentaId, Long empresaId) {
        PlanCuenta c = planCuentaRepository.findByCuentaIdAndEmpresa_EmpresaId(cuentaId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));
        c.setActive(false);
        planCuentaRepository.save(c);
        log.info("Cuenta desactivada: {}", c.getCodigo());
    }

    // ─── Asientos ─────────────────────────────────────────────────

    @Override @Transactional
    public AsientoResponse crearAsientoManual(CrearAsientoRequest request, Long empresaId) {
        suscripcionService.verificarAccesoContabilidad(empresaId);
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        int numero = asientoRepository.maxNumeroAsiento(empresaId) + 1;

        AsientoContable asiento = AsientoContable.builder()
                .empresa(empresa).numeroAsiento(numero)
                .fecha(request.fecha()).descripcion(request.descripcion())
                .esAutomatico(false).build();

        BigDecimal totalDebito = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;
        List<LineaAsiento> lineas = new ArrayList<>();

        for (var item : request.lineas()) {
            PlanCuenta cuenta = planCuentaRepository.findByCuentaIdAndEmpresa_EmpresaId(item.cuentaId(), empresaId)
                    .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada: " + item.cuentaId()));

            LineaAsiento linea = LineaAsiento.builder()
                    .asiento(asiento).cuenta(cuenta)
                    .esDebito(item.esDebito()).monto(item.monto()).build();
            lineas.add(linea);

            if (item.esDebito()) totalDebito = totalDebito.add(item.monto());
            else totalCredito = totalCredito.add(item.monto());
        }

        if (totalDebito.compareTo(totalCredito) != 0) {
            throw new IllegalArgumentException(
                    "El asiento no está cuadrado. Débito: " + totalDebito + " ≠ Crédito: " + totalCredito);
        }

        asiento.setLineas(lineas);
        asientoRepository.save(asiento);
        log.info("Asiento manual #{} creado: {}", numero, request.descripcion());
        return toAsientoResponse(asiento);
    }

    @Override @Transactional
    public void crearAsientoAutomatico(Transaccion transaccion) {
        Long empresaId = transaccion.getEmpresa().getEmpresaId();
        Empresa empresa = transaccion.getEmpresa();

        int numero = asientoRepository.maxNumeroAsiento(empresaId) + 1;

        // Buscar cuentas por código estándar VEN-NIF
        PlanCuenta cuentaEfectivo = buscarPorCodigo("1.1.1", empresaId);
        PlanCuenta cuentaCobrar = buscarPorCodigo("1.1.2", empresaId);
        PlanCuenta cuentaPagar = buscarPorCodigo("2.1.1", empresaId);
        PlanCuenta cuentaImpuestos = buscarPorCodigo("2.1.2", empresaId);
        PlanCuenta cuentaVentas = buscarPorCodigo("4.1", empresaId);
        PlanCuenta cuentaInventario = buscarPorCodigo("1.1.3", empresaId);
        PlanCuenta cuentaCostoVentas = buscarPorCodigo("5.1", empresaId);
        PlanCuenta cuentaGastosAdmin = buscarPorCodigo("5.3", empresaId);

        // Si faltan cuentas esenciales, no se genera el asiento
        if (cuentaEfectivo == null || cuentaCobrar == null || cuentaPagar == null
                || cuentaImpuestos == null || cuentaVentas == null) {
            log.warn("No se generó asiento automático: faltan cuentas estándar en el plan contable de empresa {}", empresaId);
            return;
        }

        AsientoContable asiento = AsientoContable.builder()
                .empresa(empresa).numeroAsiento(numero)
                .fecha(transaccion.getFecha())
                .transaccionId(transaccion.getTransaccionId())
                .esAutomatico(true).build();

        List<LineaAsiento> lineas = new ArrayList<>();
        boolean esIngreso = transaccion.getTipo() == com.GesCom.enums.TipoTransaccion.INGRESO;
        boolean esPendiente = transaccion.getEstado() == com.GesCom.enums.EstadoTransaccion.PENDIENTE
                || transaccion.getEstado() == com.GesCom.enums.EstadoTransaccion.PARCIAL;

        if (esIngreso) {
            // ─── VENTA ────────────────────────────────────────────────
            asiento.setDescripcion("Venta — Factura " + (transaccion.getNumeroFactura() != null
                    ? transaccion.getNumeroFactura() : "#" + transaccion.getTransaccionId()));

            // Débito: Caja o Cuentas por Cobrar = TOTAL
            PlanCuenta cuentaDebito = esPendiente ? cuentaCobrar : cuentaEfectivo;
            lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaDebito)
                    .esDebito(true).monto(transaccion.getTotal()).build());

            // Crédito: Ventas = subtotal (base imponible, sin IVA ni IGTF)
            BigDecimal baseVenta = transaccion.getSubtotal();
            if (transaccion.getDescuentoGlobalMonto() != null) {
                baseVenta = baseVenta.subtract(transaccion.getDescuentoGlobalMonto());
            }
            lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaVentas)
                    .esDebito(false).monto(baseVenta).build());

            // Crédito: Impuestos por Pagar = IVA (si aplica)
            if (transaccion.getIvaMonto() != null && transaccion.getIvaMonto().compareTo(BigDecimal.ZERO) > 0) {
                lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaImpuestos)
                        .esDebito(false).monto(transaccion.getIvaMonto()).build());
            }

            // Si hay IGTF, se registra contra Impuestos por Pagar también
            if (transaccion.isIgtfAplica() && transaccion.getIgtfMonto() != null
                    && transaccion.getIgtfMonto().compareTo(BigDecimal.ZERO) > 0) {
                lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaImpuestos)
                        .esDebito(false).monto(transaccion.getIgtfMonto()).build());
            }

        } else {
            // ─── COMPRA / GASTO ───────────────────────────────────────
            asiento.setDescripcion("Compra — Transacción #" + transaccion.getTransaccionId());

            boolean tieneProductos = transaccion.getLineas().stream()
                    .anyMatch(l -> l.getProductoId() != null);

            // Débito: Inventario o Gastos = subtotal (base imponible)
            PlanCuenta cuentaGasto = tieneProductos ? cuentaInventario : cuentaGastosAdmin;
            BigDecimal baseCompra = transaccion.getSubtotal();
            if (transaccion.getDescuentoGlobalMonto() != null) {
                baseCompra = baseCompra.subtract(transaccion.getDescuentoGlobalMonto());
            }
            lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaGasto)
                    .esDebito(true).monto(baseCompra).build());

            // Débito: Impuestos por Pagar = IVA (crédito fiscal — reduce el pasivo)
            if (transaccion.getIvaMonto() != null && transaccion.getIvaMonto().compareTo(BigDecimal.ZERO) > 0) {
                lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaImpuestos)
                        .esDebito(true).monto(transaccion.getIvaMonto()).build());
            }

            // Si hay IGTF en compra, es un gasto adicional
            if (transaccion.isIgtfAplica() && transaccion.getIgtfMonto() != null
                    && transaccion.getIgtfMonto().compareTo(BigDecimal.ZERO) > 0) {
                lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaGastosAdmin)
                        .esDebito(true).monto(transaccion.getIgtfMonto()).build());
            }

            // Crédito: Caja o Cuentas por Pagar = TOTAL
            PlanCuenta cuentaCredito = esPendiente ? cuentaPagar : cuentaEfectivo;
            lineas.add(LineaAsiento.builder().asiento(asiento).cuenta(cuentaCredito)
                    .esDebito(false).monto(transaccion.getTotal()).build());
        }

        // Validar que el asiento esté cuadrado
        BigDecimal totalDebito = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;
        for (LineaAsiento l : lineas) {
            if (l.isEsDebito()) totalDebito = totalDebito.add(l.getMonto());
            else totalCredito = totalCredito.add(l.getMonto());
        }

        if (totalDebito.compareTo(totalCredito) != 0) {
            log.error("Asiento automático descuadrado (D={} C={}) para transacción #{}. No se generó.",
                    totalDebito, totalCredito, transaccion.getTransaccionId());
            return;
        }

        asiento.setLineas(lineas);
        asientoRepository.save(asiento);
        log.info("Asiento automático #{} generado para transacción #{}", numero, transaccion.getTransaccionId());
    }

    private PlanCuenta buscarPorCodigo(String codigo, Long empresaId) {
        return planCuentaRepository.findByEmpresa_EmpresaIdAndCodigo(empresaId, codigo).orElse(null);
    }

    @Override @Transactional(readOnly = true)
    public AsientoResponse obtenerAsiento(Long id, Long empresaId) {
        return toAsientoResponse(asientoRepository.findByAsientoIdAndEmpresa_EmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Asiento no encontrado")));
    }

    // ─── Libro Diario (RF-49) ─────────────────────────────────────

    @Override @Transactional(readOnly = true)
    public List<AsientoResponse> libroDiario(Long empresaId, LocalDate desde, LocalDate hasta) {
        List<AsientoContable> asientos;
        if (desde != null && hasta != null) {
            asientos = asientoRepository.findByEmpresa_EmpresaIdAndFechaBetweenOrderByFechaAscNumeroAsientoAsc(empresaId, desde, hasta);
        } else {
            asientos = asientoRepository.findByEmpresa_EmpresaIdOrderByFechaAscNumeroAsientoAsc(empresaId);
        }
        return asientos.stream().map(this::toAsientoResponse).toList();
    }

    // ─── Libro Mayor (RF-50) ──────────────────────────────────────

    @Override @Transactional(readOnly = true)
    public LibroMayorResponse libroMayor(Long cuentaId, Long empresaId, LocalDate desde, LocalDate hasta) {
        PlanCuenta cuenta = planCuentaRepository.findByCuentaIdAndEmpresa_EmpresaId(cuentaId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));

        // Incluir la cuenta seleccionada + todas sus cuentas hijas
        Set<Long> idsCuentas = new HashSet<>();
        idsCuentas.add(cuentaId);
        List<PlanCuenta> todas = planCuentaRepository.findByEmpresa_EmpresaIdAndIsActiveTrueOrderByCodigo(empresaId);
        for (PlanCuenta c : todas) {
            if (c.getCuentaPadreId() != null && idsCuentas.contains(c.getCuentaPadreId())) {
                idsCuentas.add(c.getCuentaId());
            }
        }

        List<AsientoContable> asientos;
        if (desde != null && hasta != null) {
            asientos = asientoRepository.findByEmpresa_EmpresaIdAndFechaBetweenOrderByFechaAscNumeroAsientoAsc(empresaId, desde, hasta);
        } else {
            asientos = asientoRepository.findByEmpresa_EmpresaIdOrderByFechaAscNumeroAsientoAsc(empresaId);
        }

        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;
        List<LineaAsientoResponse> movimientos = new ArrayList<>();

        for (AsientoContable a : asientos) {
            for (LineaAsiento l : a.getLineas()) {
                if (idsCuentas.contains(l.getCuenta().getCuentaId())) {
                    movimientos.add(toLineaResponse(l));
                    if (l.isEsDebito()) totalDebitos = totalDebitos.add(l.getMonto());
                    else totalCreditos = totalCreditos.add(l.getMonto());
                }
            }
        }

        BigDecimal saldoFinal = totalDebitos.subtract(totalCreditos);
        if (cuenta.getTipoCuenta() == TipoCuenta.PASIVO || cuenta.getTipoCuenta() == TipoCuenta.PATRIMONIO
                || cuenta.getTipoCuenta() == TipoCuenta.INGRESO) {
            saldoFinal = totalCreditos.subtract(totalDebitos);
        }

        return LibroMayorResponse.builder()
                .cuentaId(cuentaId).cuentaCodigo(cuenta.getCodigo()).cuentaNombre(cuenta.getNombre())
                .tipoCuenta(cuenta.getTipoCuenta().name()).saldoInicial(BigDecimal.ZERO)
                .totalDebitos(totalDebitos).totalCreditos(totalCreditos).saldoFinal(saldoFinal)
                .movimientos(movimientos).build();
    }

    // ─── Estado de Resultados (RF-51) ─────────────────────────────

    @Override @Transactional(readOnly = true)
    public EstadoResultadosResponse estadoResultados(Long empresaId, LocalDate desde, LocalDate hasta) {
        List<AsientoContable> asientos = asientoRepository
                .findByEmpresa_EmpresaIdAndFechaBetweenOrderByFechaAscNumeroAsientoAsc(empresaId, desde, hasta);

        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalGastos = BigDecimal.ZERO;

        for (AsientoContable a : asientos) {
            for (LineaAsiento l : a.getLineas()) {
                TipoCuenta tipo = l.getCuenta().getTipoCuenta();
                if (tipo == TipoCuenta.INGRESO) {
                    if (l.isEsDebito()) totalIngresos = totalIngresos.subtract(l.getMonto());
                    else totalIngresos = totalIngresos.add(l.getMonto());
                } else if (tipo == TipoCuenta.GASTO) {
                    if (l.isEsDebito()) totalGastos = totalGastos.add(l.getMonto());
                    else totalGastos = totalGastos.subtract(l.getMonto());
                }
            }
        }

        return EstadoResultadosResponse.builder()
                .fechaInicio(desde).fechaFin(hasta)
                .totalIngresos(totalIngresos).totalGastos(totalGastos)
                .utilidadNeta(totalIngresos.subtract(totalGastos)).build();
    }

    // ─── Balance General (RF-52) ──────────────────────────────────

    @Override @Transactional(readOnly = true)
    public BalanceGeneralResponse balanceGeneral(Long empresaId, LocalDate fecha) {
        List<AsientoContable> asientos = asientoRepository
                .findByEmpresa_EmpresaIdAndFechaBetweenOrderByFechaAscNumeroAsientoAsc(empresaId, LocalDate.of(2000,1,1), fecha);

        BigDecimal activo = BigDecimal.ZERO;
        BigDecimal pasivo = BigDecimal.ZERO;
        BigDecimal patrimonio = BigDecimal.ZERO;

        for (AsientoContable a : asientos) {
            for (LineaAsiento l : a.getLineas()) {
                TipoCuenta tipo = l.getCuenta().getTipoCuenta();
                BigDecimal monto = l.getMonto();
                if (l.isEsDebito()) {
                    if (tipo == TipoCuenta.ACTIVO) activo = activo.add(monto);
                    else if (tipo == TipoCuenta.PASIVO) pasivo = pasivo.subtract(monto);
                    else if (tipo == TipoCuenta.PATRIMONIO) patrimonio = patrimonio.subtract(monto);
                } else {
                    if (tipo == TipoCuenta.ACTIVO) activo = activo.subtract(monto);
                    else if (tipo == TipoCuenta.PASIVO) pasivo = pasivo.add(monto);
                    else if (tipo == TipoCuenta.PATRIMONIO) patrimonio = patrimonio.add(monto);
                }
            }
        }

        boolean cuadrado = activo.compareTo(pasivo.add(patrimonio)) == 0;

        return BalanceGeneralResponse.builder()
                .fecha(fecha).totalActivos(activo).totalPasivos(pasivo)
                .totalPatrimonio(patrimonio).cuadrado(cuadrado).build();
    }

    // ─── Cierre de período (RF-55) ────────────────────────────────

    @Override @Transactional
    public void cerrarPeriodo(Long empresaId, LocalDate desde, LocalDate hasta) {
        List<AsientoContable> asientos = asientoRepository
                .findByEmpresa_EmpresaIdAndFechaBetweenOrderByFechaAscNumeroAsientoAsc(empresaId, desde, hasta);
        for (AsientoContable a : asientos) {
            a.setPeriodoCerrado(true);
        }
        asientoRepository.saveAll(asientos);
        log.info("Período cerrado: {} a {} — {} asientos bloqueados", desde, hasta, asientos.size());
    }

    // ─── Helpers ───────────────────────────────────────────────────

    private PlanCuentaResponse toPlanResponse(PlanCuenta c) {
        return PlanCuentaResponse.builder()
                .cuentaId(c.getCuentaId()).codigo(c.getCodigo()).nombre(c.getNombre())
                .tipoCuenta(c.getTipoCuenta().name()).cuentaPadreId(c.getCuentaPadreId())
                .activo(c.isActive()).esPredeterminada(c.isEsPredeterminada()).build();
    }

    private LineaAsientoResponse toLineaResponse(LineaAsiento l) {
        return LineaAsientoResponse.builder()
                .lineaId(l.getLineaLibroId()).cuentaId(l.getCuenta().getCuentaId())
                .cuentaCodigo(l.getCuenta().getCodigo()).cuentaNombre(l.getCuenta().getNombre())
                .esDebito(l.isEsDebito()).monto(l.getMonto()).build();
    }

    private AsientoResponse toAsientoResponse(AsientoContable a) {
        BigDecimal debito = BigDecimal.ZERO;
        BigDecimal credito = BigDecimal.ZERO;
        List<LineaAsientoResponse> lineas = new ArrayList<>();
        for (LineaAsiento l : a.getLineas()) {
            lineas.add(toLineaResponse(l));
            if (l.isEsDebito()) debito = debito.add(l.getMonto());
            else credito = credito.add(l.getMonto());
        }
        return AsientoResponse.builder()
                .asientoId(a.getAsientoId()).numeroAsiento(a.getNumeroAsiento())
                .fecha(a.getFecha()).descripcion(a.getDescripcion())
                .transaccionId(a.getTransaccionId()).esAutomatico(a.isEsAutomatico())
                .periodoCerrado(a.isPeriodoCerrado()).totalDebito(debito).totalCredito(credito)
                .lineas(lineas).createdAt(a.getCreatedAt()).build();
    }
}
