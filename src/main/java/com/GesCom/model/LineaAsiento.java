package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor @AllArgsConstructor @Builder
@Getter @Setter
@Table(name = "linea_asiento")
public class LineaAsiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "linea_id")
    private Long lineaLibroId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id", nullable = false)
    private AsientoContable asiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private PlanCuenta cuenta;

    @Column(name = "es_debito")
    private boolean esDebito;

    @Column(precision = 12, scale = 2)
    private BigDecimal monto;
}
