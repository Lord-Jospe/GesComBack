package com.GesCom.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long rolId;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre; // ADMIN, CONTADOR, OPERADOR

    @Column(length = 100)
    private String descripcion;
}
