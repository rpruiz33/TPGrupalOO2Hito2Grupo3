package com.example.hito2grupo3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@Setter 
@Getter 

@Table(name = "puestos_desarmables")
public class PuestoDesarmable extends UnidadVenta {
    public static final String PREFIJO_CODIGO = "PD-";
    public static final String REGEX_CODIGO_PUESTO = "^PD-[0-9]{7}$";

    private Integer cantidadCarpas;
    private Integer tiempoMontajeMin;

    public static boolean esCodigoValidoPuesto(String codigo) {
        if (codigo == null) {
            return false;
        }
        String limpio = codigo.trim();
        return limpio.length() == LONGITUD_CODIGO && limpio.matches(REGEX_CODIGO_PUESTO);
    }

    @Override
    public boolean validarCodigo(String codigoAValidar) {
        return esCodigoValidoPuesto(codigoAValidar);
    }

    @Override
    public void setCodigo(String codigo) {
        if (!validarCodigo(codigo)) {
            throw new IllegalArgumentException(
                    "Código de Puesto Desarmable inválido: '" + codigo
                    + "'. Debe tener exactamente " + LONGITUD_CODIGO
                    + " caracteres y comenzar con '" + PREFIJO_CODIGO + "' (ej: PD-0000001).");
        }
        super.setCodigo(codigo);
    }

    public PuestoDesarmable(Long id, String codigo, String nombreComercial, BigDecimal superficieM2,
                            List<Plato> platosOfrecidos, Set<Staff> staffAsignado,
                            Staff responsable, Set<Festival> festivales, List<Pedido> pedidos,
                            Integer cantidadCarpas, Integer tiempoMontajeMin) {
        super();
        setId(id);
        setCodigo(codigo);
        setNombreComercial(nombreComercial);
        setSuperficieM2(superficieM2);
        setPlatosOfrecidos(platosOfrecidos != null ? platosOfrecidos : new ArrayList<>());
        setStaffAsignado(staffAsignado != null ? staffAsignado : new HashSet<>());
        setResponsable(responsable);
        setFestivales(festivales != null ? festivales : new HashSet<>());
        setPedidos(pedidos != null ? pedidos : new ArrayList<>());
        this.cantidadCarpas = cantidadCarpas;
        this.tiempoMontajeMin = tiempoMontajeMin;
    }

@Override
public BigDecimal calcularCanon() {
    return getSuperficieM2()
            .multiply(BigDecimal.valueOf(500))
            .subtract(BigDecimal.valueOf(getTiempoMontajeMin())
                    .multiply(BigDecimal.valueOf(10)));
}
}
