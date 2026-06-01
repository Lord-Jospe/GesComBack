package com.GesCom.service;

import com.GesCom.dto.request.TasaBcvRequest;
import com.GesCom.dto.response.TasaBcvResponse;

import java.time.LocalDate;
import java.util.List;

public interface TasaBcvService {

    TasaBcvResponse registrarTasa(TasaBcvRequest request, Long empresaId, Long usuarioId);

    List<TasaBcvResponse> historialTasas(Long empresaId);

    TasaBcvResponse obtenerTasaDelDia(Long empresaId, LocalDate fecha);
}
