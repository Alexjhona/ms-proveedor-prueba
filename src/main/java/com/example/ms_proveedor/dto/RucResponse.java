// src/main/java/com/example/ms_cliente/dto/RucResponse.java
package com.example.ms_proveedor.dto;

import lombok.Data;

@Data
public class RucResponse {
    private String nombre;          // Ej:
    private String tipoDocumento;   // Ej:
    private String numeroDocumento; // Ej:
    private String estado;          // Ej:
    private String condicion;       // Ej:
    private String direccion;       // Ej: "-"
    private String ubigeo;          // Ej: "-"
    private String viaTipo;         // Ej: "-"
    private String viaNombre;       // Ej: "-"
    private String zonaCodigo;      // Ej: "-"
    private String zonaTipo;        // Ej: "-"
    private String numero;          // Ej: "-"
    private String interior;        // Ej: "-"
    private String lote;            // Ej: "-"
    private String dpto;            // Ej: "-"
    private String manzana;         // Ej: "-"
    private String kilometro;       // Ej: "-"
    private String distrito;        // Ej: ""
    private String provincia;       // Ej: ""
    private String departamento;    // Ej: ""
}
