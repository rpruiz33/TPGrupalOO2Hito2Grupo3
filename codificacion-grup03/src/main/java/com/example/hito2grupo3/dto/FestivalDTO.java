package com.example.hito2grupo3.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.hito2grupo3.entities.Festival;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos del festival gastronómico")
public class FestivalDTO {

    @Schema(description = "ID del festival (autogenerado)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre del festival es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s,.'()\\-]+$", message = "El nombre contiene caracteres no permitidos")
    @Schema(description = "Nombre del festival", example = "Epicentro Gourmet Primavera 2026")
    private String nombre;

    @NotBlank(message = "La temporada es obligatoria")
    @Pattern(regexp = "Invierno|Primavera|Verano|Otoño", message = "La temporada debe ser Invierno, Primavera, Verano u Otoño")
    @Schema(description = "Temporada del festival", example = "Primavera", allowableValues = { "Invierno", "Primavera", "Verano", "Otoño" })
    private String temporada;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Fecha de inicio (YYYY-MM-DD)", example = "2026-11-01")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Fecha de fin (YYYY-MM-DD)", example = "2026-11-15")
    private LocalDate fechaFin;

    @Schema(description = "IDs de las unidades de venta participantes", example = "[1, 2]")
    private Set<Long> unidadesVentaIds = new HashSet<>();

    @Schema(description = "Cantidad de unidades participantes", accessMode = Schema.AccessMode.READ_ONLY)
    private int cantidadUnidades;

    @Schema(description = "Estado lógico del festival", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean activo = true;

    @Schema(description = "Estado operativo calculado", accessMode = Schema.AccessMode.READ_ONLY)
    private String estado;

    @Schema(description = "Duración en días", accessMode = Schema.AccessMode.READ_ONLY)
    private long diasDuracion;

    @AssertTrue(message = "La fecha de inicio debe ser anterior o igual a la fecha de finalización")
    @Schema(hidden = true)
    public boolean isRangoFechasValido() {
        if (fechaInicio == null || fechaFin == null) {
            return true;
        }
        return !fechaInicio.isAfter(fechaFin);
    }

    public FestivalDTO(Long id, String nombre, String temporada, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.nombre = nombre;
        this.temporada = temporada;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = true;
        calcularEstadoYDuracion();
    }

    public FestivalDTO(Long id, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this(id, nombre, fechaInicio != null ? Festival.determinarTemporada(fechaInicio) : null, fechaInicio, fechaFin);
    }

    public void calcularEstadoYDuracion() {
        if (fechaInicio != null && fechaFin != null) {
            this.diasDuracion = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        }
        if (!this.activo) {
            this.estado = "Dado de baja";
        } else if (fechaInicio != null && fechaFin != null) {
            LocalDate hoy = LocalDate.now();
            if (hoy.isAfter(fechaFin)) {
                this.estado = "Finalizado";
            } else if (hoy.isBefore(fechaInicio)) {
                this.estado = "Próximo";
            } else {
                this.estado = "En curso";
            }
        } else {
            this.estado = "Dado de alta";
        }
    }
}
