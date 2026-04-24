package com.example.ms_proveedor.controller;

import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.service.ProveedorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProveedorController.class)
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProveedorService proveedorService;

    @Test
    @DisplayName("POST /api/proveedores - crea proveedor correctamente")
    void crearProveedor_RetornaCreated() throws Exception {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Test")
                .direccion("Av. Peru 123")
                .telefono("987654321")
                .build();

        ProveedorDto response = ProveedorDto.builder()
                .id(1L)
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Test")
                .direccion("Av. Peru 123")
                .telefono("987654321")
                .build();

        when(proveedorService.crearProveedor(any(ProveedorDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor Test"))
                .andExpect(jsonPath("$.direccion").value("Av. Peru 123"))
                .andExpect(jsonPath("$.telefono").value("987654321"));

        verify(proveedorService).crearProveedor(any(ProveedorDto.class));
    }

    @Test
    @DisplayName("GET /api/proveedores/{id} - obtiene proveedor por id")
    void obtenerProveedorPorId_RetornaOk() throws Exception {
        ProveedorDto response = ProveedorDto.builder()
                .id(1L)
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Test")
                .direccion("Av. Peru 123")
                .telefono("987654321")
                .build();

        when(proveedorService.obtenerProveedorPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/proveedores/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor Test"));

        verify(proveedorService).obtenerProveedorPorId(1L);
    }

    @Test
    @DisplayName("GET /api/proveedores - lista proveedores")
    void listarProveedores_RetornaOk() throws Exception {
        ProveedorDto proveedor1 = ProveedorDto.builder()
                .id(1L)
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Uno")
                .direccion("Direccion 1")
                .telefono("900111222")
                .build();

        ProveedorDto proveedor2 = ProveedorDto.builder()
                .id(2L)
                .dniOrRuc("20123456789")
                .razonSocialONombre("Proveedor Dos")
                .direccion("Direccion 2")
                .telefono("900333444")
                .build();

        when(proveedorService.listarProveedores()).thenReturn(List.of(proveedor1, proveedor2));

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].razonSocialONombre").value("Proveedor Uno"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].razonSocialONombre").value("Proveedor Dos"));

        verify(proveedorService).listarProveedores();
    }

    @Test
    @DisplayName("PUT /api/proveedores/{id} - actualiza proveedor")
    void actualizarProveedor_RetornaOk() throws Exception {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("20123456789")
                .razonSocialONombre("Proveedor Actualizado")
                .direccion("Nueva Direccion")
                .telefono("911111111")
                .build();

        ProveedorDto response = ProveedorDto.builder()
                .id(1L)
                .dniOrRuc("20123456789")
                .razonSocialONombre("Proveedor Actualizado")
                .direccion("Nueva Direccion")
                .telefono("911111111")
                .build();

        when(proveedorService.actualizarProveedor(eq(1L), any(ProveedorDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/proveedores/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dniOrRuc").value("20123456789"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor Actualizado"))
                .andExpect(jsonPath("$.direccion").value("Nueva Direccion"))
                .andExpect(jsonPath("$.telefono").value("911111111"));

        verify(proveedorService).actualizarProveedor(eq(1L), any(ProveedorDto.class));
    }

    @Test
    @DisplayName("DELETE /api/proveedores/{id} - elimina proveedor")
    void eliminarProveedor_RetornaNoContent() throws Exception {
        doNothing().when(proveedorService).eliminarProveedor(1L);

        mockMvc.perform(delete("/api/proveedores/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(proveedorService).eliminarProveedor(1L);
    }

    @Test
    @DisplayName("POST /api/proveedores - retorna Bad Request si datos obligatorios están vacíos")
    void crearProveedor_DatosInvalidos_RetornaBadRequest() throws Exception {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("")
                .razonSocialONombre("Proveedor Test")
                .direccion("")
                .telefono("")
                .build();

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}