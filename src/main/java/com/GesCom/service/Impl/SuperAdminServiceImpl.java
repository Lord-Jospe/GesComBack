package com.GesCom.service.Impl;

import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminServiceImpl implements SuperAdminService {

    private final EmpresaRepository empresaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PlanSuscripcionRepository planSuscripcionRepository;
    private final AdjuntoRepository adjuntoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarEmpresas() {
        List<Empresa> empresas = empresaRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Empresa e : empresas) {
            Suscripcion s = suscripcionRepository
                    .findAllByEmpresa_EmpresaIdAndEstado(e.getEmpresaId(), "ACTIVA")
                    .stream().findFirst().orElse(null);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("empresaId", e.getEmpresaId());
            item.put("nombre", e.getNombre());
            item.put("rif", e.getRif());
            item.put("correo", e.getCorreo());
            item.put("monedaBase", e.getMonedaBase());
            if (s != null) {
                item.put("planNombre", s.getPlan().getNombre());
                item.put("planPrecio", s.getPlan().getPrecioUsd());
                item.put("fechaInicio", s.getFechaInicio());
                item.put("fechaVence", s.getFechaVence());
                item.put("estadoSuscripcion", s.getEstado());
            } else {
                item.put("planNombre", "Sin plan");
                item.put("estadoSuscripcion", "SIN_PLAN");
            }
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public void actualizarSuscripcion(Long empresaId, LocalDate fechaVence, Long planId, String estado) {
        Suscripcion s = suscripcionRepository
                .findAllByEmpresa_EmpresaIdAndEstado(empresaId, "ACTIVA")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No hay suscripción activa para la empresa " + empresaId));

        if (fechaVence != null) s.setFechaVence(fechaVence);
        if (planId != null) {
            PlanSuscripcion plan = planSuscripcionRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
            s.setPlan(plan);
        }
        if (estado != null) s.setEstado(estado);

        suscripcionRepository.save(s);
        log.info("Suscripción actualizada: empresa={}, fechaVence={}, planId={}, estado={}", empresaId, fechaVence, planId, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarComprobantes() {
        List<Adjunto> adjuntos = adjuntoRepository.findByTransaccionIsNullOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Adjunto a : adjuntos) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("adjuntoId", a.getAdjuntoId());
            item.put("nombreOriginal", a.getNombreOriginal());
            item.put("tipoArchivo", a.getTipoArchivo());
            item.put("tamanio", a.getTamanio());
            item.put("empresaId", a.getEmpresa() != null ? a.getEmpresa().getEmpresaId() : null);
            item.put("empresaNombre", a.getEmpresa() != null ? a.getEmpresa().getNombre() : null);
            item.put("createdAt", a.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> estadisticas() {
        return Map.of(
                "totalEmpresas", empresaRepository.count(),
                "suscripcionesActivas", suscripcionRepository.countByEstado("ACTIVA"),
                "suscripcionesVencidas", suscripcionRepository.countByEstado("VENCIDA")
        );
    }
}
