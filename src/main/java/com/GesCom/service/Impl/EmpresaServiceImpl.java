package com.GesCom.service.Impl;

import com.GesCom.dto.request.EditarEmpresaRequest;
import com.GesCom.dto.request.MonedaRequest;
import com.GesCom.dto.response.EmpresaResponse;
import com.GesCom.model.Empresa;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.service.EmpresaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPerfil(Long empresaId) {
        return toResponse(buscarEmpresa(empresaId));
    }

    @Override
    @Transactional
    public EmpresaResponse editarPerfil(Long empresaId, EditarEmpresaRequest request) {
        Empresa empresa = buscarEmpresa(empresaId);

        if (request.nombre()    != null) empresa.setNombre(request.nombre());
        if (request.rif()       != null) {
            if (empresaRepository.existsByRif(request.rif())
                    && !request.rif().equals(empresa.getRif())) {
                throw new IllegalArgumentException("El RIF ya está registrado");
            }
            empresa.setRif(request.rif());
        }

        if (request.correo() != null
                && !request.correo().equalsIgnoreCase(empresa.getCorreo())) {
            if (empresaRepository.existsByCorreo(request.correo())) {
                throw new IllegalArgumentException("El correo ya está registrado por otra empresa");
            }
            if (usuarioRepository.existsByEmail(request.correo())) {
                throw new IllegalArgumentException("El correo ya está en uso por un usuario");
            }
            empresa.setCorreo(request.correo());
        }

        if (request.direccion() != null) empresa.setDireccion(request.direccion());
        if (request.telefono()  != null) empresa.setTelefono(request.telefono());
        if (request.actividad() != null) empresa.setActividad(request.actividad());
        if (request.logoUrl()   != null) empresa.setLogoUrl(request.logoUrl());

        // ─── Configuración fiscal ─────────────────────────────────
        if (request.ivaActivo()      != null) empresa.setIvaActivo(request.ivaActivo());
        if (request.ivaPorcentaje()  != null) empresa.setIvaPorcentaje(request.ivaPorcentaje());
        if (request.igtfActivo()     != null) empresa.setIgtfActivo(request.igtfActivo());

        // ─── Numeración de facturas ───────────────────────────────
        if (request.facturaPrefijo()         != null) empresa.setFacturaPrefijo(request.facturaPrefijo());
        if (request.facturaSiguienteNumero() != null) empresa.setFacturaSiguienteNumero(request.facturaSiguienteNumero());

        return toResponse(empresaRepository.save(empresa));
    }

    @Override
    @Transactional
    public void cambiarMoneda(Long empresaId, MonedaRequest request) {
        Empresa empresa = buscarEmpresa(empresaId);
        empresa.setMonedaBase(request.moneda());
        empresaRepository.save(empresa);
    }

    private Empresa buscarEmpresa(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
    }

    private EmpresaResponse toResponse(Empresa e) {
        return new EmpresaResponse(
                e.getEmpresaId(),
                e.getNombre(),
                e.getRif(),
                e.getCorreo(),
                e.getTelefono(),
                e.getDireccion(),
                e.getLogoUrl(),
                e.getActividad(),
                e.getMonedaBase(),
                e.isActive(),
                e.isIvaActivo(),
                e.getIvaPorcentaje(),
                e.isIgtfActivo(),
                e.getFacturaPrefijo(),
                e.getFacturaSiguienteNumero(),
                e.getCreatedAt()
        );
    }
}
