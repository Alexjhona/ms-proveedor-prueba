package com.example.ms_proveedor.service.impl;

import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.entity.Proveedor;
import com.example.ms_proveedor.feign.SunatClient;
import com.example.ms_proveedor.dto.DniResponse;
import com.example.ms_proveedor.dto.RucResponse;
import com.example.ms_proveedor.repository.ProveedorRepository;
import com.example.ms_proveedor.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final SunatClient sunatClient;

    @Override
    public ProveedorDto crearProveedor(ProveedorDto proveedorDto) {
        String razonSocial = proveedorDto.getRazonSocialONombre();

        // Si es DNI (8 dígitos), consultamos SUNAT y tomamos solo el nombre
        if (proveedorDto.getDniOrRuc().length() == 8) {
            try {
                DniResponse respuesta = sunatClient.obtenerInfoDni(proveedorDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Si falla, mantenemos el valor enviado
            }
        }
        // Si es RUC (11 dígitos), consultamos SUNAT y tomamos el nombre
        else if (proveedorDto.getDniOrRuc().length() == 11) {
            try {
                RucResponse respuesta = sunatClient.obtenerInfoRuc(proveedorDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Si falla, mantenemos el valor enviado
            }
        }

        proveedorDto.setRazonSocialONombre(razonSocial);

        if (proveedorRepository.existsByDniOrRuc(proveedorDto.getDniOrRuc())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese DNI o RUC");
        }

        Proveedor entidad = Proveedor.builder()
                .dniOrRuc(proveedorDto.getDniOrRuc())
                .razonSocialONombre(proveedorDto.getRazonSocialONombre())
                .direccion(proveedorDto.getDireccion())
                .telefono(proveedorDto.getTelefono())
                .build();

        Proveedor guardado = proveedorRepository.save(entidad);
        return mapToDto(guardado);
    }

    @Override
    public ProveedorDto obtenerProveedorPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
        return mapToDto(proveedor);
    }

    @Override
    public List<ProveedorDto> listarProveedores() {
        return proveedorRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProveedorDto actualizarProveedor(Long id, ProveedorDto proveedorDto) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));

        String razonSocial = proveedorDto.getRazonSocialONombre();

        // Siempre consultamos SUNAT al editar, si es DNI o RUC
        if (proveedorDto.getDniOrRuc().length() == 8) {
            try {
                DniResponse respuesta = sunatClient.obtenerInfoDni(proveedorDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Mantener valor enviado
            }
        } else if (proveedorDto.getDniOrRuc().length() == 11) {
            try {
                RucResponse respuesta = sunatClient.obtenerInfoRuc(proveedorDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Mantener valor enviado
            }
        }

        proveedorDto.setRazonSocialONombre(razonSocial);

        if (!existente.getDniOrRuc().equals(proveedorDto.getDniOrRuc())
                && proveedorRepository.existsByDniOrRuc(proveedorDto.getDniOrRuc())) {
            throw new IllegalArgumentException("Ya existe otro cliente con ese DNI o RUC");
        }

        existente.setDniOrRuc(proveedorDto.getDniOrRuc());
        existente.setRazonSocialONombre(proveedorDto.getRazonSocialONombre());
        existente.setDireccion(proveedorDto.getDireccion());
        existente.setTelefono(proveedorDto.getTelefono());
        Proveedor actualizado = proveedorRepository.save(existente);
        return mapToDto(actualizado);
    }

    @Override
    public void eliminarProveedor(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe cliente con id: " + id);
        }
        proveedorRepository.deleteById(id);
    }

    private ProveedorDto mapToDto(Proveedor entidad) {
        return ProveedorDto.builder()
                .id(entidad.getId())
                .dniOrRuc(entidad.getDniOrRuc())
                .razonSocialONombre(entidad.getRazonSocialONombre())
                .direccion(entidad.getDireccion())
                .telefono(entidad.getTelefono())
                .build();
    }
}
