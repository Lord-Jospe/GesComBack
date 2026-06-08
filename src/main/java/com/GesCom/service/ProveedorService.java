package com.GesCom.service;

import com.GesCom.dto.request.CrearProveedorRequest;
import com.GesCom.dto.request.EditarProveedorRequest;
import com.GesCom.dto.response.PageResponse;
import com.GesCom.dto.response.ProveedorResponse;

import java.util.List;

public interface ProveedorService {
    ProveedorResponse crearProveedor(CrearProveedorRequest request, Long empresaId);
    ProveedorResponse editarProveedor(Long id, EditarProveedorRequest request, Long empresaId);
    ProveedorResponse obtenerPorId(Long id, Long empresaId);
    List<ProveedorResponse> obtenerTodos(Long empresaId);
    PageResponse<ProveedorResponse> obtenerPaginado(Long empresaId, int pagina, int tamano);
    void desactivarProveedor(Long id, Long empresaId);
    void activarProveedor(Long id, Long empresaId);
}
