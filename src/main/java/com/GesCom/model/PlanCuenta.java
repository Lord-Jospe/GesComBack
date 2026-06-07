package com.GesCom.model;

import com.GesCom.enums.TipoCuenta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor @Builder
@Getter @Setter
@Table(name = "plan_cuenta")
public class PlanCuenta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cuenta_id")
    private Long cuentaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 15)
    private TipoCuenta tipoCuenta;

    @Column(name = "cuenta_padre_id")
    private Long cuentaPadreId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "es_predeterminada")
    private boolean esPredeterminada = false;
}
