package com.example.hito2grupo3.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAltaDTO {

    @NotNull(message = "La fecha del pedido es obligatoria")
    @PastOrPresent(message = "La fecha del pedido no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Debe seleccionar un festival")
    private Long festivalId;

    @NotNull(message = "Debe seleccionar una unidad de venta")
    private Long unidadVentaId;

    @NotEmpty(message = "El pedido debe contener al menos un plato")
    @Valid
    private List<ItemPedidoDTO> items = new ArrayList<>();
}
