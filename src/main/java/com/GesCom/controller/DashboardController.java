package com.GesCom.controller;

import com.GesCom.dto.response.*;
import com.GesCom.enums.EstadoTransaccion;
import com.GesCom.enums.TipoTransaccion;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.security.user.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TransaccionRepository transaccionRepository;
    private final EmpresaRepository empresaRepository;
    private final TasaBcvRepository tasaBcvRepository;
    private final ProductoRepository productoRepository;

    private Long empresaId(UsuarioDetails ud) { return ud.getUsuario().getEmpresa().getEmpresaId(); }

    // GET /api/dashboard/today
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<DashboardResumenResponse> resumenHoy(@AuthenticationPrincipal UsuarioDetails ud) {
        Long empId = empresaId(ud);
        LocalDate hoy = LocalDate.now();
        Empresa empresa = empresaRepository.findById(empId).orElseThrow();
        String moneda = empresa.getMonedaBase();

        List<Transaccion> delDia = transaccionRepository
                .findByEmpresa_EmpresaIdAndFecha(empId, hoy);
        delDia = delDia.stream().filter(t -> t.getEstado() != EstadoTransaccion.ANULADA).toList();

        BigDecimal ventas = delDia.stream().filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gastos = delDia.stream().filter(t -> t.getTipo() == TipoTransaccion.EGRESO)
                .map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(DashboardResumenResponse.builder()
                .ventas(ventas).gastos(gastos).ganancia(ventas.subtract(gastos))
                .transacciones(delDia.size()).moneda(moneda).build());
    }

    // GET /api/dashboard/monthly
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<DashboardResumenResponse> resumenMes(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mes) {
        Long empId = empresaId(ud);
        if (mes == null) mes = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = mes.plusMonths(1).minusDays(1);
        Empresa empresa = empresaRepository.findById(empId).orElseThrow();
        String moneda = empresa.getMonedaBase();

        List<Transaccion> delMes = transaccionRepository
                .findByEmpresa_EmpresaIdAndFechaBetween(empId, mes, fin);
        delMes = delMes.stream().filter(t -> t.getEstado() != EstadoTransaccion.ANULADA).toList();

        BigDecimal ventas = delMes.stream().filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gastos = delMes.stream().filter(t -> t.getTipo() == TipoTransaccion.EGRESO)
                .map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(DashboardResumenResponse.builder()
                .ventas(ventas).gastos(gastos).ganancia(ventas.subtract(gastos))
                .transacciones(delMes.size()).moneda(moneda).build());
    }

    // GET /api/dashboard/charts
    @GetMapping("/charts")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<DashboardChartsResponse> charts(@AuthenticationPrincipal UsuarioDetails ud) {
        Long empId = empresaId(ud);
        LocalDate hoy = LocalDate.now();
        Empresa empresa = empresaRepository.findById(empId).orElseThrow();
        String moneda = empresa.getMonedaBase();

        // Ventas últimos 30 días
        List<DashboardChartsResponse.VentasDiariasItem> ventas30 = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            BigDecimal total = transaccionRepository.findByEmpresa_EmpresaIdAndFecha(empId, fecha)
                    .stream().filter(t -> t.getEstado() != EstadoTransaccion.ANULADA && t.getTipo() == TipoTransaccion.INGRESO)
                    .map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            ventas30.add(new DashboardChartsResponse.VentasDiariasItem(fecha, total));
        }

        // Ingresos vs Gastos últimos 6 meses
        List<DashboardChartsResponse.IngresoVsGastoItem> ivg = new ArrayList<>();
        String[] mesesNombres = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        for (int i = 5; i >= 0; i--) {
            LocalDate inicio = hoy.minusMonths(i).withDayOfMonth(1);
            LocalDate fin = inicio.plusMonths(1).minusDays(1);
            List<Transaccion> delMes = transaccionRepository.findByEmpresa_EmpresaIdAndFechaBetween(empId, inicio, fin)
                    .stream().filter(t -> t.getEstado() != EstadoTransaccion.ANULADA).toList();
            BigDecimal ing = delMes.stream().filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                    .map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal gas = delMes.stream().filter(t -> t.getTipo() == TipoTransaccion.EGRESO)
                    .map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            ivg.add(new DashboardChartsResponse.IngresoVsGastoItem(mesesNombres[inicio.getMonthValue()-1], ing, gas));
        }

        // Categorías (distribución de ingresos del mes actual por categoría de línea)
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy;
        List<Transaccion> ingresosMes = transaccionRepository.findByEmpresa_EmpresaIdAndFechaBetween(empId, inicioMes, finMes)
                .stream().filter(t -> t.getEstado() != EstadoTransaccion.ANULADA && t.getTipo() == TipoTransaccion.INGRESO).toList();
        Map<String, BigDecimal> catMap = new LinkedHashMap<>();
        BigDecimal totalCat = BigDecimal.ZERO;
        for (Transaccion t : ingresosMes) {
            for (TransaccionLinea l : t.getLineas()) {
                String cat = l.getDescripcion().length() > 20 ? l.getDescripcion().substring(0, 20) : l.getDescripcion();
                cat = cat.isBlank() ? "Sin categoría" : cat;
                BigDecimal monto = moneda.equals("USD") ? l.getSubtotalLinea() : l.getSubtotalLinea().multiply(t.getTasaBcvUsada());
                catMap.merge(cat, monto, BigDecimal::add);
                totalCat = totalCat.add(monto);
            }
        }
        List<DashboardChartsResponse.CategoriaItem> categorias = new ArrayList<>();
        for (var entry : catMap.entrySet()) {
            double pct = totalCat.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(new BigDecimal("100")).divide(totalCat, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0;
            categorias.add(new DashboardChartsResponse.CategoriaItem(entry.getKey(), entry.getValue(), pct));
        }

        // Pendientes
        BigDecimal porCobrar = transaccionRepository
                .findByEmpresa_EmpresaIdAndTipoAndEstado(empId, TipoTransaccion.INGRESO, EstadoTransaccion.PENDIENTE)
                .stream().map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal porPagar = transaccionRepository
                .findByEmpresa_EmpresaIdAndTipoAndEstado(empId, TipoTransaccion.EGRESO, EstadoTransaccion.PENDIENTE)
                .stream().map(t -> moneda.equals("USD") ? t.getTotalUsd() : t.getTotalVes())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long criticos = productoRepository.findStockCritico(empId).size();

        var tasaActual = tasaBcvRepository.findTopByEmpresa_EmpresaIdOrderByFechaHoraDesc(empId)
                .map(TasaBcv::getTasa).orElse(BigDecimal.ZERO);

        return ResponseEntity.ok(DashboardChartsResponse.builder()
                .ventas30Dias(ventas30).ingresosVsGastos6Meses(ivg).categorias(categorias)
                .porCobrar(porCobrar).porPagar(porPagar).productosCriticos(criticos)
                .monedaBase(moneda).tasaBcvActual(tasaActual).build());
    }
}
