package com.example.ms_proveedor.integration;

import com.example.ms_proveedor.dto.DniResponse;
import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.dto.RucResponse;
import com.example.ms_proveedor.entity.Proveedor;
import com.example.ms_proveedor.repository.ProveedorRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas de integracion - ProveedorController")
class ProveedorControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @BeforeEach
    void setUp() {
        proveedorRepository.deleteAll();
        when(sunatClient.obtenerInfoDni("12345678")).thenReturn(dniResponse("12345678", "Juan Perez"));
        when(sunatClient.obtenerInfoDni("87654321")).thenReturn(dniResponse("87654321", "Maria Lopez"));
        when(sunatClient.obtenerInfoRuc("20123456789")).thenReturn(rucResponse("20123456789", "Proveedor SAC"));
    }

    @Test
    @DisplayName("POST /api/proveedores crea proveedor y lo persiste")
    void crearProveedor_RetornaCreated() throws Exception {
        ProveedorDto request = proveedorDto("12345678", "Nombre Manual", "Av. Peru 123", "987654321");

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Juan Perez"))
                .andExpect(jsonPath("$.direccion").value("Av. Peru 123"))
                .andExpect(jsonPath("$.telefono").value("987654321"));

        assertThat(proveedorRepository.existsByDniOrRuc("12345678")).isTrue();
    }

    @Test
    @DisplayName("GET /api/proveedores lista proveedores persistidos")
    void listarProveedores_RetornaLista() throws Exception {
        guardarProveedor("12345678", "Proveedor Uno", "Direccion 1", "900111222");
        guardarProveedor("20123456789", "Proveedor Dos", "Direccion 2", "900333444");

        mockMvc.perform(get("/api/proveedores")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$[0].razonSocialONombre").value("Proveedor Uno"))
                .andExpect(jsonPath("$[1].dniOrRuc").value("20123456789"))
                .andExpect(jsonPath("$[1].razonSocialONombre").value("Proveedor Dos"));
    }

    @Test
    @DisplayName("GET /api/proveedores/{id} obtiene proveedor existente")
    void obtenerProveedor_RetornaProveedor() throws Exception {
        Proveedor proveedor = guardarProveedor("12345678", "Proveedor Test", "Av. Peru 123", "987654321");

        mockMvc.perform(get("/api/proveedores/{id}", proveedor.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(proveedor.getId()))
                .andExpect(jsonPath("$.dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor Test"));
    }

    @Test
    @DisplayName("PUT /api/proveedores/{id} actualiza proveedor existente")
    void actualizarProveedor_RetornaProveedorActualizado() throws Exception {
        Proveedor proveedor = guardarProveedor("12345678", "Proveedor Inicial", "Direccion Inicial", "900000000");
        ProveedorDto request = proveedorDto("20123456789", "Proveedor Manual", "Nueva Direccion", "911111111");

        mockMvc.perform(put("/api/proveedores/{id}", proveedor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(proveedor.getId()))
                .andExpect(jsonPath("$.dniOrRuc").value("20123456789"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor SAC"))
                .andExpect(jsonPath("$.direccion").value("Nueva Direccion"))
                .andExpect(jsonPath("$.telefono").value("911111111"));
    }

    @Test
    @DisplayName("DELETE /api/proveedores/{id} elimina proveedor existente")
    void eliminarProveedor_RetornaNoContent() throws Exception {
        Proveedor proveedor = guardarProveedor("12345678", "Proveedor Test", "Av. Peru 123", "987654321");

        mockMvc.perform(delete("/api/proveedores/{id}", proveedor.getId()))
                .andExpect(status().isNoContent());

        assertThat(proveedorRepository.findById(proveedor.getId())).isEmpty();
    }

    @Test
    @DisplayName("GET /api/proveedores/{id} propaga error cuando no existe")
    void obtenerProveedor_CuandoNoExiste_PropagaError() {
        ServletException exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/proveedores/{id}", 99999L)
                        .accept(MediaType.APPLICATION_JSON)));

        assertThat(exception.getCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente no encontrado con id: 99999");
    }

    private ProveedorDto proveedorDto(String dniOrRuc, String razonSocialONombre, String direccion, String telefono) {
        return ProveedorDto.builder()
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .direccion(direccion)
                .telefono(telefono)
                .build();
    }

    private Proveedor guardarProveedor(String dniOrRuc, String razonSocialONombre, String direccion, String telefono) {
        Proveedor proveedor = Proveedor.builder()
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .direccion(direccion)
                .telefono(telefono)
                .build();
        return proveedorRepository.save(proveedor);
    }

    private DniResponse dniResponse(String dni, String nombre) {
        DniResponse response = new DniResponse();
        response.setDni(dni);
        response.setNombre(nombre);
        return response;
    }

    private RucResponse rucResponse(String numeroDocumento, String nombre) {
        RucResponse response = new RucResponse();
        response.setNumeroDocumento(numeroDocumento);
        response.setNombre(nombre);
        return response;
    }
}
