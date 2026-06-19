package com.GesCom.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    byte[] obtenerQR() throws IOException;
    void subirQR(MultipartFile file) throws IOException;
    Map<String, String> subirComprobante(MultipartFile file, String planSolicitado, Long empresaId) throws IOException;
    List<Map<String, Object>> misComprobantes(Long empresaId);
    byte[] descargarComprobante(Long comprobanteId) throws IOException;
    List<Map<String, Object>> comprobantesPendientes();
    void revisarComprobante(Long comprobanteId, String estado, String notas);
    List<Map<String, Object>> todosComprobantes();
    Map<String, Object> estadisticas();
}
