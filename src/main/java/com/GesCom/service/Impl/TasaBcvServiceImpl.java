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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
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

        // Si ya existe una tasa para esa fecha, reemplazarla
        tasaBcvRepository.findByEmpresa_EmpresaIdAndFecha(empresaId, request.fecha())
                .ifPresent(t -> tasaBcvRepository.delete(t));

        TasaBcv tasa = tasaBcvRepository.save(TasaBcv.builder()
                .empresa(empresa)
                .tasa(request.tasa())
                .fecha(request.fecha())
                .registradoPor(usuario)
                .build());

        return toResponse(tasa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TasaBcvResponse> historialTasas(Long empresaId) {
        return tasaBcvRepository.findByEmpresa_EmpresaIdOrderByFechaDesc(empresaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TasaBcvResponse obtenerTasaDelDia(Long empresaId, LocalDate fecha) {
        return tasaBcvRepository.findByEmpresa_EmpresaIdAndFecha(empresaId, fecha)
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
                t.getFecha(),
                nombreUsuario
        );
    }
}
