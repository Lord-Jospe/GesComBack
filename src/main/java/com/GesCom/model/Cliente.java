package com.GesCom.model;


import com.GesCom.enums.TipoPersona;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cliente_id")
    private Long clienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona", nullable = false, length = 20)
    private TipoPersona tipoPersona;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "rif_cedula", nullable = false, unique = true, length = 20)
    private String rifCedula;

    @Column(length = 255)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
