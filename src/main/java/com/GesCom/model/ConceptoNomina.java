package com.GesCom.model;

import com.GesCom.enums.TipoConcepto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor @AllArgsConstructor @Builder
@Getter @Setter
@Table(name = "concepto_nomina")
public class ConceptoNomina {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concepto_id")
    private Long conceptoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomina_id", nullable = false)
    private Nomina nomina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoConcepto tipo;

    @Column(nullable = false, length = 100)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;
}
