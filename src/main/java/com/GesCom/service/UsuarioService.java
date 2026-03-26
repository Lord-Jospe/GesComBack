package com.GesCom.service;

import com.GesCom.dto.request.CrearUsuarioRequest;
import com.GesCom.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    UsuarioResponse crearUsuario(CrearUsuarioRequest request, Long empresaId);
    List<UsuarioResponse> obtenerTodos(Long empresaId);
    UsuarioResponse editarUsuario(Long id, CrearUsuarioRequest request, Long empresaId);
    void desactivarUsuario(Long id, Long empresaId);
}
