package com.GesCom.service;

import com.GesCom.dto.request.CrearClienteRequest;
import com.GesCom.dto.request.EditarClienteRequest;
import com.GesCom.dto.response.ClienteResponse;
import com.GesCom.dto.response.PageResponse;

import java.util.List;

public interface ClienteService {
    ClienteResponse crearCliente(CrearClienteRequest request, Long empresaId);
    ClienteResponse editarCliente(Long id, EditarClienteRequest request, Long empresaId);
    ClienteResponse obtenerPorId(Long id, Long empresaId);
    List<ClienteResponse> obtenerTodos(Long empresaId);
    PageResponse<ClienteResponse> obtenerPaginado(Long empresaId, int pagina, int tamano);
    void desactivarCliente(Long id, Long empresaId);
    void activarCliente(Long id, Long empresaId);
}
