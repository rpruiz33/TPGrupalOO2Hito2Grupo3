package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

public class RankingUnidadDTO {

    private Long unidadVentaId;
    private String nombreComercial;
    private String codigo;
    private String tipoUnidad;
    private BigDecimal recaudacion;

    public RankingUnidadDTO(Long unidadVentaId, String nombreComercial, String codigo, String tipoUnidad, BigDecimal recaudacion) {
        this.unidadVentaId = unidadVentaId;
        this.nombreComercial = nombreComercial;
        this.codigo = codigo;
        this.tipoUnidad = tipoUnidad;
        this.recaudacion = recaudacion;
    }

    public RankingUnidadDTO(Long unidadVentaId, String nombreComercial, String codigo, String tipoUnidad, Number recaudacion) {
        this.unidadVentaId = unidadVentaId;
        this.nombreComercial = nombreComercial;
        this.codigo = codigo;
        this.tipoUnidad = tipoUnidad;
        this.recaudacion = recaudacion != null ? BigDecimal.valueOf(recaudacion.doubleValue()) : BigDecimal.ZERO;
    }

    public Long getUnidadVentaId() {
        return unidadVentaId;
    }

    public void setUnidadVentaId(Long unidadVentaId) {
        this.unidadVentaId = unidadVentaId;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipoUnidad() {
        return tipoUnidad;
    }

    public void setTipoUnidad(String tipoUnidad) {
        this.tipoUnidad = tipoUnidad;
    }

    public BigDecimal getRecaudacion() {
        return recaudacion;
    }

    public void setRecaudacion(BigDecimal recaudacion) {
        this.recaudacion = recaudacion;
    }
}
