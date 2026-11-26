package com.example.hito2grupo3.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Staff;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pedido registrado en una unidad de venta durante un festival")
public class PedidoResponseDTO {

    @Schema(example = "15")
    private Long id;
@JsonFormat(pattern = "dd-MM-yyyy")
    @Schema(example = "20-11-2026")
    private LocalDate fechaTransaccion;

    @Schema(description = "A = Activo / R = Revertido (anulado)", example = "A")
    private String estado;

    @Schema(example = "1")
    private Long festivalId;

    @Schema(example = "Festival de Primavera")
    private String festivalNombre;

    @Schema(example = "2")
    private Long unidadVentaId;

    @Schema(example = "El Rey del Choripán")
    private String unidadVentaNombre;

    @Schema(example = "FT-0000001")
    private String unidadVentaCodigo;

    @Schema(example = "Juan Pérez")
    private String registradoPor;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
 @Schema(type = "string", example = "20-11-2026 18:45")
private LocalDateTime fechaRegistro;


    private List<ItemPedidoResponseDTO> items;

    @Schema(example = "17000.00")
    private BigDecimal total;

    public static PedidoResponseDTO desde(Pedido pedido) {

        List<ItemPedidoResponseDTO> items = pedido.getItems().stream()
                .map(ItemPedidoResponseDTO::desde)
                .sorted(Comparator.comparing(ItemPedidoResponseDTO::getPlatoNombre))
                .toList();

        BigDecimal total = items.stream()
                .map(ItemPedidoResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Staff staff = pedido.getRegistradoPor();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getFechaTransaccion(),
                pedido.getEstado(),
                pedido.getFestival().getId(),
                pedido.getFestival().getNombre(),
                pedido.getUnidadVenta().getId(),
                pedido.getUnidadVenta().getNombreComercial(),
                pedido.getUnidadVenta().getCodigo(),
                staff != null ? staff.getNombre() + " " + staff.getApellido() : null,
                pedido.getFechaRegistro(),
                items,
                total);
    }
}
