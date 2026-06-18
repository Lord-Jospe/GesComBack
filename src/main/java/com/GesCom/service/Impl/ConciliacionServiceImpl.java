package com.GesCom.service.Impl;

import com.GesCom.dto.response.*;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.service.ConciliacionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConciliacionServiceImpl implements ConciliacionService {

    private final MovimientoBancoRepository movimientoBancoRepository;
    private final TransaccionRepository transaccionRepository;
    private final EmpresaRepository empresaRepository;

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
    public ConciliacionResponse obtenerConciliacion(Long empresaId) {
        // Auto-match: intentar conciliar movimientos no conciliados
        autoMatch(empresaId);

        List<MovimientoBanco> todos = movimientoBancoRepository.findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId);
        List<MovimientoBancoResponse> conciliados = new ArrayList<>();
        List<MovimientoBancoResponse> sinConciliar = new ArrayList<>();
        Set<Long> txConciliadas = new HashSet<>();

        for (MovimientoBanco mb : todos) {
            var resp = toResponse(mb);
            if (mb.isConciliado()) {
                conciliados.add(resp);
                if (mb.getTransaccionId() != null) txConciliadas.add(mb.getTransaccionId());
            } else {
                sinConciliar.add(resp);
            }
        }

        // Transacciones bancarias no conciliadas
        List<ConciliacionResponse.TxConciliar> txSinConciliar = transaccionRepository
                .findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId)
                .stream()
                .filter(t -> !txConciliadas.contains(t.getTransaccionId()))
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
    public void vincular(Long movimientoBancoId, Long transaccionId, Long empresaId) {
        MovimientoBanco mb = movimientoBancoRepository
                .findByMovimientoBancoIdAndEmpresa_EmpresaId(movimientoBancoId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));

        Transaccion t = transaccionRepository
                .findByTransaccionIdAndEmpresa_EmpresaId(transaccionId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));

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
        log.info("Desvinculado: movimiento banco {}", movimientoBancoId);
    }

    @Override
    @Transactional
    public void importarCSV(Long empresaId, String csvContent) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        String[] lineas = csvContent.split("\n");
        int count = 0;
        for (int i = 1; i < lineas.length; i++) { // saltar cabecera
            String linea = lineas[i].trim();
            if (linea.isEmpty()) continue;
            String[] cols = linea.split(",");
            if (cols.length < 4) continue;
            try {
                LocalDate fecha = LocalDate.parse(cols[0].trim());
                String desc = cols[1].trim().replace("\"", "");
                BigDecimal monto = new BigDecimal(cols[2].trim());
                String tipo = cols[3].trim().toUpperCase(); // CREDITO o DEBITO
                if (!tipo.equals("CREDITO") && !tipo.equals("DEBITO")) tipo = "CREDITO";
                movimientoBancoRepository.save(MovimientoBanco.builder()
                        .empresa(empresa).fecha(fecha).descripcion(desc)
                        .monto(monto).tipo(tipo).conciliado(false).build());
                count++;
            } catch (Exception ignored) { /* línea inválida, saltar */ }
        }
        log.info("Importados {} movimientos bancarios desde CSV para empresa {}", count, empresaId);
    }

    private void autoMatch(Long empresaId) {
        List<MovimientoBanco> sinConciliar = movimientoBancoRepository
                .findByEmpresa_EmpresaIdAndConciliadoOrderByFechaDesc(empresaId, false);
        List<Transaccion> txns = transaccionRepository.findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId);

        for (MovimientoBanco mb : sinConciliar) {
            for (Transaccion t : txns) {
                // Coincidencia: mismo monto, fecha +/- 3 días
                if (Math.abs(mb.getMonto().compareTo(t.getTotal())) == 0
                        && Math.abs(mb.getFecha().toEpochDay() - t.getFecha().toEpochDay()) <= 3) {
                    mb.setConciliado(true);
                    mb.setTransaccionId(t.getTransaccionId());
                    mb.setFechaConciliacion(LocalDate.now());
                    movimientoBancoRepository.save(mb);
                    break;
                }
            }
        }
    }

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
