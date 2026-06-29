package com.example.ms_proveedor.service.impl;

import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.entity.Proveedor;
import com.example.ms_proveedor.exception.ConflictoRecursoException;
import com.example.ms_proveedor.exception.RecursoNoEncontradoException;
import com.example.ms_proveedor.feign.SunatClient;
import com.example.ms_proveedor.dto.RucResponse;
import com.example.ms_proveedor.repository.ProveedorRepository;
import com.example.ms_proveedor.service.ProveedorService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final SunatClient sunatClient;

    @Override
    public ProveedorDto crearProveedor(ProveedorDto proveedorDto) {
        String razonSocial = proveedorDto.getRazonSocialONombre();

        // Proveedores se consultan solo por RUC.
        if (proveedorDto.getDniOrRuc().length() == 11) {
            try {
                RucResponse respuesta = sunatClient.obtenerInfoRuc(proveedorDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Si falla, mantenemos el valor enviado
            }
        }

        proveedorDto.setRazonSocialONombre(razonSocial);

        if (proveedorRepository.existsByDniOrRuc(proveedorDto.getDniOrRuc())) {
            throw new ConflictoRecursoException("Ya existe un proveedor con ese RUC");
        }

        Proveedor entidad = Proveedor.builder()
                .dniOrRuc(proveedorDto.getDniOrRuc())
                .razonSocialONombre(proveedorDto.getRazonSocialONombre())
                .correoElectronico(proveedorDto.getCorreoElectronico())
                .direccion(normalizarDireccion(proveedorDto.getDireccion()))
                .telefono(proveedorDto.getTelefono())
                .build();

        Proveedor guardado = proveedorRepository.save(entidad);
        return mapToDto(guardado);
    }

    @Override
    public ProveedorDto obtenerProveedorPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id: " + id));
        return mapToDto(proveedor);
    }

    @Override
    public List<ProveedorDto> listarProveedores() {
        return proveedorRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public ProveedorDto actualizarProveedor(Long id, ProveedorDto proveedorDto) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id: " + id));

        String razonSocial = proveedorDto.getRazonSocialONombre();

        // Proveedores se consultan solo por RUC.
        if (proveedorDto.getDniOrRuc().length() == 11) {
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
            throw new ConflictoRecursoException("Ya existe otro proveedor con ese RUC");
        }

        existente.setDniOrRuc(proveedorDto.getDniOrRuc());
        existente.setRazonSocialONombre(proveedorDto.getRazonSocialONombre());
        existente.setCorreoElectronico(proveedorDto.getCorreoElectronico());
        existente.setDireccion(normalizarDireccion(proveedorDto.getDireccion()));
        existente.setTelefono(proveedorDto.getTelefono());
        Proveedor actualizado = proveedorRepository.save(existente);
        return mapToDto(actualizado);
    }

    @Override
    public void eliminarProveedor(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe proveedor con id: " + id);
        }
        proveedorRepository.deleteById(id);
    }

    @Override
    public RucResponse consultarRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException("RUC invalido");
        }
        try {
            return sunatClient.obtenerInfoRuc(ruc);
        } catch (FeignException.NotFound | FeignException.UnprocessableEntity ex) {
            throw new IllegalArgumentException("No se encontro el RUC en la API gratuita");
        }
    }

    private ProveedorDto mapToDto(Proveedor entidad) {
        return ProveedorDto.builder()
                .id(entidad.getId())
                .dniOrRuc(entidad.getDniOrRuc())
                .razonSocialONombre(entidad.getRazonSocialONombre())
                .correoElectronico(entidad.getCorreoElectronico())
                .direccion(entidad.getDireccion())
                .telefono(entidad.getTelefono())
                .build();
    }

    private String normalizarDireccion(String direccion) {
        return direccion == null ? "" : direccion.trim();
    }
}
