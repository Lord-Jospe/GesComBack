package com.GesCom.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SuperAdminService {
    List<Map<String, Object>> listarEmpresas();
    void actualizarSuscripcion(Long empresaId, LocalDate fechaVence, Long planId, String estado);
    List<Map<String, Object>> listarComprobantes();
    Map<String, Object> estadisticas();
}
