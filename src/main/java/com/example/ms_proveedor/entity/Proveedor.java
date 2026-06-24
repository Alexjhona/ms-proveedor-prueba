package com.example.ms_proveedor.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "RUC es obligatorio")
    @Column(name = "dni_or_ruc", unique = true, nullable = false, length = 20)
    private String dniOrRuc;

    @NotBlank(message = "Razón social o nombre es obligatorio")
    @Column(name = "razon_social_o_nombre", nullable = false)
    private String razonSocialONombre;

    @NotBlank(message = "Correo electronico es obligatorio")
    @Column(name = "correo_electronico", nullable = false)
    private String correoElectronico;

    @Column(nullable = false)
    private String direccion;

    @NotBlank(message = "Teléfono es obligatorio")
    @Column(nullable = false)
    private String telefono;
}
