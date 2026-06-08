package com.GesCom.service;

import com.GesCom.dto.response.AdjuntoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdjuntoService {

    AdjuntoResponse subir(Long transaccionId, MultipartFile archivo, Long empresaId);

    List<AdjuntoResponse> listar(Long transaccionId, Long empresaId);

    byte[] descargar(Long adjuntoId, Long empresaId);

    void eliminar(Long adjuntoId, Long empresaId);
}
