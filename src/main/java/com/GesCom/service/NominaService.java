package com.GesCom.service;

import com.GesCom.dto.request.CalcularNominaRequest;
import com.GesCom.dto.response.NominaResponse;

import java.util.List;

public interface NominaService {
    NominaResponse calcularNomina(CalcularNominaRequest request, Long empresaId);
    NominaResponse obtenerPorId(Long id, Long empresaId);
    List<NominaResponse> listarPorEmpresa(Long empresaId);
    List<NominaResponse> listarPorEmpleado(Long usuarioId, Long empresaId);
    void marcarPagada(Long id, Long empresaId);
    void anular(Long id, Long empresaId);
}
