package com.GesCom.service;

import com.GesCom.dto.request.CrearUsuarioRequest;
import com.GesCom.dto.request.EditarUsuarioRequest;
import com.GesCom.dto.request.UsuarioFiltroRequest;
import com.GesCom.dto.response.UsuarioPageResponse;
import com.GesCom.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    UsuarioResponse crearUsuario(CrearUsuarioRequest request, Long empresaId);
    List<UsuarioResponse> obtenerTodos(Long empresaId);
    UsuarioResponse editarUsuario(Long id, EditarUsuarioRequest request, Long empresaId);
    void desactivarUsuario(Long id, Long empresaId);
    void activarUsuario(Long id, Long empresaId);

    UsuarioResponse obtenerPorId(Long id, Long empresaId);
    UsuarioPageResponse obtenerPaginado(Long empresaId, UsuarioFiltroRequest filtro);

}
