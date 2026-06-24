package com.example.ms_proveedor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorDto {

    private Long id;

    @NotBlank(message = "Campo obligatorio")
    @Pattern(regexp = "^\\d{11}$", message = "RUC debe tener 11 digitos")
    private String dniOrRuc;

    private String razonSocialONombre;

    @NotBlank(message = "Campo obligatorio")
    @Email(message = "Correo electronico invalido")
    private String correoElectronico;

    @NotBlank(message = "Campo obligatorio")
    private String direccion;

    @NotBlank(message = "Campo obligatorio")
    private String telefono;
}
