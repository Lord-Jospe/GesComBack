package com.GesCom.service.Impl;

import com.GesCom.dto.request.CrearProveedorRequest;
import com.GesCom.dto.request.EditarProveedorRequest;
import com.GesCom.dto.response.PageResponse;
import com.GesCom.dto.response.ProveedorResponse;
import com.GesCom.model.Empresa;
import com.GesCom.model.Proveedor;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.ProveedorRepository;
import com.GesCom.service.ProveedorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final EmpresaRepository empresaRepository;


    @Override
    @Transactional
    public ProveedorResponse crearProveedor(CrearProveedorRequest request, Long empresaId) {
        if (proveedorRepository.existsByRif(request.rif())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese RIF");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Proveedor proveedor = proveedorRepository.save(
                Proveedor.builder()
                        .empresa(empresa)
                        .nombre(request.nombre())
                        .rif(request.rif())
                        .email(request.email())
                        .telefono(request.telefono())
                        .categoria(request.categoria())
                        .isActive(true)
                        .build()
        );
        return toResponse(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponse editarProveedor(Long id, EditarProveedorRequest request, Long empresaId) {
        Proveedor proveedor = buscar(id, empresaId);

        if (request.rif() != null
                && !request.rif().equals(proveedor.getRif())
                && proveedorRepository.existsByRif(request.rif())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese RIF");
        }

        if (request.nombre()    != null) proveedor.setNombre(request.nombre());
        if (request.rif()       != null) proveedor.setRif(request.rif());
        if (request.email()    != null) proveedor.setEmail(request.email());
        if (request.telefono()  != null) proveedor.setTelefono(request.telefono());
        if (request.categoria() != null) proveedor.setCategoria(request.categoria());

        return toResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Long id, Long empresaId) {
        return toResponse(buscar(id, empresaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponse> obtenerTodos(Long empresaId) {
        return proveedorRepository.findAllByEmpresa_EmpresaId(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void desactivarProveedor(Long id, Long empresaId) {
        Proveedor proveedor = buscar(id, empresaId);
        if (!proveedor.isActive()) {
            throw new IllegalStateException("El proveedor ya está desactivado");
        }
        proveedor.setActive(false);
        proveedorRepository.save(proveedor);
        log.info("Proveedor desactivado: id={}, nombre={}", id, proveedor.getNombre());
    }

    @Override
    @Transactional
    public void activarProveedor(Long id, Long empresaId) {
        Proveedor proveedor = buscar(id, empresaId);
        if (proveedor.isActive()) {
            throw new IllegalStateException("El proveedor ya está activo");
        }
        proveedor.setActive(true);
        proveedorRepository.save(proveedor);
        log.info("Proveedor activado: id={}, nombre={}", id, proveedor.getNombre());
    }


    @Override
    public PageResponse<ProveedorResponse> obtenerPaginado(Long empresaId, int pagina, int tamano) {
        var page = proveedorRepository
                .findByEmpresa_EmpresaId(empresaId,
                        org.springframework.data.domain.PageRequest.of(pagina, tamano));
        return PageResponse.<ProveedorResponse>builder()
                .contenido(page.getContent().stream().map(this::toResponse).toList())
                .paginaActual(page.getNumber())
                .totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements())
                .tamano(page.getSize())
                .esUltima(page.isLast())
                .build();
    }

    private Proveedor buscar(Long id, Long empresaId) {
        return proveedorRepository
                .findByProveedorIdAndEmpresa_EmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
    }

    private ProveedorResponse toResponse(Proveedor p) {
        return new ProveedorResponse(
                p.getProveedorId(),
                p.getNombre(),
                p.getRif(),
                p.getEmail(),
                p.getTelefono(),
                p.getCategoria().name(),
                p.isActive(),
                p.getCreatedAt()
        );
    }
}
