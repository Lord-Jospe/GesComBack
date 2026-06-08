package com.GesCom.service.Impl;

import com.GesCom.dto.request.CrearProductoRequest;
import com.GesCom.dto.request.EditarProductoRequest;
import com.GesCom.dto.request.RegistrarMovimientoRequest;
import com.GesCom.dto.response.MovimientoInventarioResponse;
import com.GesCom.dto.response.PageResponse;
import com.GesCom.dto.response.ProductoResponse;
import com.GesCom.enums.TipoMovimientoInventario;
import com.GesCom.model.Empresa;
import com.GesCom.model.MovimientoInventario;
import com.GesCom.model.Producto;
import com.GesCom.model.Usuario;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.MovimientoInventarioRepository;
import com.GesCom.repository.ProductoRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.service.InventarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override @Transactional
    public ProductoResponse crearProducto(CrearProductoRequest request, Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Producto p = Producto.builder()
                .empresa(empresa)
                .codigo(request.codigo())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .categoria(request.categoria())
                .unidadMedida(request.unidadMedida())
                .costoUnitario(request.costoUnitario() != null ? request.costoUnitario() : BigDecimal.ZERO)
                .precioVenta(request.precioVenta())
                .stockActual(request.stockInicial() != null ? request.stockInicial() : BigDecimal.ZERO)
                .stockMinimo(request.stockMinimo() != null ? request.stockMinimo() : new BigDecimal("5"))
                .ventaBajoPedido(request.ventaBajoPedido())
                .isActive(true)
                .build();

        p = productoRepository.save(p);
        log.info("Producto creado: id={}, nombre={}, stock={}", p.getProductoId(), p.getNombre(), p.getStockActual());

        // Si hay stock inicial, registrar movimiento de entrada
        if (request.stockInicial() != null && request.stockInicial().compareTo(BigDecimal.ZERO) > 0) {
            movimientoRepository.save(MovimientoInventario.builder()
                    .producto(p).tipo(TipoMovimientoInventario.ENTRADA)
                    .cantidad(request.stockInicial()).motivo("Stock inicial").build());
        }
        return toResponse(p);
    }

    @Override @Transactional
    public ProductoResponse editarProducto(Long id, EditarProductoRequest request, Long empresaId) {
        Producto p = buscar(id, empresaId);
        if (request.codigo() != null) p.setCodigo(request.codigo());
        if (request.nombre() != null) p.setNombre(request.nombre());
        if (request.descripcion() != null) p.setDescripcion(request.descripcion());
        if (request.categoria() != null) p.setCategoria(request.categoria());
        if (request.unidadMedida() != null) p.setUnidadMedida(request.unidadMedida());
        if (request.costoUnitario() != null) p.setCostoUnitario(request.costoUnitario());
        if (request.precioVenta() != null) p.setPrecioVenta(request.precioVenta());
        if (request.stockMinimo() != null) p.setStockMinimo(request.stockMinimo());
        if (request.ventaBajoPedido() != null) p.setVentaBajoPedido(request.ventaBajoPedido());
        log.info("Producto editado: id={}", id);
        return toResponse(productoRepository.save(p));
    }

    @Override @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id, Long empresaId) {
        return toResponse(buscar(id, empresaId));
    }

    @Override @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerTodos(Long empresaId) {
        return productoRepository.findAllByEmpresa_EmpresaId(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> obtenerPaginado(Long empresaId, int pagina, int tamano) {
        var page = productoRepository.findByEmpresa_EmpresaId(empresaId, PageRequest.of(pagina, tamano));
        return PageResponse.<ProductoResponse>builder()
                .contenido(page.getContent().stream().map(this::toResponse).toList())
                .paginaActual(page.getNumber()).totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements()).tamano(page.getSize()).esUltima(page.isLast())
                .build();
    }

    @Override @Transactional
    public void desactivarProducto(Long id, Long empresaId) {
        Producto p = buscar(id, empresaId);
        p.setActive(false);
        productoRepository.save(p);
        log.info("Producto desactivado: id={}", id);
    }

    @Override @Transactional
    public void activarProducto(Long id, Long empresaId) {
        Producto p = buscar(id, empresaId);
        p.setActive(true);
        productoRepository.save(p);
        log.info("Producto activado: id={}", id);
    }

    @Override @Transactional(readOnly = true)
    public List<ProductoResponse> stockCritico(Long empresaId) {
        return productoRepository.findStockCritico(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public BigDecimal valorTotalInventario(Long empresaId) {
        return productoRepository.valorTotalInventario(empresaId);
    }

    @Override @Transactional
    public MovimientoInventarioResponse registrarMovimiento(RegistrarMovimientoRequest request, Long empresaId, Long usuarioId) {
        Producto p = buscar(request.productoId(), empresaId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Validar stock no negativo
        if ((request.tipo() == TipoMovimientoInventario.SALIDA || request.tipo() == TipoMovimientoInventario.MERMA)
                && p.getStockActual().subtract(request.cantidad()).compareTo(BigDecimal.ZERO) < 0
                && !p.isVentaBajoPedido()) {
            throw new IllegalStateException(
                    "Stock insuficiente. Disponible: " + p.getStockActual() + " " + p.getUnidadMedida()
                    + ". Activa 'Venta bajo pedido' para permitir stock negativo.");
        }

        MovimientoInventario mov = MovimientoInventario.builder()
                .producto(p).tipo(request.tipo())
                .cantidad(request.cantidad()).costoUnitario(request.costoUnitario())
                .motivo(request.motivo()).registradoPor(usuario)
                .build();

        BigDecimal nuevoStock = p.getStockActual();
        if (request.tipo() == TipoMovimientoInventario.ENTRADA) {
            nuevoStock = nuevoStock.add(request.cantidad());
        } else {
            nuevoStock = nuevoStock.subtract(request.cantidad());
        }
        p.setStockActual(nuevoStock);
        productoRepository.save(p);
        movimientoRepository.save(mov);

        log.info("Movimiento: producto={}, tipo={}, cant={}, stock={}, usuario={}",
                p.getNombre(), request.tipo(), request.cantidad(), nuevoStock, usuario.getPrimerNombre());
        return toMovResponse(mov);
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<MovimientoInventarioResponse> todosMovimientos(Long empresaId, int pagina, int tamano,
                                                                       TipoMovimientoInventario tipo, LocalDate desde, LocalDate hasta) {
        var pageable = org.springframework.data.domain.PageRequest.of(pagina, tamano);
        org.springframework.data.domain.Page<MovimientoInventario> page;

        if (tipo != null && desde != null && hasta != null) {
            page = movimientoRepository.findByProducto_Empresa_EmpresaIdAndTipoAndCreatedAtBetweenOrderByCreatedAtDesc(
                    empresaId, tipo, desde.atStartOfDay(), hasta.atTime(23,59,59), pageable);
        } else if (tipo != null) {
            page = movimientoRepository.findByProducto_Empresa_EmpresaIdAndTipoOrderByCreatedAtDesc(empresaId, tipo, pageable);
        } else if (desde != null && hasta != null) {
            page = movimientoRepository.findByProducto_Empresa_EmpresaIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    empresaId, desde.atStartOfDay(), hasta.atTime(23,59,59), pageable);
        } else {
            page = movimientoRepository.findByProducto_Empresa_EmpresaIdOrderByCreatedAtDesc(empresaId, pageable);
        }

        return PageResponse.<MovimientoInventarioResponse>builder()
                .contenido(page.getContent().stream().map(this::toMovResponse).toList())
                .paginaActual(page.getNumber()).totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements()).tamano(page.getSize()).esUltima(page.isLast())
                .build();
    }

    @Override @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> historialMovimientos(Long productoId, Long empresaId) {
        buscar(productoId, empresaId);
        return movimientoRepository.findByProducto_ProductoIdOrderByCreatedAtDesc(productoId)
                .stream().map(this::toMovResponse).toList();
    }

    private Producto buscar(Long id, Long empresaId) {
        return productoRepository.findByProductoIdAndEmpresa_EmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    }

    private ProductoResponse toResponse(Producto p) {
        String alerta = null;
        if (p.getStockActual().compareTo(BigDecimal.ZERO) == 0) {
            alerta = "ROJO";
        } else if (p.getStockActual().compareTo(p.getStockMinimo()) <= 0) {
            alerta = "AMARILLO";
        }
        BigDecimal valorTotal = p.getStockActual() != null && p.getCostoUnitario() != null
                ? p.getStockActual().multiply(p.getCostoUnitario()) : BigDecimal.ZERO;

        return ProductoResponse.builder()
                .productoId(p.getProductoId()).codigo(p.getCodigo()).nombre(p.getNombre())
                .descripcion(p.getDescripcion()).categoria(p.getCategoria())
                .unidadMedida(p.getUnidadMedida() != null ? p.getUnidadMedida().name() : null)
                .costoUnitario(p.getCostoUnitario()).precioVenta(p.getPrecioVenta())
                .stockActual(p.getStockActual()).stockMinimo(p.getStockMinimo())
                .ventaBajoPedido(p.isVentaBajoPedido()).alertaStock(alerta)
                .valorTotal(valorTotal).activo(p.isActive()).createdAt(p.getCreatedAt())
                .build();
    }

    private MovimientoInventarioResponse toMovResponse(MovimientoInventario m) {
        String nombreRegistrador = m.getRegistradoPor() != null
                ? m.getRegistradoPor().getPrimerNombre() + " " + m.getRegistradoPor().getPrimerApellido()
                : null;
        return MovimientoInventarioResponse.builder()
                .movimientoId(m.getMovimientoId())
                .productoId(m.getProducto().getProductoId())
                .productoNombre(m.getProducto().getNombre())
                .tipo(m.getTipo().name()).cantidad(m.getCantidad())
                .costoUnitario(m.getCostoUnitario()).motivo(m.getMotivo())
                .transaccionId(m.getTransaccionId()).registradoPor(nombreRegistrador)
                .createdAt(m.getCreatedAt())
                .build();
    }
}
