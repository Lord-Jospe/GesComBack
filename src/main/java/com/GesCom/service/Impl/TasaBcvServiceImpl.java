package com.GesCom.service.Impl;

import com.GesCom.dto.request.TasaBcvRequest;
import com.GesCom.dto.response.TasaBcvResponse;
import com.GesCom.model.Empresa;
import com.GesCom.model.TasaBcv;
import com.GesCom.model.Usuario;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.TasaBcvRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.service.TasaBcvService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TasaBcvServiceImpl implements TasaBcvService {

    private final TasaBcvRepository tasaBcvRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public TasaBcvResponse registrarTasa(TasaBcvRequest request, Long empresaId, Long usuarioId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Siempre crear nueva — permite múltiples tasas por día con distinta hora
        TasaBcv tasa = tasaBcvRepository.save(TasaBcv.builder()
                .empresa(empresa)
                .tasa(request.tasa())
                .fechaHora(request.fechaHora())
                .registradoPor(usuario)
                .build());

        log.info("Tasa BCV registrada: Bs. {} por USD — {} (usuario: {})",
                request.tasa(), request.fechaHora(), usuario.getPrimerNombre());

        return toResponse(tasa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TasaBcvResponse> historialTasas(Long empresaId) {
        return tasaBcvRepository.findByEmpresa_EmpresaIdOrderByFechaHoraDesc(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TasaBcvResponse obtenerTasaDelDia(Long empresaId, java.time.LocalDate fecha) {
        // Obtener la tasa más reciente para esa fecha (ignora hora)
        java.time.LocalDateTime inicio = fecha.atStartOfDay();
        java.time.LocalDateTime fin = fecha.atTime(23, 59, 59);
        return tasaBcvRepository.findTopByEmpresa_EmpresaIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                        empresaId, inicio, fin)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No hay tasa BCV registrada para la fecha " + fecha));
    }

    private TasaBcvResponse toResponse(TasaBcv t) {
        String nombreUsuario = t.getRegistradoPor().getPrimerNombre()
                + " " + t.getRegistradoPor().getPrimerApellido();
        return new TasaBcvResponse(
                t.getTasaId(),
                t.getTasa(),
                t.getFechaHora(),
                nombreUsuario
        );
    }
}
