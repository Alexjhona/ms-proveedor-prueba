package com.example.ms_proveedor.repository;

import com.example.ms_proveedor.config.MySQLTestContainer;
import com.example.ms_proveedor.entity.Proveedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@DisplayName("Pruebas de integracion - ProveedorRepository con MySQL real")
class ProveedorRepositoryIntegrationTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", MySQLTestContainer.INSTANCE::getDriverClassName);
    }

    @Autowired
    private ProveedorRepository proveedorRepository;

    @BeforeEach
    void setUp() {
        proveedorRepository.deleteAll();
    }

    @Test
    @DisplayName("Guardar proveedor - debe persistir y generar id")
    void guardarProveedor_DebePersistirConId() {
        Proveedor proveedor = proveedor("12345678", "Proveedor Test", "Av. Peru 123", "987654321");

        Proveedor guardado = proveedorRepository.save(proveedor);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getDniOrRuc()).isEqualTo("12345678");
        assertThat(guardado.getRazonSocialONombre()).isEqualTo("Proveedor Test");
    }

    @Test
    @DisplayName("Buscar proveedor por id - retorna proveedor existente")
    void buscarPorId_RetornaProveedorExistente() {
        Proveedor guardado = proveedorRepository.save(
                proveedor("12345678", "Proveedor Test", "Av. Peru 123", "987654321"));

        assertThat(proveedorRepository.findById(guardado.getId()))
                .isPresent()
                .get()
                .satisfies(encontrado -> {
                    assertThat(encontrado.getDniOrRuc()).isEqualTo("12345678");
                    assertThat(encontrado.getDireccion()).isEqualTo("Av. Peru 123");
                });
    }

    @Test
    @DisplayName("Listar proveedores - retorna proveedores persistidos")
    void listarProveedores_RetornaLista() {
        proveedorRepository.save(proveedor("12345678", "Proveedor Uno", "Direccion 1", "900111222"));
        proveedorRepository.save(proveedor("20123456789", "Proveedor Dos", "Direccion 2", "900333444"));

        List<Proveedor> proveedores = proveedorRepository.findAll();

        assertThat(proveedores).hasSize(2);
        assertThat(proveedores)
                .extracting(Proveedor::getDniOrRuc)
                .containsExactlyInAnyOrder("12345678", "20123456789");
    }

    @Test
    @DisplayName("Actualizar proveedor - persiste los nuevos datos")
    void actualizarProveedor_PersisteCambios() {
        Proveedor guardado = proveedorRepository.save(
                proveedor("12345678", "Proveedor Inicial", "Direccion Inicial", "900000000"));

        guardado.setRazonSocialONombre("Proveedor Actualizado");
        guardado.setDireccion("Nueva Direccion");
        guardado.setTelefono("911111111");
        proveedorRepository.save(guardado);

        assertThat(proveedorRepository.findById(guardado.getId()))
                .isPresent()
                .get()
                .satisfies(actualizado -> {
                    assertThat(actualizado.getRazonSocialONombre()).isEqualTo("Proveedor Actualizado");
                    assertThat(actualizado.getDireccion()).isEqualTo("Nueva Direccion");
                    assertThat(actualizado.getTelefono()).isEqualTo("911111111");
                });
    }

    @Test
    @DisplayName("Eliminar proveedor - ya no debe existir")
    void eliminarProveedor_NoExisteDespues() {
        Proveedor guardado = proveedorRepository.save(
                proveedor("12345678", "Proveedor Test", "Av. Peru 123", "987654321"));

        proveedorRepository.deleteById(guardado.getId());

        assertThat(proveedorRepository.findById(guardado.getId())).isEmpty();
    }

    @Test
    @DisplayName("Buscar por DNI o RUC - valida existencia por campo propio")
    void existsByDniOrRuc_RetornaExistencia() {
        proveedorRepository.save(proveedor("20123456789", "Proveedor SAC", "Jr. Lima 456", "999888777"));

        assertThat(proveedorRepository.existsByDniOrRuc("20123456789")).isTrue();
        assertThat(proveedorRepository.existsByDniOrRuc("00000000")).isFalse();
    }

    private Proveedor proveedor(String dniOrRuc, String razonSocialONombre, String direccion, String telefono) {
        return Proveedor.builder()
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .direccion(direccion)
                .telefono(telefono)
                .build();
    }
}
