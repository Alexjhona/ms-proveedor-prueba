package com.example.ms_proveedor.service;
import com.example.ms_proveedor.dto.ProveedorDto;
import java.util.List;

public interface ProveedorService {
    ProveedorDto crearProveedor(ProveedorDto proveedorDto);
    ProveedorDto obtenerProveedorPorId(Long id);
    List<ProveedorDto> listarProveedores();
    ProveedorDto actualizarProveedor(Long id, ProveedorDto proveedorDto);
    void eliminarProveedor(Long id);
}