package com.GesCom.service;

public interface FacturaPdfService {

    /**
     * Genera una factura en PDF y retorna los bytes del archivo.
     */
    byte[] generarFactura(Long transaccionId, Long empresaId);
}
