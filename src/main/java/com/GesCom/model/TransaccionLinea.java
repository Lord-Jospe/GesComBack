package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transaccion_linea")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "linea_id")
    private Long lineaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Transaccion transaccion;

    // Se enlazará a Producto en Fase 4 (Inventario)
    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "descuento_porcentaje", precision = 5, scale = 2)
    private BigDecimal descuentoPorcentaje;

    @Column(name = "descuento_monto", precision = 14, scale = 2)
    private BigDecimal descuentoMonto;

    @Column(name = "subtotal_linea", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalLinea;
}
