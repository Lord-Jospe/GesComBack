package com.GesCom.service;

import com.GesCom.model.PlanSuscripcion;

public interface SuscripcionService {
    PlanSuscripcion obtenerPlanActivo(Long empresaId);
    void verificarAccesoInventario(Long empresaId);
    void verificarAccesoNomina(Long empresaId);
    void verificarAccesoContabilidad(Long empresaId);
    void marcarSuscripcionesVencidas();
}
