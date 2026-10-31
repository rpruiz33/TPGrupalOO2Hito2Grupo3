package com.example.hito2grupo3.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cierres_caja")
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha del cierre es obligatoria")
    @PastOrPresent(message = "La fecha de cierre no puede ser futura")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "El monto recaudado es obligatorio")
    @PositiveOrZero(message = "El monto recaudado debe ser mayor o igual a cero")
    @Digits(integer = 10, fraction = 2, message = "El monto solo puede tener hasta 2 decimales")
    @Column(name = "monto_recaudado", nullable = false)
    private BigDecimal montoRecaudado;

    // Relación con Cajero (Muchos CierreCaja pertenecen a un Cajero/Staff)
    @NotNull(message = "El cajero es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cajero_id", nullable = false)
    private Cajero cajero; // O Staff si Cajero hereda de Staff

    // Relación con UnidadVenta (Muchos CierreCaja pertenecen a una UnidadVenta)
    @NotNull(message = "La unidad de venta es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidadVenta;

    public CierreCaja(Long id, LocalDate fecha, BigDecimal montoRecaudado,
                      Cajero cajero, UnidadVenta unidadVenta) {
        this.id = id;
        this.fecha = fecha;
        this.montoRecaudado = montoRecaudado;
        this.cajero = cajero;
        this.unidadVenta = unidadVenta;
    }
}