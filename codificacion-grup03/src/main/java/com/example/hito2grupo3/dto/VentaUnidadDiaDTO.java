package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

/**
 * DTO que representa el resumen de ventas de una unidad de venta en un día determinado.
 * Se usa en el cierre de caja para mostrar al cajero lo vendido en su unidad.
 */
public class VentaUnidadDiaDTO {

    private String nombrePlato;
    private Integer cantidadVendida;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public VentaUnidadDiaDTO(String nombrePlato, Long cantidadVendida, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.nombrePlato = nombrePlato;
        this.cantidadVendida = cantidadVendida != null ? cantidadVendida.intValue() : 0;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public String getNombrePlato() { return nombrePlato; }
    public Integer getCantidadVendida() { return cantidadVendida; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
