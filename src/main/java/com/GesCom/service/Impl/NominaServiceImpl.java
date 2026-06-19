package com.GesCom.service.Impl;

import com.GesCom.dto.request.CalcularNominaRequest;
import com.GesCom.dto.request.ConceptoExtraRequest;
import com.GesCom.dto.response.ConceptoNominaResponse;
import com.GesCom.dto.response.NominaResponse;
import com.GesCom.enums.TipoConcepto;
import com.GesCom.model.*;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.NominaRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.service.NominaService;
import com.GesCom.service.SuscripcionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NominaServiceImpl implements NominaService {

    // Deducciones legales venezolanas (RF-58)
    // Las deducciones ahora vienen de la configuración de la empresa (ajustes del sistema)

    private final NominaRepository nominaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final SuscripcionService suscripcionService;

    @Override @Transactional
    public NominaResponse calcularNomina(CalcularNominaRequest request, Long empresaId) {
        suscripcionService.verificarAccesoNomina(empresaId);
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Usuario empleado = usuarioRepository.findById(request.usuarioId())
                .filter(u -> u.getEmpresa().getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado"));

        if (empleado.getSueldo() == null || empleado.getSueldo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El empleado no tiene un sueldo asignado");
        }

        BigDecimal salarioBase = empleado.getSueldo();
        List<ConceptoNomina> conceptos = new ArrayList<>();

        // Salario base (asignación)
        conceptos.add(ConceptoNomina.builder().tipo(TipoConcepto.ASIGNACION)
                .descripcion("Salario base").monto(salarioBase).build());

        // Conceptos extras (bonos, horas extra, comisiones)
        BigDecimal totalExtras = BigDecimal.ZERO;
        if (request.extras() != null) {
            for (ConceptoExtraRequest extra : request.extras()) {
                conceptos.add(ConceptoNomina.builder()
                        .tipo(extra.tipo()).descripcion(extra.descripcion()).monto(extra.monto()).build());
                if (extra.tipo() == TipoConcepto.ASIGNACION) {
                    totalExtras = totalExtras.add(extra.monto());
                }
            }
        }

        // Cálculo de deducciones legales desde configuración de la empresa
        BigDecimal ssoPct = empresa.getSsoPorcentaje() != null ? empresa.getSsoPorcentaje() : new BigDecimal("4.00");
        BigDecimal incesPct = empresa.getIncesPorcentaje() != null ? empresa.getIncesPorcentaje() : new BigDecimal("0.50");
        BigDecimal faovPct = empresa.getFaovPorcentaje() != null ? empresa.getFaovPorcentaje() : new BigDecimal("1.00");

        BigDecimal sso   = salarioBase.multiply(ssoPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal inces = salarioBase.multiply(incesPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal faov  = salarioBase.multiply(faovPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        conceptos.add(ConceptoNomina.builder().tipo(TipoConcepto.DEDUCCION)
                .descripcion("SSO (" + ssoPct + "%)").monto(sso).build());
        conceptos.add(ConceptoNomina.builder().tipo(TipoConcepto.DEDUCCION)
                .descripcion("INCES (" + incesPct + "%)").monto(inces).build());
        conceptos.add(ConceptoNomina.builder().tipo(TipoConcepto.DEDUCCION)
                .descripcion("FAOV (" + faovPct + "%)").monto(faov).build());

        // Totales
        BigDecimal totalAsignaciones = salarioBase.add(totalExtras);
        BigDecimal totalDeducciones = conceptos.stream()
                .filter(c -> c.getTipo() == TipoConcepto.DEDUCCION)
                .map(ConceptoNomina::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal salarioNeto = totalAsignaciones.subtract(totalDeducciones);

        // Guardar nómina
        Nomina nomina = Nomina.builder()
                .empresa(empresa).usuario(empleado)
                .periodoInicio(request.periodoInicio()).periodoFin(request.periodoFin())
                .salarioBase(salarioBase).totalAsignaciones(totalAsignaciones)
                .totalDeducciones(totalDeducciones).salarioNeto(salarioNeto)
                .estado("CALCULADA").notas(request.notas())
                .build();

        for (ConceptoNomina c : conceptos) { c.setNomina(nomina); }
        nomina.setConceptos(conceptos);
        nominaRepository.save(nomina);

        log.info("Nómina calculada: empleado={}, periodo={}/{}, neto={}",
                empleado.getPrimerNombre(), request.periodoInicio(), request.periodoFin(), salarioNeto);

        return toResponse(nomina);
    }

    @Override @Transactional(readOnly = true)
    public NominaResponse obtenerPorId(Long id, Long empresaId) {
        return toResponse(buscar(id, empresaId));
    }

    @Override @Transactional(readOnly = true)
    public List<NominaResponse> listarPorEmpresa(Long empresaId) {
        return nominaRepository.findByEmpresa_EmpresaIdOrderByCreatedAtDesc(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<NominaResponse> listarPorEmpleado(Long usuarioId, Long empresaId) {
        return nominaRepository.findByUsuario_UsuarioIdOrderByPeriodoInicioDesc(usuarioId)
                .stream().filter(n -> n.getEmpresa().getEmpresaId().equals(empresaId))
                .map(this::toResponse).toList();
    }

    @Override @Transactional
    public void marcarPagada(Long id, Long empresaId) {
        Nomina n = buscar(id, empresaId);
        n.setEstado("PAGADA");
        nominaRepository.save(n);
        log.info("Nómina marcada como pagada: id={}", id);
    }

    @Override @Transactional
    public void anular(Long id, Long empresaId) {
        Nomina n = buscar(id, empresaId);
        n.setEstado("ANULADA");
        nominaRepository.save(n);
        log.info("Nómina anulada: id={}", id);
    }

    private Nomina buscar(Long id, Long empresaId) {
        return nominaRepository.findByNominaIdAndEmpresa_EmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Nómina no encontrada"));
    }

    private NominaResponse toResponse(Nomina n) {
        List<ConceptoNominaResponse> conceptos = n.getConceptos().stream()
                .map(c -> ConceptoNominaResponse.builder()
                        .conceptoId(c.getConceptoId()).tipo(c.getTipo().name())
                        .descripcion(c.getDescripcion()).monto(c.getMonto()).build())
                .toList();

        return NominaResponse.builder()
                .nominaId(n.getNominaId())
                .usuarioId(n.getUsuario().getUsuarioId())
                .nombreEmpleado(n.getUsuario().getPrimerNombre() + " " + n.getUsuario().getPrimerApellido())
                .periodoInicio(n.getPeriodoInicio()).periodoFin(n.getPeriodoFin())
                .salarioBase(n.getSalarioBase()).totalAsignaciones(n.getTotalAsignaciones())
                .totalDeducciones(n.getTotalDeducciones()).salarioNeto(n.getSalarioNeto())
                .estado(n.getEstado()).notas(n.getNotas())
                .conceptos(conceptos).createdAt(n.getCreatedAt())
                .build();
    }
}
