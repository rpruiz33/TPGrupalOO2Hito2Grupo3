package com.example.hito2grupo3.dto;

public class ItemPedidoDTO {

    private Long platoId;
    private Integer cantidad;

    public ItemPedidoDTO() {
    }

    public Long getPlatoId() {
        return platoId;
    }

    public void setPlatoId(Long platoId) {
        this.platoId = platoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
