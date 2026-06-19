package com.GesCom.service.Impl;

import com.GesCom.model.PlanSuscripcion;
import com.GesCom.model.Suscripcion;
import com.GesCom.repository.AdjuntoRepository;
import com.GesCom.repository.SuscripcionRepository;
import com.GesCom.repository.TransaccionRepository;
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
    private final TransaccionRepository transaccionRepository;
    private final AdjuntoRepository adjuntoRepository;

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
    public void verificarLimiteTransacciones(Long empresaId) {
        PlanSuscripcion plan = obtenerPlanActivo(empresaId);
        Integer max = plan.getMaxTransaccionesMes();
        if (max == null) return;

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        long count = transaccionRepository
                .findByEmpresa_EmpresaIdAndFechaBetween(empresaId, inicioMes, LocalDate.now())
                .stream().filter(t -> t.getEstado() != com.GesCom.enums.EstadoTransaccion.ANULADA)
                .count();

        if (count >= max) {
            throw new IllegalStateException(
                    "Límite de transacciones alcanzado (" + max + "/mes). Actualiza tu plan.");
        }
    }

    @Override
    public void verificarLimiteArchivos(Long empresaId) {
        PlanSuscripcion plan = obtenerPlanActivo(empresaId);
        Integer max = plan.getMaxArchivosMes();
        if (max == null) return;

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        long count = adjuntoRepository.findByEmpresa_EmpresaIdOrderByCreatedAtDesc(empresaId)
                .stream().filter(a -> !a.getCreatedAt().toLocalDate().isBefore(inicioMes))
                .count();

        if (count >= max) {
            throw new IllegalStateException(
                    "Límite de archivos alcanzado (" + max + "/mes). Actualiza tu plan.");
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
