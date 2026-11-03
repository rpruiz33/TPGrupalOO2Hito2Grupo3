package com.example.hito2grupo3.services.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IModificarUnidadVentaService;

@Service
public class ModificarUnidadVentaServiceImpl implements IModificarUnidadVentaService {

    private final UnidadVentaRepository unidadVentaRepository;

    public ModificarUnidadVentaServiceImpl(UnidadVentaRepository unidadVentaRepository) {
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Override
    @Transactional
    public void modificar(
            Long id,
            String nombreComercial,
            BigDecimal superficieM2,
            String patente,
            Boolean requiereElectricidad,
            Integer cantidadCarpas,
            Integer tiempoMontajeMin) {

        UnidadVenta unidad = unidadVentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la unidad de venta seleccionada."));

        // Normalizar y validar datos comunes
        nombreComercial = nombreComercial == null ? "" : nombreComercial.trim();

        if (nombreComercial.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre comercial es obligatorio.");
        }

        if (superficieM2 == null || superficieM2.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La superficie debe ser mayor que cero.");
        }

        // Actualizar datos comunes
        unidad.setNombreComercial(nombreComercial);
        unidad.setSuperficieM2(superficieM2);

        // Actualizar datos específicos según el tipo
        if (unidad instanceof FoodTruck foodTruck) {

            patente = patente == null ? "" : patente.trim();

            if (patente.isBlank()) {
                throw new IllegalArgumentException(
                        "La patente es obligatoria para un Food Truck.");
            }

            foodTruck.setPatente(patente);
            foodTruck.setRequiereElectricidad(
                    Boolean.TRUE.equals(requiereElectricidad));

        } else if (unidad instanceof PuestoDesarmable puesto) {

            if (cantidadCarpas == null || cantidadCarpas <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad de carpas debe ser mayor que cero.");
            }

            if (tiempoMontajeMin == null || tiempoMontajeMin <= 0) {
                throw new IllegalArgumentException(
                        "El tiempo de montaje debe ser mayor que cero.");
            }

            puesto.setCantidadCarpas(cantidadCarpas);
            puesto.setTiempoMontajeMin(tiempoMontajeMin);
        }

        unidadVentaRepository.save(unidad);
    }
}