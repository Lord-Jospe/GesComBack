package com.GesCom.service;

import com.GesCom.dto.request.CrearProductoRequest;
import com.GesCom.dto.request.EditarProductoRequest;
import com.GesCom.dto.request.RegistrarMovimientoRequest;
import com.GesCom.dto.response.MovimientoInventarioResponse;
import com.GesCom.dto.response.PageResponse;
import com.GesCom.dto.response.ProductoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface InventarioService {
    ProductoResponse crearProducto(CrearProductoRequest request, Long empresaId);
    ProductoResponse editarProducto(Long id, EditarProductoRequest request, Long empresaId);
    ProductoResponse obtenerPorId(Long id, Long empresaId);
    List<ProductoResponse> obtenerTodos(Long empresaId);
    PageResponse<ProductoResponse> obtenerPaginado(Long empresaId, int pagina, int tamano);
    void desactivarProducto(Long id, Long empresaId);
    void activarProducto(Long id, Long empresaId);

    List<ProductoResponse> stockCritico(Long empresaId);
    BigDecimal valorTotalInventario(Long empresaId);

    MovimientoInventarioResponse registrarMovimiento(RegistrarMovimientoRequest request, Long empresaId);
    List<MovimientoInventarioResponse> historialMovimientos(Long productoId, Long empresaId);
}
