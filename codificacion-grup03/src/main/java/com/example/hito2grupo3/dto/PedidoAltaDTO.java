package com.example.hito2grupo3.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoAltaDTO {

    private LocalDate fecha;
    private Long festivalId;
    private Long unidadVentaId;
    private List<ItemPedidoDTO> items = new ArrayList<>();

    public PedidoAltaDTO() {
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getFestivalId() {
        return festivalId;
    }

    public void setFestivalId(Long festivalId) {
        this.festivalId = festivalId;
    }

    public Long getUnidadVentaId() {
        return unidadVentaId;
    }

    public void setUnidadVentaId(Long unidadVentaId) {
        this.unidadVentaId = unidadVentaId;
    }

    public List<ItemPedidoDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoDTO> items) {
        this.items = items;
    }
}
