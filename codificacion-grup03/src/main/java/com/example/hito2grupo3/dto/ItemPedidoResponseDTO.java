package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

import com.example.hito2grupo3.entities.ItemPedido;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Plato incluido en un pedido")
public class ItemPedidoResponseDTO {

    @Schema(example = "3")
    private Long platoId;

    @Schema(example = "Hamburguesa completa")
    private String platoNombre;

    @Schema(example = "2")
    private Integer cantidad;

    @Schema(description = "Precio del plato al momento de la venta", example = "8500.00")
    private BigDecimal precioUnitario;

    @Schema(example = "17000.00")
    private BigDecimal subtotal;

    public static ItemPedidoResponseDTO desde(ItemPedido item) {
        return new ItemPedidoResponseDTO(
                item.getPlato().getId(),
                item.getPlato().getNombre(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.calcularSubtotal());
    }
}
