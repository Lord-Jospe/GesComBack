package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_suscripcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre; // SEMILLA, EMPRENDEDOR, NEGOCIO

    @Column(name = "precio_usd", nullable = false, precision = 8, scale = 2)
    private java.math.BigDecimal precioUsd;

    @Column(name = "max_archivos_mes")
    private Integer maxArchivosMes; // null = ilimitado

    @Column(name = "max_transacciones_mes")
    private Integer maxTransaccionesMes; // null = ilimitado

    @Column(name = "max_usuarios")
    private Integer maxUsuarios; // null = ilimitado

    @Column(name = "tiene_inventario", nullable = false)
    private boolean tieneInventario = false;

    @Column(name = "tiene_nomina", nullable = false)
    private boolean tieneNomina = false;

    @Column(name = "tiene_contabilidad", nullable = false)
    private boolean tieneContabilidad = false;
}

