package com.example.ms_proveedor.controller;

import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.dto.RucResponse;
import com.example.ms_proveedor.exception.ConflictoRecursoException;
import com.example.ms_proveedor.exception.RecursoNoEncontradoException;
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
import static org.mockito.Mockito.doThrow;
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
        ProveedorDto request = proveedorDto("20123456789", "Proveedor Test", "contacto@proveedor.com",
                "Av. Peru 123", "987654321");
        ProveedorDto response = proveedorDto(1L, "20123456789", "Proveedor Test", "contacto@proveedor.com",
                "Av. Peru 123", "987654321");

        when(proveedorService.crearProveedor(any(ProveedorDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dniOrRuc").value("20123456789"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor Test"))
                .andExpect(jsonPath("$.correoElectronico").value("contacto@proveedor.com"))
                .andExpect(jsonPath("$.direccion").value("Av. Peru 123"))
                .andExpect(jsonPath("$.telefono").value("987654321"));

        verify(proveedorService).crearProveedor(any(ProveedorDto.class));
    }

    @Test
    @DisplayName("GET /api/proveedores/{id} - obtiene proveedor por id")
    void obtenerProveedorPorId_RetornaOk() throws Exception {
        ProveedorDto response = proveedorDto(1L, "20123456789", "Proveedor Test", "contacto@proveedor.com",
                "Av. Peru 123", "987654321");

        when(proveedorService.obtenerProveedorPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/proveedores/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dniOrRuc").value("20123456789"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor Test"));

        verify(proveedorService).obtenerProveedorPorId(1L);
    }

    @Test
    @DisplayName("GET /api/proveedores - lista proveedores")
    void listarProveedores_RetornaOk() throws Exception {
        ProveedorDto proveedor1 = proveedorDto(1L, "20123456789", "Proveedor Uno", "uno@proveedor.com",
                "Direccion 1", "900111222");
        ProveedorDto proveedor2 = proveedorDto(2L, "20987654321", "Proveedor Dos", "dos@proveedor.com",
                "Direccion 2", "900333444");

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
        ProveedorDto request = proveedorDto("20123456789", "Proveedor Actualizado", "contacto@proveedor.com",
                "Nueva Direccion", "911111111");
        ProveedorDto response = proveedorDto(1L, "20123456789", "Proveedor Actualizado", "contacto@proveedor.com",
                "Nueva Direccion", "911111111");

        when(proveedorService.actualizarProveedor(eq(1L), any(ProveedorDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/proveedores/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dniOrRuc").value("20123456789"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Proveedor Actualizado"))
                .andExpect(jsonPath("$.correoElectronico").value("contacto@proveedor.com"))
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
    void consultarRuc_RetornaRespuestaDelServicio() throws Exception {
        RucResponse response = new RucResponse();
        response.setNumeroDocumento("20123456789");
        response.setNombre("Empresa SAC");
        when(proveedorService.consultarRuc("20123456789")).thenReturn(response);

        mockMvc.perform(get("/api/proveedores/ruc/{ruc}", "20123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDocumento").value("20123456789"))
                .andExpect(jsonPath("$.nombre").value("Empresa SAC"));

        verify(proveedorService).consultarRuc("20123456789");
    }

    @Test
    @DisplayName("POST /api/proveedores - retorna Bad Request con todos los errores de validacion")
    void crearProveedor_DatosInvalidos_RetornaBadRequest() throws Exception {
        ProveedorDto request = proveedorDto("", "Proveedor Test", "", "", "");

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("Se encontraron errores de validación"))
                .andExpect(jsonPath("$.ruta").value("/api/proveedores"))
                .andExpect(jsonPath("$.datosRecibidos.dniOrRuc").value(""))
                .andExpect(jsonPath("$.datosRecibidos.correoElectronico").value(""))
                .andExpect(jsonPath("$.datosRecibidos.direccion").value(""))
                .andExpect(jsonPath("$.datosRecibidos.telefono").value(""))
                .andExpect(jsonPath("$.errores.*", hasSize(4)))
                .andExpect(jsonPath("$.errores.dniOrRuc").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.correoElectronico").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.direccion").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.telefono").value("Campo obligatorio"));
    }

    @Test
    @DisplayName("PUT /api/proveedores/{id} - retorna Bad Request con datos recibidos y errores")
    void actualizarProveedor_DatosInvalidos_RetornaBadRequest() throws Exception {
        ProveedorDto request = proveedorDto("", "Proveedor Test", "", "", "");

        mockMvc.perform(put("/api/proveedores/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("Se encontraron errores de validación"))
                .andExpect(jsonPath("$.ruta").value("/api/proveedores/1"))
                .andExpect(jsonPath("$.datosRecibidos.razonSocialONombre").value("Proveedor Test"))
                .andExpect(jsonPath("$.errores.dniOrRuc").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.correoElectronico").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.direccion").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.telefono").value("Campo obligatorio"));
    }

    @Test
    @DisplayName("POST /api/proveedores - retorna Bad Request si datos obligatorios son nulos")
    void crearProveedor_DatosNulos_RetornaBadRequestConMensajes() throws Exception {
        ProveedorDto request = ProveedorDto.builder()
                .razonSocialONombre("Proveedor Test")
                .build();

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.datosRecibidos.razonSocialONombre").value("Proveedor Test"))
                .andExpect(jsonPath("$.errores.dniOrRuc").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.correoElectronico").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.direccion").value("Campo obligatorio"))
                .andExpect(jsonPath("$.errores.telefono").value("Campo obligatorio"));
    }

    @Test
    @DisplayName("PUT /api/proveedores/{id} - retorna Bad Request si RUC tiene formato invalido")
    void actualizarProveedor_DocumentoInvalido_RetornaBadRequestConMensaje() throws Exception {
        ProveedorDto request = proveedorDto("ABC123", "Proveedor Test", "contacto@proveedor.com",
                "Av. Peru 123", "987654321");

        mockMvc.perform(put("/api/proveedores/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.datosRecibidos.dniOrRuc").value("ABC123"))
                .andExpect(jsonPath("$.errores.dniOrRuc").value("RUC debe tener 11 digitos"));
    }

    @Test
    @DisplayName("POST /api/proveedores - retorna Bad Request si un campo tiene tipo invalido")
    void crearProveedor_TipoDatoInvalido_RetornaBadRequestConCampo() throws Exception {
        String request = """
                {
                  "id": "abc",
                  "dniOrRuc": "20123456789",
                  "razonSocialONombre": "Proveedor Test",
                  "correoElectronico": "contacto@proveedor.com",
                  "direccion": "Av. Peru 123",
                  "telefono": "987654321"
                }
                """;

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("Se encontraron errores de validación"))
                .andExpect(jsonPath("$.ruta").value("/api/proveedores"))
                .andExpect(jsonPath("$.datosRecibidos.id").value("abc"))
                .andExpect(jsonPath("$.errores.id").value("Tipo de dato invalido"));
    }

    @Test
    @DisplayName("GET /api/proveedores/{id} - retorna Not Found uniforme")
    void obtenerProveedor_CuandoServicioLanzaNoEncontrado_RetornaNotFound() throws Exception {
        when(proveedorService.obtenerProveedorPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Proveedor no encontrado con id: 99"));

        mockMvc.perform(get("/api/proveedores/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("No se encontró el recurso solicitado"))
                .andExpect(jsonPath("$.ruta").value("/api/proveedores/99"));
    }

    @Test
    @DisplayName("POST /api/proveedores - retorna Conflict uniforme ante duplicado")
    void crearProveedor_CuandoServicioLanzaDuplicado_RetornaConflict() throws Exception {
        ProveedorDto request = proveedorDto("20123456789", "Proveedor Test", "contacto@proveedor.com",
                "Av. Peru 123", "987654321");

        when(proveedorService.crearProveedor(any(ProveedorDto.class)))
                .thenThrow(new ConflictoRecursoException("Ya existe un proveedor con ese RUC"));

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.mensaje").value("El registro ya existe o genera conflicto"))
                .andExpect(jsonPath("$.ruta").value("/api/proveedores"));
    }

    @Test
    @DisplayName("DELETE /api/proveedores/{id} - retorna Not Found uniforme")
    void eliminarProveedor_CuandoServicioLanzaNoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe proveedor con id: 99"))
                .when(proveedorService).eliminarProveedor(99L);

        mockMvc.perform(delete("/api/proveedores/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("No se encontró el recurso solicitado"))
                .andExpect(jsonPath("$.ruta").value("/api/proveedores/99"));
    }

    @Test
    @DisplayName("GET /api/proveedores - retorna Internal Server Error uniforme")
    void listarProveedores_CuandoServicioLanzaError_RetornaInternalServerError() throws Exception {
        when(proveedorService.listarProveedores()).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado en el servidor"))
                .andExpect(jsonPath("$.ruta").value("/api/proveedores"));
    }

    private ProveedorDto proveedorDto(
            String dniOrRuc,
            String razonSocialONombre,
            String correoElectronico,
            String direccion,
            String telefono) {
        return proveedorDto(null, dniOrRuc, razonSocialONombre, correoElectronico, direccion, telefono);
    }

    private ProveedorDto proveedorDto(
            Long id,
            String dniOrRuc,
            String razonSocialONombre,
            String correoElectronico,
            String direccion,
            String telefono) {
        return ProveedorDto.builder()
                .id(id)
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .correoElectronico(correoElectronico)
                .direccion(direccion)
                .telefono(telefono)
                .build();
    }
}
