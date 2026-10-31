package com.example.hito2grupo3.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class FestivalDTO {

    private Long id;

    @NotBlank(message = "El nombre del festival es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre del festival debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s,.-]+$", message = "El nombre del festival contiene caracteres no permitidos")
    private String nombre;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    private Set<Long> unidadesVentaIds = new HashSet<>();

    private int cantidadUnidades;

    private boolean activo = true;

    private String estado;

    private long diasDuracion;

    @AssertTrue(message = "La fecha de inicio debe ser anterior o igual a la fecha de finalización")
    public boolean isRangoFechasValido() {
        if (fechaInicio == null || fechaFin == null) {
            return true;
        }
        return !fechaInicio.isAfter(fechaFin);
    }

    public FestivalDTO(Long id, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.nombre = nombre != null ? nombre.trim() : null;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = true;
        calcularEstadoYDuracion();
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
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
