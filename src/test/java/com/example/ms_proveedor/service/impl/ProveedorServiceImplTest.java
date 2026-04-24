package com.example.ms_proveedor.service.impl;

import com.example.ms_proveedor.dto.DniResponse;
import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.dto.RucResponse;
import com.example.ms_proveedor.entity.Proveedor;
import com.example.ms_proveedor.feign.SunatClient;
import com.example.ms_proveedor.repository.ProveedorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private SunatClient sunatClient;

    @InjectMocks
    private ProveedorServiceImpl proveedorService;

    @Test
    @DisplayName("Crear proveedor con DNI - consulta SUNAT y guarda correctamente")
    void crearProveedor_ConDni_ConsultaSunatYGuarda() {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("12345678")
                .razonSocialONombre("Nombre manual")
                .direccion("Av. Peru 123")
                .telefono("987654321")
                .build();

        DniResponse dniResponse = new DniResponse();
        dniResponse.setDni("12345678");
        dniResponse.setNombre("Juan Perez");

        when(sunatClient.obtenerInfoDni("12345678")).thenReturn(dniResponse);
        when(proveedorRepository.existsByDniOrRuc("12345678")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor proveedor = invocation.getArgument(0);
            proveedor.setId(1L);
            return proveedor;
        });

        ProveedorDto resultado = proveedorService.crearProveedor(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("12345678");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Juan Perez");
        assertThat(resultado.getDireccion()).isEqualTo("Av. Peru 123");
        assertThat(resultado.getTelefono()).isEqualTo("987654321");

        verify(sunatClient).obtenerInfoDni("12345678");
        verify(proveedorRepository).existsByDniOrRuc("12345678");
        verify(proveedorRepository).save(argThat(proveedor ->
                proveedor.getDniOrRuc().equals("12345678")
                        && proveedor.getRazonSocialONombre().equals("Juan Perez")
        ));
    }

    @Test
    @DisplayName("Crear proveedor con RUC - consulta SUNAT y guarda correctamente")
    void crearProveedor_ConRuc_ConsultaSunatYGuarda() {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("20123456789")
                .razonSocialONombre("Empresa manual")
                .direccion("Jr. Lima 456")
                .telefono("999888777")
                .build();

        RucResponse rucResponse = new RucResponse();
        rucResponse.setNombre("Empresa SAC");
        rucResponse.setNumeroDocumento("20123456789");

        when(sunatClient.obtenerInfoRuc("20123456789")).thenReturn(rucResponse);
        when(proveedorRepository.existsByDniOrRuc("20123456789")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor proveedor = invocation.getArgument(0);
            proveedor.setId(2L);
            return proveedor;
        });

        ProveedorDto resultado = proveedorService.crearProveedor(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("20123456789");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Empresa SAC");
        assertThat(resultado.getDireccion()).isEqualTo("Jr. Lima 456");
        assertThat(resultado.getTelefono()).isEqualTo("999888777");

        verify(sunatClient).obtenerInfoRuc("20123456789");
        verify(proveedorRepository).existsByDniOrRuc("20123456789");
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Crear proveedor - si SUNAT falla mantiene el nombre enviado")
    void crearProveedor_CuandoSunatFalla_MantieneNombreEnviado() {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Manual")
                .direccion("Av. Manual 100")
                .telefono("900111222")
                .build();

        when(sunatClient.obtenerInfoDni("12345678")).thenThrow(new RuntimeException("Error SUNAT"));
        when(proveedorRepository.existsByDniOrRuc("12345678")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor proveedor = invocation.getArgument(0);
            proveedor.setId(3L);
            return proveedor;
        });

        ProveedorDto resultado = proveedorService.crearProveedor(request);

        assertThat(resultado.getId()).isEqualTo(3L);
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Proveedor Manual");

        verify(sunatClient).obtenerInfoDni("12345678");
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Crear proveedor - lanza excepción si DNI o RUC ya existe")
    void crearProveedor_CuandoDniORucExiste_LanzaExcepcion() {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Repetido")
                .direccion("Av. Repetida 123")
                .telefono("911222333")
                .build();

        when(proveedorRepository.existsByDniOrRuc("12345678")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> proveedorService.crearProveedor(request)
        );

        assertThat(exception.getMessage()).contains("Ya existe");
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Obtener proveedor por id - retorna proveedor existente")
    void obtenerProveedorPorId_CuandoExiste_RetornaProveedor() {
        Proveedor proveedor = Proveedor.builder()
                .id(1L)
                .dniOrRuc("12345678")
                .razonSocialONombre("Juan Perez")
                .direccion("Av. Peru 123")
                .telefono("987654321")
                .build();

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        ProveedorDto resultado = proveedorService.obtenerProveedorPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("12345678");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Juan Perez");

        verify(proveedorRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener proveedor por id - lanza excepción si no existe")
    void obtenerProveedorPorId_CuandoNoExiste_LanzaExcepcion() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> proveedorService.obtenerProveedorPorId(99L)
        );

        assertThat(exception.getMessage()).contains("no encontrado");
        verify(proveedorRepository).findById(99L);
    }

    @Test
    @DisplayName("Listar proveedores - retorna lista de proveedores")
    void listarProveedores_RetornaLista() {
        Proveedor proveedor1 = Proveedor.builder()
                .id(1L)
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Uno")
                .direccion("Direccion 1")
                .telefono("900111222")
                .build();

        Proveedor proveedor2 = Proveedor.builder()
                .id(2L)
                .dniOrRuc("20123456789")
                .razonSocialONombre("Proveedor Dos")
                .direccion("Direccion 2")
                .telefono("900333444")
                .build();

        when(proveedorRepository.findAll()).thenReturn(List.of(proveedor1, proveedor2));

        List<ProveedorDto> resultado = proveedorService.listarProveedores();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(1).getId()).isEqualTo(2L);
        assertThat(resultado.get(0).getRazonSocialONombre()).isEqualTo("Proveedor Uno");
        assertThat(resultado.get(1).getRazonSocialONombre()).isEqualTo("Proveedor Dos");

        verify(proveedorRepository).findAll();
    }

    @Test
    @DisplayName("Actualizar proveedor - actualiza datos correctamente")
    void actualizarProveedor_CuandoExiste_ActualizaCorrectamente() {
        Proveedor existente = Proveedor.builder()
                .id(1L)
                .dniOrRuc("12345678")
                .razonSocialONombre("Nombre Antiguo")
                .direccion("Direccion Antigua")
                .telefono("900000000")
                .build();

        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("20123456789")
                .razonSocialONombre("Nombre Nuevo")
                .direccion("Direccion Nueva")
                .telefono("911111111")
                .build();

        RucResponse rucResponse = new RucResponse();
        rucResponse.setNombre("Empresa Nueva SAC");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoRuc("20123456789")).thenReturn(rucResponse);
        when(proveedorRepository.existsByDniOrRuc("20123456789")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProveedorDto resultado = proveedorService.actualizarProveedor(1L, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("20123456789");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Empresa Nueva SAC");
        assertThat(resultado.getDireccion()).isEqualTo("Direccion Nueva");
        assertThat(resultado.getTelefono()).isEqualTo("911111111");

        verify(proveedorRepository).findById(1L);
        verify(sunatClient).obtenerInfoRuc("20123456789");
        verify(proveedorRepository).save(existente);
    }

    @Test
    @DisplayName("Actualizar proveedor - lanza excepción si proveedor no existe")
    void actualizarProveedor_CuandoNoExiste_LanzaExcepcion() {
        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor")
                .direccion("Direccion")
                .telefono("900111222")
                .build();

        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> proveedorService.actualizarProveedor(99L, request)
        );

        assertThat(exception.getMessage()).contains("no encontrado");
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Actualizar proveedor - lanza excepción si nuevo DNI o RUC ya existe")
    void actualizarProveedor_CuandoNuevoDniORucYaExiste_LanzaExcepcion() {
        Proveedor existente = Proveedor.builder()
                .id(1L)
                .dniOrRuc("12345678")
                .razonSocialONombre("Proveedor Antiguo")
                .direccion("Direccion Antigua")
                .telefono("900000000")
                .build();

        ProveedorDto request = ProveedorDto.builder()
                .dniOrRuc("20123456789")
                .razonSocialONombre("Proveedor Nuevo")
                .direccion("Direccion Nueva")
                .telefono("911111111")
                .build();

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.existsByDniOrRuc("20123456789")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> proveedorService.actualizarProveedor(1L, request)
        );

        assertThat(exception.getMessage()).contains("Ya existe");
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Eliminar proveedor - elimina si existe")
    void eliminarProveedor_CuandoExiste_EliminaCorrectamente() {
        when(proveedorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(proveedorRepository).deleteById(1L);

        proveedorService.eliminarProveedor(1L);

        verify(proveedorRepository).existsById(1L);
        verify(proveedorRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar proveedor - lanza excepción si no existe")
    void eliminarProveedor_CuandoNoExiste_LanzaExcepcion() {
        when(proveedorRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> proveedorService.eliminarProveedor(99L)
        );

        assertThat(exception.getMessage()).contains("No existe");
        verify(proveedorRepository).existsById(99L);
        verify(proveedorRepository, never()).deleteById(anyLong());
    }
}