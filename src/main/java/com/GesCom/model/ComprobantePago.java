package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobante_pago")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ComprobantePago {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comprobanteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    @Column(precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(length = 50, nullable = false)
    private String estado; // PENDIENTE, APROBADO, RECHAZADO

    @Column(name = "plan_solicitado", length = 50)
    private String planSolicitado; // EMPRENDEDOR o NEGOCIO

    @Column(length = 500)
    private String notas;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
