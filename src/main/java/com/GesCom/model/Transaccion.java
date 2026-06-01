package com.GesCom.model;

import com.GesCom.enums.EstadoTransaccion;
import com.GesCom.enums.MetodoPago;
import com.GesCom.enums.TipoTransaccion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transaccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaccion_id")
    private Long transaccionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoTransaccion tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(name = "numero_factura", length = 30)
    private String numeroFactura;

    @Column(name = "transaccion_origen_id")
    private Long transaccionOrigenId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 5)
    private String moneda;

    @Column(name = "tasa_bcv_usada", precision = 12, scale = 4)
    private BigDecimal tasaBcvUsada;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "iva_porcentaje", precision = 5, scale = 2)
    private BigDecimal ivaPorcentaje;

    @Column(name = "iva_monto", precision = 14, scale = 2)
    private BigDecimal ivaMonto;

    @Column(name = "igtf_aplica", nullable = false)
    private boolean igtfAplica;

    @Column(name = "igtf_monto", precision = 14, scale = 2)
    private BigDecimal igtfMonto;

    @Column(name = "descuento_global_porcentaje", precision = 5, scale = 2)
    private BigDecimal descuentoGlobalPorcentaje;

    @Column(name = "descuento_global_monto", precision = 14, scale = 2)
    private BigDecimal descuentoGlobalMonto;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "total_usd", precision = 14, scale = 2)
    private BigDecimal totalUsd;

    @Column(name = "total_ves", precision = 14, scale = 2)
    private BigDecimal totalVes;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoTransaccion estado;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Column(length = 500)
    private String notas;

    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransaccionLinea> lineas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
