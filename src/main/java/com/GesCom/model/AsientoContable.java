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
@Table(name = "asiento_contable")
public class AsientoContable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asiento_id")
    private Long asientoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "numero_asiento")
    private Integer numeroAsiento;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(name = "transaccion_id")
    private Long transaccionId;

    @Column(name = "es_automatico")
    private boolean esAutomatico = false;

    @Column(name = "periodo_cerrado")
    private boolean periodoCerrado = false;

    @OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LineaAsiento> lineas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
