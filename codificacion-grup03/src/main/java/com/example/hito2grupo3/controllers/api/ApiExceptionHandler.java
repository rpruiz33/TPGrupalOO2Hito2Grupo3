package com.example.hito2grupo3.controllers.api;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.example.hito2grupo3.dto.ApiErrorDTO;

import jakarta.persistence.EntityNotFoundException;

/**
 * Manejador global de excepciones de los controladores REST ubicados en
 * {@code com.example.hito2grupo3.controllers.api}. Los controladores MVC
 * tradicionales conservan su propio manejo de errores.
 */
@RestControllerAdvice(basePackages = "com.example.hito2grupo3.controllers.api")
public class ApiExceptionHandler {

    // Zona horaria usada al generar la fecha de cada respuesta de error.
    private static final ZoneId ZONA_AR = ZoneId.of("America/Argentina/Buenos_Aires");

    // Convierte los errores de validación de DTOs en una respuesta HTTP 400.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return construir(HttpStatus.BAD_REQUEST,
                "Datos inválidos en la solicitud.", detalles);
    }

    // Responde HTTP 400 cuando falta un parámetro obligatorio de consulta.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorDTO> manejarParametroFaltante(
            MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.", null);
    }

    // Agrupa los errores derivados de datos, formatos o estados inválidos.
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
        } else if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            mensaje = "Valor inválido para '" + mismatch.getName() + "': "
                    + mismatch.getValue()
                    + ". Verificá el tipo de dato o formato esperado.";
        } else {
            mensaje = "El cuerpo JSON no tiene un formato válido o contiene tipos incompatibles.";
        }

        return construir(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    // Devuelve HTTP 404 cuando una entidad o recurso solicitado no existe.
    @ExceptionHandler({ NoSuchElementException.class, EntityNotFoundException.class })
    public ResponseEntity<ApiErrorDTO> manejarNoEncontrado(RuntimeException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Conserva el código HTTP y el motivo definidos por ResponseStatusException.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> manejarConEstado(ResponseStatusException ex) {
        return construir(ex.getStatusCode(), ex.getReason(), null);
    }

    // Construye una respuesta uniforme para todos los errores de la API.
    private ResponseEntity<ApiErrorDTO> construir(
            HttpStatusCode status,
            String mensaje,
            List<String> detalles) {
        String nombre = status instanceof HttpStatus httpStatus
                ? httpStatus.getReasonPhrase()
                : status.toString();

        ApiErrorDTO body = new ApiErrorDTO(
                LocalDateTime.now(ZONA_AR),
                status.value(),
                nombre,
                mensaje,
                detalles);

        return ResponseEntity.status(status).body(body);
    }
}
