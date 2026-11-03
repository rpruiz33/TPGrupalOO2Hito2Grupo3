package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaCocinaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotNull(message = "El plus fijo es obligatorio")
    @DecimalMin(value = "0.00", message = "El plus fijo debe ser mayor o igual a cero")
    @Digits(integer = 10, fraction = 2, message = "El plus solo puede tener hasta 2 decimales")
    private BigDecimal plusFijo;

    private boolean activo = true;

    private Long cantidadCocinerosAsociados = 0L;

    public CategoriaCocinaDTO(Long id, String nombre, BigDecimal plusFijo) {
        this.id = id;
        this.nombre = nombre;
        this.plusFijo = plusFijo;
        this.activo = true;
        this.cantidadCocinerosAsociados = 0L;
    }

    public CategoriaCocinaDTO(Long id, String nombre, BigDecimal plusFijo, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.plusFijo = plusFijo;
        this.activo = activo;
        this.cantidadCocinerosAsociados = 0L;
    }
}
