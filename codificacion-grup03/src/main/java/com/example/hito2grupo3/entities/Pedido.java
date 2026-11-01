package com.example.hito2grupo3.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidadVenta;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ItemPedido> items;

    // Fecha de la transacción
    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDate fechaTransaccion;

    // Estado: A = Activo / R = Revertido
    @Column(name = "estado", nullable = false, length = 1)
    private String estado = "A";

    // Datos del registro
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Staff registradoPor;

    // Datos de la anulación
    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id")
    private Staff anuladoPor;

    public Pedido() {
        this.items = new HashSet<>();
        this.estado = "A";
    }

    public Pedido(
            Long id,
            Festival festival,
            UnidadVenta unidadVenta,
            Set<ItemPedido> items,
            LocalDate fechaTransaccion
    ) {
        this.id = id;
        this.festival = festival;
        this.unidadVenta = unidadVenta;
        this.items = items != null ? items : new HashSet<>();
        this.fechaTransaccion = fechaTransaccion;
        this.estado = "A";
    }

    public double calcularTotal() {
        return items.stream()
                .mapToDouble(item ->
                        item.calcularSubtotal().doubleValue())
                .sum();
    }

    public void agregarItem(ItemPedido item) {
        items.add(item);
    }

    public void removerItem(ItemPedido item) {
        items.remove(item);
    }
}
