package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

public class PlatoDTO {

    private Long id;
    private String nombre;
    private Long cantidadVendida;
    private BigDecimal recaudacionTotal;

    public PlatoDTO() {
    }

    public PlatoDTO(Long id, String nombre, Long cantidadVendida,
                    BigDecimal recaudacionTotal) {
        this.id = id;
        this.nombre = nombre;
        this.cantidadVendida = cantidadVendida;
        this.recaudacionTotal = recaudacionTotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getRecaudacionTotal() {
        return recaudacionTotal;
    }

    public void setRecaudacionTotal(BigDecimal recaudacionTotal) {
        this.recaudacionTotal = recaudacionTotal;
    }
}