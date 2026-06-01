package com.GesCom.service;

import com.GesCom.dto.request.EditarEmpresaRequest;
import com.GesCom.dto.request.MonedaRequest;
import com.GesCom.dto.response.EmpresaResponse;

public interface EmpresaService {
    EmpresaResponse obtenerPerfil(Long empresaId);
    EmpresaResponse editarPerfil(Long empresaId, EditarEmpresaRequest request);
    void cambiarMoneda(Long empresaId, MonedaRequest request);
}
