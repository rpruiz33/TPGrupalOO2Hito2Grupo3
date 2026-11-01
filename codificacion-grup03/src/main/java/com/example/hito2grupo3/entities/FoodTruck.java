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

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "food_trucks")
public class FoodTruck extends UnidadVenta {
    public static final String PREFIJO_CODIGO = "FT-";
    public static final String REGEX_CODIGO_FOOD_TRUCK = "^FT-[0-9]{7}$";

    @jakarta.validation.constraints.NotBlank(message = "La patente es obligatoria para un Food Truck")
    @jakarta.validation.constraints.Pattern(regexp = "^[A-Z0-9\\s-]{6,10}$", message = "La patente debe tener un formato válido (ej: AB123CD o ABC123)")
    private String patente;
    private boolean requiereElectricidad;

    public void setPatente(String patente) {
        this.patente = patente != null ? patente.trim().toUpperCase() : null;
    }

    public static boolean esCodigoValidoFoodTruck(String codigo) {
        if (codigo == null) {
            return false;
        }
        String limpio = codigo.trim();
        return limpio.length() == LONGITUD_CODIGO && limpio.matches(REGEX_CODIGO_FOOD_TRUCK);
    }

    @Override
    public boolean validarCodigo(String codigoAValidar) {
        return esCodigoValidoFoodTruck(codigoAValidar);
    }

    @Override
    public void setCodigo(String codigo) {
        if (!validarCodigo(codigo)) {
            throw new IllegalArgumentException(
                    "Código de Food Truck inválido: '" + codigo
                    + "'. Debe tener exactamente " + LONGITUD_CODIGO
                    + " caracteres y comenzar con '" + PREFIJO_CODIGO + "' (ej: FT-0000001).");
        }
        super.setCodigo(codigo);
    }

    public FoodTruck(Long id, String codigo, String nombreComercial, BigDecimal superficieM2,
                     List<Plato> platosOfrecidos, Set<Staff> staffAsignado,
                     Staff responsable, Set<Festival> festivales, List<Pedido> pedidos,
                     String patente, boolean requiereElectricidad) {
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
        this.patente = patente;
        this.requiereElectricidad = requiereElectricidad;
    }

}

