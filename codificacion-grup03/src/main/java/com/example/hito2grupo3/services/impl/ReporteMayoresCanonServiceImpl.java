package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.ReporteMayoresCanonDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.services.ICanonService;
import com.example.hito2grupo3.services.IReporteMayoresCanonService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ReporteMayoresCanonServiceImpl implements IReporteMayoresCanonService {

    private final FestivalRepository festivalRepository;
    private final ICanonService canonService;

    public ReporteMayoresCanonServiceImpl(
            FestivalRepository festivalRepository,
            ICanonService canonService) {
        this.festivalRepository = festivalRepository;
        this.canonService = canonService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> obtenerMayoresCanon(int cantidad) {
        return obtenerMayoresCanon(null, cantidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> obtenerMayoresCanon(Long festivalId, int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de unidades debe ser mayor a cero.");
        }

        Festival festival;
        if (festivalId != null) {
            festival = festivalRepository.findById(festivalId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El festival seleccionado no existe."));
        } else {
            LocalDate hoy = LocalDate.now();
            festival = festivalRepository
                    .findFirstByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(hoy, hoy)
                    .orElseGet(() -> festivalRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "No hay festivales registrados en el sistema.")));
        }

        if (festival.getUnidadesVentaHabilitadas() == null || festival.getUnidadesVentaHabilitadas().isEmpty()) {
            return List.of();
        }

        return festival.getUnidadesVentaHabilitadas()
                .stream()
                .filter(UnidadVenta::isActivo)
                .sorted(Comparator.comparing(
                        canonService::calcularCanon,
                        Comparator.reverseOrder()))
                .limit(cantidad)
                .map(unidad -> new ReporteMayoresCanonDTO(
                        unidad.getNombreComercial(),
                        unidad.getCodigo(),
                        obtenerTipoUnidad(unidad),
                        canonService.calcularCanon(unidad)))
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
