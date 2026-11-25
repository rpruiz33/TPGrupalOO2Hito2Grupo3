package com.example.hito2grupo3.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de error de la API")
public class ApiErrorDTO {

@JsonFormat(pattern = "dd-MM-yyyy HH:mm")
@Schema(type = "string", example = "20-11-2026 12:30")
private LocalDateTime timestamp;

    @Schema(example = "400")
    private int status;

    @Schema(example = "Bad Request")
    private String error;

    @Schema(example = "La unidad de venta no está habilitada para ese festival.")
    private String mensaje;

    @Schema(description = "Errores de validación por campo, si los hay")
    private List<String> detalles;
}
