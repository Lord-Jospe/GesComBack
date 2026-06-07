package com.GesCom.service.Impl;

import com.GesCom.dto.request.CrearClienteRequest;
import com.GesCom.dto.request.EditarClienteRequest;
import com.GesCom.dto.response.ClienteResponse;
import com.GesCom.dto.response.PageResponse;
import com.GesCom.model.Cliente;
import com.GesCom.model.Empresa;
import com.GesCom.repository.ClienteRepository;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.service.ClienteService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public ClienteResponse crearCliente(CrearClienteRequest request, Long empresaId) {

        if (clienteRepository.existsByRifCedula(request.rifCedula())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese RIF/cédula");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .empresa(empresa)
                        .tipoPersona(request.tipoPersona())
                        .nombre(request.nombre())
                        .rifCedula(request.rifCedula())
                        .correo(request.correo())
                        .telefono(request.telefono())
                        .direccion(request.direccion())
                        .isActive(true)
                        .build()
        );
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse editarCliente(Long id, EditarClienteRequest request, Long empresaId) {
        Cliente cliente = buscar(id, empresaId);

        if (request.rifCedula() != null
                && !request.rifCedula().equals(cliente.getRifCedula())
                && clienteRepository.existsByRifCedula(request.rifCedula())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese RIF/cédula");
        }

        if (request.tipoPersona() != null) cliente.setTipoPersona(request.tipoPersona());
        if (request.nombre()      != null) cliente.setNombre(request.nombre());
        if (request.rifCedula()   != null) cliente.setRifCedula(request.rifCedula());
        if (request.correo()      != null) cliente.setCorreo(request.correo());
        if (request.telefono()    != null) cliente.setTelefono(request.telefono());
        if (request.direccion()   != null) cliente.setDireccion(request.direccion());

        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id, Long empresaId) {
        return toResponse(buscar(id, empresaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerTodos(Long empresaId) {
        return clienteRepository.findAllByEmpresa_EmpresaId(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void desactivarCliente(Long id, Long empresaId) {
        Cliente cliente = buscar(id, empresaId);
        if (!cliente.isActive()) {
            throw new IllegalStateException("El cliente ya está desactivado");
        }
        cliente.setActive(false);
        clienteRepository.save(cliente);
        log.info("Cliente desactivado: id={}, nombre={}", id, cliente.getNombre());
    }

    @Override
    @Transactional
    public void activarCliente(Long id, Long empresaId) {
        Cliente cliente = buscar(id, empresaId);
        if (cliente.isActive()) {
            throw new IllegalStateException("El cliente ya está activo");
        }
        cliente.setActive(true);
        clienteRepository.save(cliente);
        log.info("Cliente activado: id={}, nombre={}", id, cliente.getNombre());
    }

    @Override
    public PageResponse<ClienteResponse> obtenerPaginado(Long empresaId, int pagina, int tamano) {
        var page = clienteRepository
                .findByEmpresa_EmpresaId(empresaId,
                        org.springframework.data.domain.PageRequest.of(pagina, tamano));
        return PageResponse.<ClienteResponse>builder()
                .contenido(page.getContent().stream().map(this::toResponse).toList())
                .paginaActual(page.getNumber())
                .totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements())
                .tamano(page.getSize())
                .esUltima(page.isLast())
                .build();
    }

    private Cliente buscar(Long id, Long empresaId) {
        return clienteRepository
                .findByClienteIdAndEmpresa_EmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getClienteId(),
                c.getTipoPersona().name(),
                c.getNombre(),
                c.getRifCedula(),
                c.getCorreo(),
                c.getTelefono(),
                c.getDireccion(),
                c.isActive(),
                c.getCreatedAt()
        );
    }
}
