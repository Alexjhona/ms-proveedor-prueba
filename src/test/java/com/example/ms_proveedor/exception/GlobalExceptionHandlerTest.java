package com.example.ms_proveedor.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new ObjectMapper());

    @Test
    void validationPrioritizesRequiredMessageAndIncludesParsedBody() throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "proveedor");
        result.addError(new FieldError("proveedor", "dniOrRuc", "Formato invalido"));
        result.addError(new FieldError("proveedor", "dniOrRuc", "Campo obligatorio"));
        result.addError(new FieldError("proveedor", "correoElectronico", "Correo invalido"));
        ContentCachingRequestWrapper request = cachedRequest(
                "POST", "/api/proveedores", "{\"dniOrRuc\":\"\",\"correoElectronico\":\"x\"}");

        Map<String, Object> body = handler.handleValidationException(
                new MethodArgumentNotValidException(mock(MethodParameter.class), result), request).getBody();

        assertThat(body).containsEntry("status", 400).containsEntry("ruta", "/api/proveedores");
        assertThat(castMap(body.get("datosRecibidos"))).containsEntry("correoElectronico", "x");
        assertThat(castMap(body.get("errores")))
                .containsEntry("dniOrRuc", "Campo obligatorio")
                .containsEntry("correoElectronico", "Correo invalido");
    }

    @Test
    void unreadableBodyUsesDeepestInvalidFormatFieldAndKeepsMalformedBody() {
        InvalidFormatException cause = mock(InvalidFormatException.class);
        List<JsonMappingException.Reference> path = List.of(
                new JsonMappingException.Reference(new Object(), "proveedor"),
                new JsonMappingException.Reference(new Object(), "id"));
        when(cause.getPath()).thenReturn(path);
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMostSpecificCause()).thenReturn(cause);
        ContentCachingRequestWrapper request = cachedRequest(
                "POST", "/api/proveedores", "{malformed");

        Map<String, Object> body = handler.handleHttpMessageNotReadableException(exception, request).getBody();

        assertThat(castMap(body.get("errores"))).containsEntry("id", "Tipo de dato invalido");
        assertThat(castMap(body.get("datosRecibidos"))).containsEntry("body", "{malformed");
    }

    @Test
    void unreadableBodySupportsGenericMappingCauseAndFallbackField() {
        JsonMappingException mapping = mock(JsonMappingException.class);
        JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);
        when(reference.getFieldName()).thenReturn(null);
        when(mapping.getPath()).thenReturn(List.of(reference));
        HttpMessageNotReadableException mappedException = mock(HttpMessageNotReadableException.class);
        when(mappedException.getMostSpecificCause()).thenReturn(mapping);
        HttpMessageNotReadableException genericException = mock(HttpMessageNotReadableException.class);
        when(genericException.getMostSpecificCause()).thenReturn(new IllegalArgumentException());
        MockHttpServletRequest request = request("POST", "/api/proveedores");

        assertThat(castMap(handler.handleHttpMessageNotReadableException(mappedException, request)
                .getBody().get("errores"))).containsEntry("solicitud", "Tipo de dato invalido");
        assertThat(castMap(handler.handleHttpMessageNotReadableException(genericException, request)
                .getBody().get("errores"))).containsEntry("solicitud", "Tipo de dato invalido");
    }

    @Test
    void requestParameterErrorsDescribeMissingAndInvalidValues() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/proveedores/x");
        MissingServletRequestParameterException missing =
                new MissingServletRequestParameterException("numero", "String");
        MissingPathVariableException missingPath =
                new MissingPathVariableException("id", mock(MethodParameter.class));
        MethodArgumentTypeMismatchException mismatch = new MethodArgumentTypeMismatchException(
                "x", Long.class, "id", mock(MethodParameter.class), new NumberFormatException());

        assertThat(errors(handler.handleRequestParameterException(missing, request)))
                .containsEntry("numero", "Parametro obligatorio");
        assertThat(errors(handler.handleRequestParameterException(missingPath, request)))
                .containsEntry("id", "Parametro obligatorio");
        assertThat(errors(handler.handleRequestParameterException(mismatch, request)))
                .containsEntry("id", "Tipo de dato invalido");
    }

    @Test
    void badRequestHandlersCoverIllegalArgumentAndConstraintViolation() {
        MockHttpServletRequest request = request("GET", "/api/proveedores/ruc/x");
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("consultarRuc.ruc");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("RUC invalido");

        assertThat(errors(handler.handleIllegalArgumentException(
                new IllegalArgumentException("RUC invalido"), request)))
                .containsEntry("solicitud", "RUC invalido");
        assertThat(errors(handler.handleConstraintViolationException(
                new ConstraintViolationException(Set.of(violation)), request)))
                .containsEntry("consultarRuc.ruc", "RUC invalido");
    }

    @Test
    void statusHandlersReturnUniformBodies() {
        MockHttpServletRequest request = request("GET", "/api/proveedores/99");

        assertThat(handler.handleRecursoNoEncontrado(request).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleConflictoRecurso(request).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.handleException(request).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void dataExtractionHandlesEmptyAndBlankCachedBodies() throws Exception {
        ContentCachingRequestWrapper empty = new ContentCachingRequestWrapper(
                request("POST", "/api/proveedores"));
        ContentCachingRequestWrapper blank = cachedRequest("POST", "/api/proveedores", "   ");

        assertThat(errors(handler.handleIllegalArgumentException(new IllegalArgumentException("x"), empty)))
                .containsEntry("solicitud", "x");
        assertThat(castMap(handler.handleIllegalArgumentException(new IllegalArgumentException("x"), blank)
                .getBody().get("datosRecibidos"))).isEmpty();
    }

    @Test
    void helperFallbacksUseGenericRequestMessages() {
        String parameter = ReflectionTestUtils.invokeMethod(
                handler, "obtenerParametroConError", new RuntimeException());
        String message = ReflectionTestUtils.invokeMethod(
                handler, "obtenerMensajeParametro", new RuntimeException());

        assertThat(parameter).isEqualTo("solicitud");
        assertThat(message).isEqualTo("Parametro invalido");
    }

    private ContentCachingRequestWrapper cachedRequest(String method, String uri, String content) {
        MockHttpServletRequest raw = request(method, uri);
        raw.setCharacterEncoding(StandardCharsets.UTF_8.name());
        raw.setContent(content.getBytes(StandardCharsets.UTF_8));
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(raw);
        try {
            wrapper.getInputStream().readAllBytes();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        return wrapper;
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        return request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private Map<String, Object> errors(org.springframework.http.ResponseEntity<Map<String, Object>> response) {
        return castMap(response.getBody().get("errores"));
    }
}
