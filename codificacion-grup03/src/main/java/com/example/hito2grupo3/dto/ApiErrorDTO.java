package com.example.hito2grupo3.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO estándar para respuestas de error en la API REST.
 */
@Schema(description = "Estructura estándar de error devuelta por la API REST")
public record ApiErrorDTO(
        @Schema(description = "Fecha y hora del error", example = "25-11-2026 11:30")
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
        LocalDateTime timestamp,

        @Schema(description = "Código de estado HTTP", example = "400")
        int status,

        @Schema(description = "Nombre del error HTTP", example = "Bad Request")
        String error,

        @Schema(description = "Mensaje descriptivo del error", example = "La fecha de inicio debe ser anterior a la de fin.")
        String mensaje,

        @Schema(description = "Lista opcional de errores de validación por campo")
        List<String> detalles) {
}

