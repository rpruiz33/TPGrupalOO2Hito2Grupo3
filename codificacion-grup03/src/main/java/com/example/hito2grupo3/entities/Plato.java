package com.example.hito2grupo3.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "platos")
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del plato es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre del plato debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s,.-]+$", message = "El nombre del plato contiene caracteres no permitidos")
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser estrictamente mayor a cero")
    @jakarta.validation.constraints.Digits(integer = 8, fraction = 2, message = "El precio debe tener como máximo 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "El costo de producción es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo de producción debe ser estrictamente mayor a cero")
    @jakarta.validation.constraints.Digits(integer = 8, fraction = 2, message = "El costo de producción debe tener como máximo 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoProduccion;

    @AssertTrue(message = "El precio de venta debe ser mayor que el costo de producción")
    public boolean isPrecioMayorACosto() {
        if (precio == null || costoProduccion == null) {
            return true;
        }
        return precio.compareTo(costoProduccion) > 0;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidadVenta;

    public Plato(Long id, String nombre, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public Plato(Long id, String nombre, BigDecimal precio,
                 BigDecimal costoProduccion) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.costoProduccion = costoProduccion;
    }

    @AssertTrue(message = "El precio de venta debe ser mayor que el costo de producción")
    public boolean isPrecioMayorAlCosto() {
        if (precio == null || costoProduccion == null) {
            return true;
        }

        return precio.compareTo(costoProduccion) > 0;
    }
}
