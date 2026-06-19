package com.GesCom.service.Impl;

import com.GesCom.dto.response.*;
import com.GesCom.enums.EstadoTransaccion;
import com.GesCom.enums.MetodoPago;
import com.GesCom.enums.TipoTransaccion;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.service.ConciliacionService;
import com.GesCom.service.TransaccionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConciliacionServiceImpl implements ConciliacionService {

    private final MovimientoBancoRepository movimientoBancoRepository;
    private final TransaccionRepository transaccionRepository;
    private final EmpresaRepository empresaRepository;
    private final TransaccionService transaccionService;

    @Override
    @Transactional
    public MovimientoBancoResponse agregarMovimiento(Long empresaId, LocalDate fecha, String descripcion, BigDecimal monto, String tipo) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        MovimientoBanco mb = movimientoBancoRepository.save(MovimientoBanco.builder()
                .empresa(empresa).fecha(fecha).descripcion(descripcion)
                .monto(monto).tipo(tipo).conciliado(false).build());
        log.info("Movimiento banco agregado: {} - {} {}", mb.getMovimientoBancoId(), tipo, monto);
        return toResponse(mb);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoBancoResponse> listarMovimientos(Long empresaId) {
        return movimientoBancoRepository.findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ConciliacionResponse obtenerConciliacion(Long empresaId, LocalDate desde, LocalDate hasta) {
        List<MovimientoBanco> todos = movimientoBancoRepository.findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId);
        List<MovimientoBancoResponse> conciliados = new ArrayList<>();
        List<MovimientoBancoResponse> sinConciliar = new ArrayList<>();
        Set<Long> txConciliadas = new HashSet<>();
        Set<Long> txIdsYaVinculadas = new HashSet<>();

        for (MovimientoBanco mb : todos) {
            if (mb.isConciliado() && mb.getFecha().compareTo(desde) >= 0 && mb.getFecha().compareTo(hasta) <= 0) {
                conciliados.add(toResponse(mb));
            }
            if (mb.getTransaccionId() != null) txIdsYaVinculadas.add(mb.getTransaccionId());
            if (mb.isConciliado() && mb.getTransaccionId() != null) txConciliadas.add(mb.getTransaccionId());
            if (!mb.isConciliado()) sinConciliar.add(toResponse(mb));
        }

        // Transacciones bancarias no conciliadas en el rango
        List<ConciliacionResponse.TxConciliar> txSinConciliar = transaccionRepository
                .findByEmpresa_EmpresaIdAndFechaBetweenOrderByFechaDesc(empresaId, desde, hasta)
                .stream()
                .filter(t -> !txIdsYaVinculadas.contains(t.getTransaccionId()))
                .filter(t -> t.getMetodoPago() != null && (
                        t.getMetodoPago().name().equals("TRANSFERENCIA") ||
                        t.getMetodoPago().name().equals("PAGO_MOVIL") ||
                        t.getMetodoPago().name().equals("DIVISAS")))
                .map(t -> ConciliacionResponse.TxConciliar.builder()
                        .transaccionId(t.getTransaccionId())
                        .tipo(t.getTipo().name())
                        .clienteProveedor(t.getCliente() != null ? t.getCliente().getNombre() :
                                t.getProveedor() != null ? t.getProveedor().getNombre() : null)
                        .numeroFactura(t.getNumeroFactura())
                        .fecha(t.getFecha())
                        .moneda(t.getMoneda())
                        .total(t.getTotal())
                        .estado(t.getEstado().name())
                        .metodoPago(t.getMetodoPago().name())
                        .build())
                .toList();

        return ConciliacionResponse.builder()
                .conciliados(conciliados)
                .sinConciliarBanco(sinConciliar)
                .sinConciliarGesCom(txSinConciliar)
                .build();
    }

    @Override
    @Transactional
    public int autoConciliar(Long empresaId) {
        List<MovimientoBanco> sinConciliar = movimientoBancoRepository
                .findByEmpresa_EmpresaIdAndConciliadoOrderByFechaDesc(empresaId, false);
        List<Transaccion> txns = transaccionRepository.findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId);
        Set<Long> yaVinculadas = new HashSet<>();
        // Recolectar transacciones ya vinculadas
        movimientoBancoRepository.findByEmpresa_EmpresaIdAndConciliadoOrderByFechaDesc(empresaId, true)
                .forEach(mb -> { if (mb.getTransaccionId() != null) yaVinculadas.add(mb.getTransaccionId()); });

        int count = 0;
        for (MovimientoBanco mb : sinConciliar) {
            for (Transaccion t : txns) {
                if (yaVinculadas.contains(t.getTransaccionId())) continue;
                // Coincidencia: mismo monto, fecha ±3 días, tipo compatible
                boolean mismoMonto = mb.getMonto().compareTo(t.getTotal()) == 0;
                boolean fechaCercana = Math.abs(mb.getFecha().toEpochDay() - t.getFecha().toEpochDay()) <= 3;
                boolean tipoCompatible = (mb.getTipo().equals("INGRESO") && t.getTipo() == TipoTransaccion.INGRESO) ||
                                        (mb.getTipo().equals("EGRESO") && t.getTipo() == TipoTransaccion.EGRESO);
                if (mismoMonto && fechaCercana && tipoCompatible) {
                    mb.setConciliado(true);
                    mb.setTransaccionId(t.getTransaccionId());
                    mb.setFechaConciliacion(LocalDate.now());
                    movimientoBancoRepository.save(mb);
                    yaVinculadas.add(t.getTransaccionId());
                    count++;
                    break;
                }
            }
        }
        log.info("Auto-conciliación: {} movimientos conciliados para empresa {}", count, empresaId);
        return count;
    }

    @Override
    @Transactional
    public void vincular(Long movimientoBancoId, Long transaccionId, Long empresaId) {
        MovimientoBanco mb = movimientoBancoRepository
                .findByMovimientoBancoIdAndEmpresa_EmpresaId(movimientoBancoId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));
        mb.setTransaccionId(transaccionId);
        mb.setConciliado(true);
        mb.setFechaConciliacion(LocalDate.now());
        movimientoBancoRepository.save(mb);
        log.info("Conciliado: movimiento banco {} ↔ transacción {}", movimientoBancoId, transaccionId);
    }

    @Override
    @Transactional
    public void desvincular(Long movimientoBancoId, Long empresaId) {
        MovimientoBanco mb = movimientoBancoRepository
                .findByMovimientoBancoIdAndEmpresa_EmpresaId(movimientoBancoId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));
        mb.setTransaccionId(null);
        mb.setConciliado(false);
        mb.setFechaConciliacion(null);
        movimientoBancoRepository.save(mb);
    }

    @Override
    @Transactional
    public void conciliarSinTransaccion(Long movimientoBancoId, Long empresaId) {
        MovimientoBanco mb = movimientoBancoRepository
                .findByMovimientoBancoIdAndEmpresa_EmpresaId(movimientoBancoId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));

        // Crear transacción automática para reflejar en ingresos/egresos
        boolean esIngreso = "INGRESO".equals(mb.getTipo());
        Transaccion t = transaccionRepository.save(Transaccion.builder()
                .empresa(mb.getEmpresa())
                .tipo(esIngreso ? TipoTransaccion.INGRESO : TipoTransaccion.EGRESO)
                .fecha(mb.getFecha())
                .moneda(mb.getEmpresa().getMonedaBase())
                .subtotal(mb.getMonto())
                .total(mb.getMonto())
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .estado(EstadoTransaccion.PAGADA)
                .notas("Conciliación bancaria: " + mb.getDescripcion())
                .build());

        mb.setConciliado(true);
        mb.setTransaccionId(t.getTransaccionId());
        mb.setFechaConciliacion(LocalDate.now());
        movimientoBancoRepository.save(mb);
        log.info("Conciliado con transacción #{}: {}", t.getTransaccionId(), mb.getDescripcion());
    }

    @Override
    @Transactional
    public void eliminarMovimiento(Long movimientoBancoId, Long empresaId) {
        MovimientoBanco mb = movimientoBancoRepository
                .findByMovimientoBancoIdAndEmpresa_EmpresaId(movimientoBancoId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));
        movimientoBancoRepository.delete(mb);
    }

    @Override
    @Transactional
    public void importarCSV(Long empresaId, String csvContent) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        String[] lineas = csvContent.split("\n");
        int count = 0;
        for (int i = 1; i < lineas.length; i++) {
            String linea = lineas[i].trim();
            if (linea.isEmpty()) continue;
            String[] cols = linea.split(",");
            if (cols.length < 4) continue;
            try {
                LocalDate fecha = LocalDate.parse(cols[0].trim());
                String desc = cols[1].trim().replace("\"", "");
                BigDecimal monto = new BigDecimal(cols[2].trim());
                String tipo = cols[3].trim().toUpperCase();
                if (!tipo.equals("INGRESO") && !tipo.equals("EGRESO")) tipo = "INGRESO";
                movimientoBancoRepository.save(MovimientoBanco.builder()
                        .empresa(empresa).fecha(fecha).descripcion(desc)
                        .monto(monto).tipo(tipo).conciliado(false).build());
                count++;
            } catch (Exception ignored) {}
        }
        log.info("Importados {} movimientos bancarios para empresa {}", count, empresaId);
        // Auto-conciliar inmediatamente después de importar
        int matched = autoConciliar(empresaId);
        log.info("Auto-conciliados {} después de importar CSV", matched);
    }

    // ─── Helpers ───────────────────────────────────────────────────

    private MovimientoBancoResponse toResponse(MovimientoBanco mb) {
        String numFactura = null;
        if (mb.getTransaccionId() != null) {
            numFactura = transaccionRepository.findById(mb.getTransaccionId())
                    .map(Transaccion::getNumeroFactura).orElse(null);
        }
        return MovimientoBancoResponse.builder()
                .movimientoBancoId(mb.getMovimientoBancoId())
                .fecha(mb.getFecha()).descripcion(mb.getDescripcion())
                .monto(mb.getMonto()).tipo(mb.getTipo())
                .transaccionId(mb.getTransaccionId()).numeroFactura(numFactura)
                .conciliado(mb.isConciliado()).fechaConciliacion(mb.getFechaConciliacion())
                .createdAt(mb.getCreatedAt()).build();
    }
}
