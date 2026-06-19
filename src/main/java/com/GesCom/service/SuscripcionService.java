package com.GesCom.service;

import com.GesCom.model.PlanSuscripcion;

public interface SuscripcionService {
    PlanSuscripcion obtenerPlanActivo(Long empresaId);
    void verificarAccesoInventario(Long empresaId);
    void verificarAccesoNomina(Long empresaId);
    void verificarAccesoContabilidad(Long empresaId);
    void verificarLimiteTransacciones(Long empresaId);
    void verificarLimiteArchivos(Long empresaId);
    void marcarSuscripcionesVencidas();
}
