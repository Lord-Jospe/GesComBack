package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table( name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String rif;

    @Column(nullable = false, unique = true, length = 255)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 100)
    private String actividad;

    @Column(name = "moneda_base", length = 5)
    private String monedaBase = "USD";

    // ─── Configuración fiscal ─────────────────────────────────────
    @Column(name = "iva_activo", nullable = false)
    private boolean ivaActivo = true;

    @Column(name = "iva_porcentaje", precision = 5, scale = 2)
    private java.math.BigDecimal ivaPorcentaje = new java.math.BigDecimal("16.00");

    @Column(name = "igtf_activo", nullable = false)
    private boolean igtfActivo = false;

    // ─── Deducciones de nómina configurables ──────────────────────
    @Column(name = "sso_porcentaje", precision = 5, scale = 2)
    private java.math.BigDecimal ssoPorcentaje = new java.math.BigDecimal("4.00");

    @Column(name = "inces_porcentaje", precision = 5, scale = 2)
    private java.math.BigDecimal incesPorcentaje = new java.math.BigDecimal("0.50");

    @Column(name = "faov_porcentaje", precision = 5, scale = 2)
    private java.math.BigDecimal faovPorcentaje = new java.math.BigDecimal("1.00");

    // ─── Numeración de facturas ───────────────────────────────────
    @Column(name = "factura_prefijo", length = 10)
    private String facturaPrefijo;

    @Column(name = "factura_siguiente_numero", nullable = false)
    private Integer facturaSiguienteNumero = 1;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
