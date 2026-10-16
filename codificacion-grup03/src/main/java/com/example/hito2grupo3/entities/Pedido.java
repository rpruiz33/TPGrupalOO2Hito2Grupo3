package com.example.hito2grupo3.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Data
@Getter 
@Setter 
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

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemPedido> items;

    private LocalDate fechaTransaccion;

    public Pedido() {
        this.items = new HashSet<>();
    }

    public Pedido(Long id, Festival festival, UnidadVenta unidadVenta,
                  Set<ItemPedido> items, LocalDate fechaTransaccion) {
        this.id = id;
        this.festival = festival;
        this.unidadVenta = unidadVenta;
        this.items = items != null ? items : new HashSet<>();
        this.fechaTransaccion = fechaTransaccion;
    }

    public double calcularTotal() {
    return items.stream()
            .mapToDouble(item -> item.calcularSubtotal().doubleValue())
            .sum();
}

public void agregarItem(ItemPedido item) {
    items.add(item);
    item.setPedido(this);
}

public void removerItem(ItemPedido item) {
    items.remove(item);
    item.setPedido(null);
}

}
