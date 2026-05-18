package com.GesCom.model;

import com.GesCom.enums.CategoriaProveedor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proveedor_id")
    private Long proveedorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String rif;

    @Column(length = 255)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaProveedor categoria;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
