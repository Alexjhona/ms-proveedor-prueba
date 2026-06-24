package com.example.ms_proveedor.controller;

import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.dto.RucResponse;
import com.example.ms_proveedor.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Validated
@Tag(name = "Proveedores", description = "Endpoints para administrar proveedores usados en operaciones de compra y abastecimiento.")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    @Operation(summary = "Registrar proveedor", description = "Registra un nuevo proveedor para ser utilizado en operaciones de compra y abastecimiento. El microservicio tiene cliente Feign para consultar una API externa de DNI/RUC cuando el servicio lo requiera.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proveedor registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de proveedor invalidos")
    })
    public ResponseEntity<ProveedorDto> crear(@Valid @RequestBody ProveedorDto proveedorDto) {
        ProveedorDto creado = proveedorService.crearProveedor(proveedorDto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por id", description = "Obtiene el detalle de un proveedor registrado usando su identificador unico, permitiendo validar informacion antes de operaciones de compra.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<ProveedorDto> obtener(@Parameter(description = "Identificador unico del recurso proveedor.", example = "1") @PathVariable Long id) {
        ProveedorDto dto = proveedorService.obtenerProveedorPorId(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "Listar proveedores", description = "Obtiene todos los proveedores registrados en el sistema para consulta y seleccion durante procesos de compra.")
    @ApiResponse(responseCode = "200", description = "Listado de proveedores obtenido correctamente")
    public ResponseEntity<List<ProveedorDto>> listar() {
        List<ProveedorDto> lista = proveedorService.listarProveedores();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ruc/{ruc}")
    @Operation(summary = "Consultar proveedor por RUC", description = "Consulta una API externa usando el RUC y devuelve la razon social para autocompletar proveedores.")
    public ResponseEntity<RucResponse> consultarRuc(@Parameter(description = "RUC de 11 digitos.", example = "20123456789") @PathVariable String ruc) {
        return ResponseEntity.ok(proveedorService.consultarRuc(ruc));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor", description = "Modifica los datos de un proveedor existente usando su identificador unico, manteniendo actualizado el registro para compras y abastecimiento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<ProveedorDto> actualizar(
            @Parameter(description = "Identificador unico del recurso proveedor.", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProveedorDto proveedorDto) {
        ProveedorDto actualizado = proveedorService.actualizarProveedor(id, proveedorDto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor", description = "Elimina un proveedor registrado mediante su identificador. Verificar previamente si el proveedor esta asociado a compras existentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador unico del recurso proveedor.", example = "1") @PathVariable Long id) {
        proveedorService.eliminarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}
