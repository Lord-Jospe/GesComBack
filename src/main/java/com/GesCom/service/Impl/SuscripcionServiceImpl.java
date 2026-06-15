package com.GesCom.service.Impl;

import com.GesCom.model.PlanSuscripcion;
import com.GesCom.model.Suscripcion;
import com.GesCom.repository.SuscripcionRepository;
import com.GesCom.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;

    @Override
    public PlanSuscripcion obtenerPlanActivo(Long empresaId) {
        return suscripcionRepository
                .findByEmpresa_EmpresaIdAndEstado(empresaId, "ACTIVA")
                .map(Suscripcion::getPlan)
                .orElseThrow(() -> new IllegalStateException(
                        "La empresa no tiene una suscripción activa"));
    }

    @Override
    public void verificarAccesoInventario(Long empresaId) {
        if (!obtenerPlanActivo(empresaId).isTieneInventario()) {
            throw new IllegalStateException(
                    "Tu plan actual no incluye el módulo de Inventario");
        }
    }

    @Override
    public void verificarAccesoNomina(Long empresaId) {
        if (!obtenerPlanActivo(empresaId).isTieneNomina()) {
            throw new IllegalStateException(
                    "Tu plan actual no incluye el módulo de Nómina");
        }
    }

    @Override
    public void verificarAccesoContabilidad(Long empresaId) {
        if (!obtenerPlanActivo(empresaId).isTieneContabilidad()) {
            throw new IllegalStateException(
                    "Tu plan actual no incluye el módulo de Contabilidad");
        }
    }

    @Override
    @Scheduled(cron = "0 3 2 * * *") // 2:03 AM todos los días
    public void marcarSuscripcionesVencidas() {
        List<Suscripcion> vencidas = suscripcionRepository
                .findByEstadoAndFechaVenceBefore("ACTIVA", LocalDate.now());
        vencidas.forEach(s -> s.setEstado("VENCIDA"));
        suscripcionRepository.saveAll(vencidas);
    }
}
