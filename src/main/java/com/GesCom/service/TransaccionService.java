package com.GesCom.service;

import com.GesCom.dto.request.CrearTransaccionRequest;
import com.GesCom.dto.request.EditarTransaccionRequest;
import com.GesCom.dto.request.FiltroTransaccionRequest;
import com.GesCom.dto.response.TransaccionResponse;

import java.util.List;

public interface TransaccionService {

    TransaccionResponse crear(CrearTransaccionRequest request, Long empresaId);

    TransaccionResponse obtenerPorId(Long id, Long empresaId);

    List<TransaccionResponse> listar(Long empresaId, FiltroTransaccionRequest filtro);

    TransaccionResponse editar(Long id, EditarTransaccionRequest request, Long empresaId);

    void anular(Long id, String motivo, Long empresaId);

    List<TransaccionResponse> cuentasPorCobrar(Long empresaId);

    List<TransaccionResponse> cuentasPorPagar(Long empresaId);
}
