package com.example.ms_proveedor.service.impl;

import com.example.ms_proveedor.dto.ProveedorDto;
import com.example.ms_proveedor.dto.RucResponse;
import com.example.ms_proveedor.entity.Proveedor;
import com.example.ms_proveedor.exception.ConflictoRecursoException;
import com.example.ms_proveedor.exception.RecursoNoEncontradoException;
import com.example.ms_proveedor.feign.SunatClient;
import com.example.ms_proveedor.repository.ProveedorRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private SunatClient sunatClient;

    @InjectMocks
    private ProveedorServiceImpl proveedorService;

    @Test
    @DisplayName("Crear proveedor con RUC - consulta SUNAT y guarda correctamente")
    void crearProveedor_ConRuc_ConsultaSunatYGuarda() {
        ProveedorDto request = proveedorDto("20123456789", "Empresa manual", "contacto@empresa.com",
                "Jr. Lima 456", "999888777");
        RucResponse rucResponse = rucResponse("20123456789", "Empresa SAC");

        when(sunatClient.obtenerInfoRuc("20123456789")).thenReturn(rucResponse);
        when(proveedorRepository.existsByDniOrRuc("20123456789")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor proveedor = invocation.getArgument(0);
            proveedor.setId(1L);
            return proveedor;
        });

        ProveedorDto resultado = proveedorService.crearProveedor(request);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("20123456789");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Empresa SAC");
        assertThat(resultado.getCorreoElectronico()).isEqualTo("contacto@empresa.com");
        assertThat(resultado.getDireccion()).isEqualTo("Jr. Lima 456");
        assertThat(resultado.getTelefono()).isEqualTo("999888777");

        verify(sunatClient).obtenerInfoRuc("20123456789");
        verify(proveedorRepository).save(argThat(proveedor ->
                proveedor.getDniOrRuc().equals("20123456789")
                        && proveedor.getRazonSocialONombre().equals("Empresa SAC")
                        && proveedor.getCorreoElectronico().equals("contacto@empresa.com")
                        && proveedor.getDireccion().equals("Jr. Lima 456")
        ));
    }

    @Test
    @DisplayName("Crear proveedor con RUC - si SUNAT falla mantiene la razon social enviada")
    void crearProveedor_ConRucCuandoSunatFalla_MantieneRazonSocialEnviada() {
        ProveedorDto request = proveedorDto("20123456789", "Empresa Manual SAC", "contacto@empresa.com",
                "Jr. Manual 200", "900222333");

        when(sunatClient.obtenerInfoRuc("20123456789")).thenThrow(new RuntimeException("Error SUNAT"));
        when(proveedorRepository.existsByDniOrRuc("20123456789")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor proveedor = invocation.getArgument(0);
            proveedor.setId(2L);
            return proveedor;
        });

        ProveedorDto resultado = proveedorService.crearProveedor(request);

        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Empresa Manual SAC");

        verify(sunatClient).obtenerInfoRuc("20123456789");
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Crear proveedor - lanza conflicto si RUC ya existe")
    void crearProveedor_CuandoRucExiste_LanzaConflicto() {
        ProveedorDto request = proveedorDto("20123456789", "Proveedor Repetido", "contacto@empresa.com",
                "Av. Repetida 123", "911222333");

        when(sunatClient.obtenerInfoRuc("20123456789")).thenThrow(new RuntimeException("Error SUNAT"));
        when(proveedorRepository.existsByDniOrRuc("20123456789")).thenReturn(true);

        ConflictoRecursoException exception = assertThrows(
                ConflictoRecursoException.class,
                () -> proveedorService.crearProveedor(request)
        );

        assertThat(exception.getMessage()).contains("Ya existe");
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Crear proveedor con documento de otra longitud - no consulta SUNAT")
    void crearProveedor_ConDocumentoDeOtraLongitud_NoConsultaSunat() {
        ProveedorDto request = proveedorDto("ABC123", "Proveedor Manual", "contacto@empresa.com",
                "Av. Sin Consulta 123", "900444555");

        when(proveedorRepository.existsByDniOrRuc("ABC123")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor proveedor = invocation.getArgument(0);
            proveedor.setId(3L);
            return proveedor;
        });

        ProveedorDto resultado = proveedorService.crearProveedor(request);

        assertThat(resultado.getId()).isEqualTo(3L);
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Proveedor Manual");

        verifyNoInteractions(sunatClient);
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Obtener proveedor por id - retorna proveedor existente")
    void obtenerProveedorPorId_CuandoExiste_RetornaProveedor() {
        Proveedor proveedor = proveedor(1L, "20123456789", "Proveedor SAC", "contacto@empresa.com",
                "Av. Peru 123", "987654321");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        ProveedorDto resultado = proveedorService.obtenerProveedorPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("20123456789");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Proveedor SAC");
        assertThat(resultado.getCorreoElectronico()).isEqualTo("contacto@empresa.com");
        verify(proveedorRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener proveedor por id - lanza recurso no encontrado si no existe")
    void obtenerProveedorPorId_CuandoNoExiste_LanzaRecursoNoEncontrado() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> proveedorService.obtenerProveedorPorId(99L)
        );

        assertThat(exception.getMessage()).contains("no encontrado");
        verify(proveedorRepository).findById(99L);
    }

    @Test
    @DisplayName("Listar proveedores - retorna lista de proveedores")
    void listarProveedores_RetornaLista() {
        Proveedor proveedor1 = proveedor(1L, "20123456789", "Proveedor Uno", "uno@empresa.com",
                "Direccion 1", "900111222");
        Proveedor proveedor2 = proveedor(2L, "20987654321", "Proveedor Dos", "dos@empresa.com",
                "Direccion 2", "900333444");

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
        Proveedor existente = proveedor(1L, "20123456789", "Proveedor Antiguo", "antiguo@empresa.com",
                "Direccion Antigua", "900000000");
        ProveedorDto request = proveedorDto("20987654321", "Nombre Nuevo", "nuevo@empresa.com",
                "Direccion Nueva", "911111111");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoRuc("20987654321")).thenReturn(rucResponse("20987654321", "Empresa Nueva SAC"));
        when(proveedorRepository.existsByDniOrRuc("20987654321")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProveedorDto resultado = proveedorService.actualizarProveedor(1L, request);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("20987654321");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Empresa Nueva SAC");
        assertThat(resultado.getCorreoElectronico()).isEqualTo("nuevo@empresa.com");
        assertThat(resultado.getDireccion()).isEqualTo("Direccion Nueva");
        assertThat(resultado.getTelefono()).isEqualTo("911111111");

        verify(proveedorRepository).save(existente);
    }

    @Test
    @DisplayName("Actualizar proveedor - lanza recurso no encontrado si proveedor no existe")
    void actualizarProveedor_CuandoNoExiste_LanzaRecursoNoEncontrado() {
        ProveedorDto request = proveedorDto("20123456789", "Proveedor", "contacto@empresa.com",
                "Direccion", "900111222");

        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> proveedorService.actualizarProveedor(99L, request)
        );

        assertThat(exception.getMessage()).contains("no encontrado");
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Actualizar proveedor con RUC - si SUNAT falla mantiene razon social enviada")
    void actualizarProveedor_ConRucCuandoSunatFalla_MantieneRazonSocialEnviada() {
        Proveedor existente = proveedor(1L, "20123456789", "Proveedor Antiguo", "antiguo@empresa.com",
                "Direccion Antigua", "900000000");
        ProveedorDto request = proveedorDto("20987654321", "Empresa Manual SAC", "nuevo@empresa.com",
                "Direccion Nueva", "933333333");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoRuc("20987654321")).thenThrow(new RuntimeException("Error SUNAT"));
        when(proveedorRepository.existsByDniOrRuc("20987654321")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProveedorDto resultado = proveedorService.actualizarProveedor(1L, request);

        assertThat(resultado.getDniOrRuc()).isEqualTo("20987654321");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Empresa Manual SAC");
        assertThat(resultado.getCorreoElectronico()).isEqualTo("nuevo@empresa.com");
        verify(sunatClient).obtenerInfoRuc("20987654321");
        verify(proveedorRepository).save(existente);
    }

    @Test
    @DisplayName("Actualizar proveedor con mismo RUC - no valida duplicado")
    void actualizarProveedor_ConMismoRuc_NoConsultaExistenciaPorDuplicado() {
        Proveedor existente = proveedor(1L, "20123456789", "Proveedor Antiguo", "antiguo@empresa.com",
                "Direccion Antigua", "900000000");
        ProveedorDto request = proveedorDto("20123456789", "Proveedor Actualizado", "nuevo@empresa.com",
                "Direccion Actualizada", "944444444");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoRuc("20123456789")).thenThrow(new RuntimeException("Error SUNAT"));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProveedorDto resultado = proveedorService.actualizarProveedor(1L, request);

        assertThat(resultado.getDniOrRuc()).isEqualTo("20123456789");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Proveedor Actualizado");
        assertThat(resultado.getDireccion()).isEqualTo("Direccion Actualizada");

        verify(proveedorRepository, never()).existsByDniOrRuc(any());
        verify(proveedorRepository).save(existente);
    }

    @Test
    @DisplayName("Actualizar proveedor - lanza conflicto si nuevo RUC ya existe")
    void actualizarProveedor_CuandoNuevoRucYaExiste_LanzaConflicto() {
        Proveedor existente = proveedor(1L, "20123456789", "Proveedor Antiguo", "antiguo@empresa.com",
                "Direccion Antigua", "900000000");
        ProveedorDto request = proveedorDto("20987654321", "Proveedor Nuevo", "nuevo@empresa.com",
                "Direccion Nueva", "911111111");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoRuc("20987654321")).thenThrow(new RuntimeException("Error SUNAT"));
        when(proveedorRepository.existsByDniOrRuc("20987654321")).thenReturn(true);

        ConflictoRecursoException exception = assertThrows(
                ConflictoRecursoException.class,
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
    @DisplayName("Eliminar proveedor - lanza recurso no encontrado si no existe")
    void eliminarProveedor_CuandoNoExiste_LanzaRecursoNoEncontrado() {
        when(proveedorRepository.existsById(99L)).thenReturn(false);

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> proveedorService.eliminarProveedor(99L)
        );

        assertThat(exception.getMessage()).contains("No existe");
        verify(proveedorRepository).existsById(99L);
        verify(proveedorRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Consultar RUC - retorna respuesta de SUNAT")
    void consultarRuc_CuandoRucEsValido_RetornaRespuesta() {
        RucResponse response = rucResponse("20123456789", "Empresa SAC");
        when(sunatClient.obtenerInfoRuc("20123456789")).thenReturn(response);

        RucResponse resultado = proveedorService.consultarRuc("20123456789");

        assertThat(resultado.getNumeroDocumento()).isEqualTo("20123456789");
        assertThat(resultado.getNombre()).isEqualTo("Empresa SAC");
    }

    @Test
    @DisplayName("Consultar RUC - lanza excepcion si el formato es invalido")
    void consultarRuc_CuandoRucEsInvalido_LanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> proveedorService.consultarRuc("ABC123")
        );

        assertThat(exception.getMessage()).contains("RUC invalido");
        verifyNoInteractions(sunatClient);
    }

    @Test
    void consultarRuc_CuandoEsNulo_LanzaExcepcionSinConsultarSunat() {
        assertThatThrownBy(() -> proveedorService.consultarRuc(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("RUC invalido");
        verifyNoInteractions(sunatClient);
    }

    @Test
    void consultarRuc_CuandoSunatNoEncuentraRuc_TraduceLaExcepcion() {
        when(sunatClient.obtenerInfoRuc("20123456789"))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> proveedorService.consultarRuc("20123456789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se encontro el RUC en la API gratuita");
    }

    @Test
    void consultarRuc_CuandoSunatNoPuedeProcesarlo_TraduceLaExcepcion() {
        when(sunatClient.obtenerInfoRuc("20123456789"))
                .thenThrow(mock(FeignException.UnprocessableEntity.class));

        assertThatThrownBy(() -> proveedorService.consultarRuc("20123456789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se encontro el RUC en la API gratuita");
    }

    private ProveedorDto proveedorDto(
            String dniOrRuc,
            String razonSocialONombre,
            String correoElectronico,
            String direccion,
            String telefono) {
        return ProveedorDto.builder()
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .correoElectronico(correoElectronico)
                .direccion(direccion)
                .telefono(telefono)
                .build();
    }

    private Proveedor proveedor(
            Long id,
            String dniOrRuc,
            String razonSocialONombre,
            String correoElectronico,
            String direccion,
            String telefono) {
        return Proveedor.builder()
                .id(id)
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .correoElectronico(correoElectronico)
                .direccion(direccion)
                .telefono(telefono)
                .build();
    }

    private RucResponse rucResponse(String numeroDocumento, String nombre) {
        RucResponse response = new RucResponse();
        response.setNumeroDocumento(numeroDocumento);
        response.setNombre(nombre);
        return response;
    }
}
