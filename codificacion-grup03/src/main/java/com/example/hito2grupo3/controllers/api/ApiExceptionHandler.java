package com.example.hito2grupo3.controllers.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import com.example.hito2grupo3.dto.ApiErrorDTO;

/**
 * Manejo de errores solo para los controladores REST del paquete api.
 * Los controladores MVC (Thymeleaf) siguen con su manejo habitual.
 */
@RestControllerAdvice(basePackages = "com.example.hito2grupo3.controllers.api")
public class ApiExceptionHandler {

    private static final ZoneId ZONA_AR = ZoneId.of("America/Argentina/Buenos_Aires");

    // Convierte los errores de validación de los campos en una respuesta HTTP 400.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos del pedido inválidos.", detalles);
    }

@ExceptionHandler({ IllegalArgumentException.class, HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class })
public ResponseEntity<ApiErrorDTO> solicitudInvalida(Exception ex) {
    String mensaje;
    if (ex instanceof IllegalArgumentException) {
        mensaje = ex.getMessage();
    } else if (ex instanceof MethodArgumentTypeMismatchException mm) {
        mensaje = "Valor inválido para '" + mm.getName() + "': " + mm.getValue()
                + ". Las fechas van con formato dd-MM-yyyy.";
    } else {
        mensaje = "La solicitud tiene un formato inválido (revisá los tipos de datos).";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorDTO> parametroFaltante(MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.", null);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorDTO> manejarSolicitudInvalida(Exception ex) {
        String mensaje;
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            mensaje = ex.getMessage();
        } else if (ex instanceof MethodArgumentTypeMismatchException mm) {
            mensaje = "Valor inválido para '" + mm.getName() + "': " + mm.getValue()
                    + ". Verificá el tipo de dato o formato esperado.";
        } else {
            mensaje = "El cuerpo de la solicitud JSON tiene un formato inválido o tipos incompatibles.";
        }
        return construir(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    @ExceptionHandler({ NoSuchElementException.class, EntityNotFoundException.class })
    public ResponseEntity<ApiErrorDTO> manejarNoEncontrado(RuntimeException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Conserva el estado HTTP y la razón indicados por ResponseStatusException.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> conEstado(ResponseStatusException ex) {
        return construir(ex.getStatusCode(), ex.getReason(), null);
    }

    private ResponseEntity<ApiErrorDTO> construir(
            HttpStatusCode status,
            String mensaje,
            List<String> detalles) {
        String nombre = status instanceof HttpStatus hs ? hs.getReasonPhrase() : status.toString();
        ApiErrorDTO body = new ApiErrorDTO(LocalDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")), status.value(), nombre, mensaje, detalles);
        return ResponseEntity.status(status).body(body);
    }
}
