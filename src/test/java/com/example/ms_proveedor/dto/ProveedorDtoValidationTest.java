package com.example.ms_proveedor.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pruebas unitarias - Validacion Jakarta de ProveedorDto")
class ProveedorDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("DTO valido - no genera errores de validacion")
    void proveedorDtoValido_NoGeneraViolaciones() {
        ProveedorDto dto = proveedorDto("20123456789", "Proveedor SAC", "contacto@proveedor.com",
                "Av. Peru 123", "987654321");

        Set<ConstraintViolation<ProveedorDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("DTO invalido - campos obligatorios generan mensajes esperados")
    void proveedorDtoConCamposVacios_GeneraViolaciones() {
        ProveedorDto dto = proveedorDto("", "Proveedor SAC", "", "", "");

        Map<String, Set<String>> errores = erroresPorCampo(validator.validate(dto));

        assertThat(errores)
                .containsKeys("dniOrRuc", "correoElectronico", "direccion", "telefono");
        assertThat(errores.get("dniOrRuc")).contains("Campo obligatorio");
        assertThat(errores.get("correoElectronico")).contains("Campo obligatorio");
        assertThat(errores.get("direccion")).contains("Campo obligatorio");
        assertThat(errores.get("telefono")).contains("Campo obligatorio");
    }

    @Test
    @DisplayName("DTO invalido - RUC debe tener 11 digitos")
    void proveedorDtoConRucInvalido_GeneraViolacionDePatron() {
        ProveedorDto dto = proveedorDto("ABC123", "Proveedor SAC", "contacto@proveedor.com",
                "Av. Peru 123", "987654321");

        Map<String, Set<String>> errores = erroresPorCampo(validator.validate(dto));

        assertThat(errores.get("dniOrRuc")).contains("RUC debe tener 11 digitos");
    }

    @Test
    @DisplayName("DTO invalido - correo debe tener formato valido")
    void proveedorDtoConCorreoInvalido_GeneraViolacionDeEmail() {
        ProveedorDto dto = proveedorDto("20123456789", "Proveedor SAC", "correo-invalido",
                "Av. Peru 123", "987654321");

        Map<String, Set<String>> errores = erroresPorCampo(validator.validate(dto));

        assertThat(errores.get("correoElectronico")).contains("Correo electronico invalido");
    }

    private Map<String, Set<String>> erroresPorCampo(Set<ConstraintViolation<ProveedorDto>> violations) {
        return violations.stream()
                .collect(Collectors.groupingBy(
                        violation -> violation.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toSet())
                ));
    }

    private ProveedorDto proveedorDto(
            String dniOrRuc,
            String razonSocialONombre,
            String correoElectronico,
            String direccion,
            String telefono) {
        return ProveedorDto.builder()
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .correoElectronico(correoElectronico)
                .direccion(direccion)
                .telefono(telefono)
                .build();
    }
}
