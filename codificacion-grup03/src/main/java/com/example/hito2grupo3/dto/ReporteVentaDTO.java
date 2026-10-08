package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

public class ReporteVentaDTO {

    private Long festivalId;
    private String nombreFestival;
    private BigDecimal recaudacionTotal;
    private Long cantidadPedidos;

    public ReporteVentaDTO(Long festivalId, String nombreFestival, BigDecimal recaudacionTotal, Long cantidadPedidos) {
        this.festivalId = festivalId;
        this.nombreFestival = nombreFestival;
        this.recaudacionTotal = recaudacionTotal;
        this.cantidadPedidos = cantidadPedidos;
    }

    public Long getFestivalId() {
        return festivalId;
    }

    public void setFestivalId(Long festivalId) {
        this.festivalId = festivalId;
    }

    public String getNombreFestival() {
        return nombreFestival;
    }

    public void setNombreFestival(String nombreFestival) {
        this.nombreFestival = nombreFestival;
    }

    public BigDecimal getRecaudacionTotal() {
        return recaudacionTotal;
    }

    public void setRecaudacionTotal(BigDecimal recaudacionTotal) {
        this.recaudacionTotal = recaudacionTotal;
    }

    public Long getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(Long cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }
}
