package com.GesCom.service;

import com.GesCom.dto.response.MovimientoBancoResponse;
import com.GesCom.dto.response.ConciliacionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ConciliacionService {
    MovimientoBancoResponse agregarMovimiento(Long empresaId, LocalDate fecha, String descripcion, java.math.BigDecimal monto, String tipo);
    List<MovimientoBancoResponse> listarMovimientos(Long empresaId);
    ConciliacionResponse obtenerConciliacion(Long empresaId, LocalDate desde, LocalDate hasta);
    int autoConciliar(Long empresaId);
    void vincular(Long movimientoBancoId, Long transaccionId, Long empresaId);
    void desvincular(Long movimientoBancoId, Long empresaId);
    void conciliarSinTransaccion(Long movimientoBancoId, Long empresaId);
    void eliminarMovimiento(Long movimientoBancoId, Long empresaId);
    void importarCSV(Long empresaId, String csvContent);
}
