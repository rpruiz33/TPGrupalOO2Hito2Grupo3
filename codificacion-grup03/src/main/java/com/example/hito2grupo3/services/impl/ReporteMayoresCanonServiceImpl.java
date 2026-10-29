package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.ReporteMayoresCanonDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.services.IReporteMayoresCanonService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ReporteMayoresCanonServiceImpl
        implements IReporteMayoresCanonService {

    private final FestivalRepository festivalRepository;

    public ReporteMayoresCanonServiceImpl(
            FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> obtenerMayoresCanon(int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de unidades debe ser mayor a cero.");
        }

        LocalDate hoy = LocalDate.now();

        Festival festivalActivo = festivalRepository
                .findFirstByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        hoy, hoy)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay un festival activo en la fecha actual."));

        return festivalActivo.getUnidadesVentaHabilitadas()
                .stream()
                .sorted(Comparator.comparing(
                        UnidadVenta::calcularCanon,
                        Comparator.reverseOrder()))
                .limit(cantidad)
                .map(unidad -> new ReporteMayoresCanonDTO(
                        unidad.getNombreComercial(),
                        unidad.getCodigo(),
                        obtenerTipoUnidad(unidad),
                        unidad.calcularCanon()))
                .toList();
    }

    private String obtenerTipoUnidad(UnidadVenta unidad) {
        if (unidad instanceof FoodTruck) {
            return "Food Truck";
        }

        if (unidad instanceof PuestoDesarmable) {
            return "Puesto Desarmable";
        }

        return unidad.getClass().getSimpleName();
    }
}