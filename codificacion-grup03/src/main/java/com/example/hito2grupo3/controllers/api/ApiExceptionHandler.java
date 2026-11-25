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
 * Manejador global de excepciones exclusivamente para los controladores REST del paquete api.
 * Los controladores MVC (Thymeleaf) conservan su manejo de errores habitual.
 */
@RestControllerAdvice(basePackages = "com.example.hito2grupo3.controllers.api")
public class ApiExceptionHandler {

    // Zona horaria utilizada para informar la fecha y hora del error.
    private static final ZoneId ZONA_AR = ZoneId.of("America/Argentina/Buenos_Aires");

    // Convierte los errores de validación de los campos en una respuesta HTTP 400.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> manejarValidacion(MethodArgumentNotValidException ex) {
        // Reúne los campos inválidos y sus mensajes asociados.
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Error de validación en los datos enviados.", detalles);
    }

    // Responde HTTP 400 cuando falta algún parámetro requerido en la solicitud.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorDTO> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.", null);
    }

    // Agrupa las excepciones que indican que los datos recibidos no son válidos.
    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorDTO> manejarSolicitudInvalida(Exception ex) {
        String mensaje;
        // Los argumentos o estados inválidos usan el mensaje original de la excepción.
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            mensaje = ex.getMessage();
        // Los valores con tipos incorrectos se describen indicando el parámetro afectado.
        } else if (ex instanceof MethodArgumentTypeMismatchException mm) {
            mensaje = "Valor inválido para '" + mm.getName() + "': " + mm.getValue()
                    + ". Verificá el tipo de dato o formato esperado.";
        } else {
            // Se utiliza este mensaje cuando el JSON no puede ser leído correctamente.
            mensaje = "El cuerpo de la solicitud JSON tiene un formato inválido o tipos incompatibles.";
        }
        return construir(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    // Responde HTTP 404 cuando no existe la entidad o elemento solicitado.
    @ExceptionHandler({ NoSuchElementException.class, EntityNotFoundException.class })
    public ResponseEntity<ApiErrorDTO> manejarNoEncontrado(RuntimeException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Conserva el estado HTTP y la razón indicados por ResponseStatusException.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> manejarConEstado(ResponseStatusException ex) {
        return construir(ex.getStatusCode(), ex.getReason(), null);
    }

    // Construye una respuesta uniforme con información temporal y el estado del error.
    private ResponseEntity<ApiErrorDTO> construir(
            HttpStatusCode status,
            String mensaje,
            List<String> detalles) {
        String nombre = status instanceof HttpStatus hs ? hs.getReasonPhrase() : status.toString();
        ApiErrorDTO body = new ApiErrorDTO(
                LocalDateTime.now(ZONA_AR),
                status.value(),
                nombre,
                mensaje,
                detalles);
        return ResponseEntity.status(status).body(body);
    }
}

