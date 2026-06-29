package com.example.ms_proveedor.exception;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private static final String VALIDATION_ERROR_MESSAGE = "Se encontraron errores de validación";
    private static final String REQUEST_FIELD = "solicitud";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error -> {
            String campo = error.getField();
            String mensaje = error.getDefaultMessage();
            if (!errores.containsKey(campo) || "Campo obligatorio".equals(mensaje)) {
                errores.put(campo, mensaje);
            }
        });

        return ResponseEntity.badRequest().body(badRequestBody(
                VALIDATION_ERROR_MESSAGE,
                request,
                errores
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        String campo = obtenerCampoConError(exception);
        errores.put(campo, "Tipo de dato invalido");

        return ResponseEntity.badRequest().body(badRequestBody(
                VALIDATION_ERROR_MESSAGE,
                request,
                errores
        ));
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> handleRequestParameterException(
            Exception exception,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put(obtenerParametroConError(exception), obtenerMensajeParametro(exception));

        return ResponseEntity.badRequest().body(badRequestBody(
                VALIDATION_ERROR_MESSAGE,
                request,
                errores
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put(REQUEST_FIELD, exception.getMessage());

        return ResponseEntity.badRequest().body(badRequestBody(
                VALIDATION_ERROR_MESSAGE,
                request,
                errores
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errores.put(violation.getPropertyPath().toString(), violation.getMessage()));

        return ResponseEntity.badRequest().body(badRequestBody(
                VALIDATION_ERROR_MESSAGE,
                request,
                errores
        ));
    }

    @ExceptionHandler({
            RecursoNoEncontradoException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleRecursoNoEncontrado(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(
                HttpStatus.NOT_FOUND,
                "No se encontró el recurso solicitado",
                request
        ));
    }

    @ExceptionHandler({
            ConflictoRecursoException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<Map<String, Object>> handleConflictoRecurso(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(
                HttpStatus.CONFLICT,
                "El registro ya existe o genera conflicto",
                request
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception exception, HttpServletRequest request) {
        log.error("Error inesperado procesando {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado en el servidor",
                request
        ));
    }

    private Map<String, Object> badRequestBody(
            String mensaje,
            HttpServletRequest request,
            Map<String, String> errores) {
        Map<String, Object> body = errorBody(HttpStatus.BAD_REQUEST, mensaje, request);
        body.put("datosRecibidos", obtenerDatosRecibidos(request));
        body.put("errores", errores);
        return body;
    }

    private Map<String, Object> errorBody(HttpStatus status, String mensaje, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().withNano(0).toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        body.put("ruta", request.getRequestURI());
        return body;
    }

    private Map<String, Object> obtenerDatosRecibidos(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return Collections.emptyMap();
        }

        byte[] contenido = wrapper.getContentAsByteArray();
        if (contenido.length == 0) {
            return Collections.emptyMap();
        }

        Charset charset = Charset.forName(wrapper.getCharacterEncoding());
        String body = new String(contenido, charset);
        if (body.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(body, MAP_TYPE);
        } catch (Exception ex) {
            return Map.of("body", body);
        }
    }

    private String obtenerCampoConError(HttpMessageNotReadableException exception) {
        Throwable causa = exception.getMostSpecificCause();
        if (causa instanceof InvalidFormatException invalidFormatException) {
            return obtenerCampoDesdeRuta(invalidFormatException);
        }
        if (causa instanceof JsonMappingException jsonMappingException) {
            return obtenerCampoDesdeRuta(jsonMappingException);
        }
        return REQUEST_FIELD;
    }

    private String obtenerCampoDesdeRuta(JsonMappingException exception) {
        return exception.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(Objects::nonNull)
                .reduce((anterior, actual) -> actual)
                .orElse(REQUEST_FIELD);
    }

    private String obtenerParametroConError(Exception exception) {
        if (exception instanceof MissingServletRequestParameterException missingParameter) {
            return missingParameter.getParameterName();
        }
        if (exception instanceof MissingPathVariableException missingPathVariable) {
            return missingPathVariable.getVariableName();
        }
        if (exception instanceof MethodArgumentTypeMismatchException typeMismatch) {
            return typeMismatch.getName();
        }
        return REQUEST_FIELD;
    }

    private String obtenerMensajeParametro(Exception exception) {
        if (exception instanceof MissingServletRequestParameterException
                || exception instanceof MissingPathVariableException) {
            return "Parametro obligatorio";
        }
        if (exception instanceof MethodArgumentTypeMismatchException) {
            return "Tipo de dato invalido";
        }
        return "Parametro invalido";
    }
}
