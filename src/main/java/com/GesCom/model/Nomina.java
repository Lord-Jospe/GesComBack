package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor @AllArgsConstructor @Builder
@Getter @Setter
@Table(name = "nomina")
public class Nomina {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nomina_id")
    private Long nominaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "salario_base", precision = 12, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "total_asignaciones", precision = 12, scale = 2)
    private BigDecimal totalAsignaciones = BigDecimal.ZERO;

    @Column(name = "total_deducciones", precision = 12, scale = 2)
    private BigDecimal totalDeducciones = BigDecimal.ZERO;

    @Column(name = "salario_neto", precision = 12, scale = 2)
    private BigDecimal salarioNeto;

    @Column(length = 50)
    private String estado; // CALCULADA, PAGADA, ANULADA

    @Column(length = 500)
    private String notas;

    @OneToMany(mappedBy = "nomina", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConceptoNomina> conceptos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
