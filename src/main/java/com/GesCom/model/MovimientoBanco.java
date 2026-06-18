package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_banco")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoBanco {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_banco_id")
    private Long movimientoBancoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 10)
    private String tipo; // CREDITO (entrada) o DEBITO (salida)

    @Column(name = "transaccion_id")
    private Long transaccionId; // vinculado a transacción de GesCom (nullable)

    @Column(name = "conciliado", nullable = false)
    private boolean conciliado;

    @Column(name = "fecha_conciliacion")
    private LocalDate fechaConciliacion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
