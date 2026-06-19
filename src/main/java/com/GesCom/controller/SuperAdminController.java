package com.GesCom.controller;

import com.GesCom.dto.response.SuscripcionResponse;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.security.user.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final EmpresaRepository empresaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PlanSuscripcionRepository planSuscripcionRepository;
    private final AdjuntoRepository adjuntoRepository;

    @GetMapping("/empresas")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listarEmpresas() {
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
        return ResponseEntity.ok(result);
    }

    @PostMapping("/suscripcion/{empresaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> extenderSuscripcion(
            @PathVariable Long empresaId,
            @RequestBody Map<String, Object> body) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Suscripcion s = suscripcionRepository
                .findAllByEmpresa_EmpresaIdAndEstado(empresaId, "ACTIVA")
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No hay suscripción activa para esta empresa"));

        if (body.containsKey("fechaVence")) {
            s.setFechaVence(LocalDate.parse((String) body.get("fechaVence")));
        }
        if (body.containsKey("planId")) {
            Long planId = ((Number) body.get("planId")).longValue();
            PlanSuscripcion plan = planSuscripcionRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
            s.setPlan(plan);
        }
        if (body.containsKey("estado")) {
            s.setEstado((String) body.get("estado"));
        }

        suscripcionRepository.save(s);
        return ResponseEntity.ok(Map.of("mensaje", "Suscripción actualizada"));
    }

    @GetMapping("/comprobantes")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listarComprobantes() {
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
        return ResponseEntity.ok(result);
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        long totalEmpresas = empresaRepository.count();
        long suscripcionesActivas = suscripcionRepository.countByEstado("ACTIVA");
        long suscripcionesVencidas = suscripcionRepository.countByEstado("VENCIDA");

        return ResponseEntity.ok(Map.of(
                "totalEmpresas", totalEmpresas,
                "suscripcionesActivas", suscripcionesActivas,
                "suscripcionesVencidas", suscripcionesVencidas
        ));
    }
}
