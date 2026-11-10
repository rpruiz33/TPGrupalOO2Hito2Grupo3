package com.example.hito2grupo3.services.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.FestivalDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IFestivalService;
import com.example.hito2grupo3.entities.AsignacionStaff;
import com.example.hito2grupo3.repositories.AsignacionStaffRepository;


@Service
public class FestivalServiceImpl implements IFestivalService {

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final PedidoRepository pedidoRepository;
private final AsignacionStaffRepository asignacionStaffRepository;

public FestivalServiceImpl(FestivalRepository festivalRepository,
                           UnidadVentaRepository unidadVentaRepository,
                           PedidoRepository pedidoRepository,
                           AsignacionStaffRepository asignacionStaffRepository) {
    this.festivalRepository = festivalRepository;
    this.unidadVentaRepository = unidadVentaRepository;
    this.pedidoRepository = pedidoRepository;
    this.asignacionStaffRepository = asignacionStaffRepository;
}

    @Override
    @Transactional(readOnly = true)
    public List<Festival> listarTodos() {
        return festivalRepository.findAllByOrderByFechaInicioAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Festival> buscarFestivalesActivosPorUnidadVenta(Long unidadVentaId) {
        return festivalRepository.findAllByOrderByFechaInicioAsc().stream()
                .filter(Festival::isActivo)
                .filter(festival -> festival.getUnidadesVentaHabilitadas() != null
                        && festival.getUnidadesVentaHabilitadas().stream()
                                .anyMatch(unidadVenta -> unidadVentaId.equals(unidadVenta.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Festival> buscarFestivalesActivosPorUnidadVenta(Long unidadVentaId, LocalDate fecha) {
        return festivalRepository.findAllByOrderByFechaInicioAsc().stream()
                .filter(Festival::isActivo)
                .filter(festival -> festival.getFechaInicio() == null
                        || !fecha.isBefore(festival.getFechaInicio()))
                .filter(festival -> festival.getFechaFin() == null
                        || !fecha.isAfter(festival.getFechaFin()))
                .filter(festival -> festival.getUnidadesVentaHabilitadas() != null
                        && festival.getUnidadesVentaHabilitadas().stream()
                                .anyMatch(unidadVenta -> unidadVentaId.equals(unidadVenta.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FestivalDTO> listarTodosDTO() {
        return festivalRepository.findAllByOrderByFechaInicioAsc().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Festival buscarPorId(Long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el festival con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public FestivalDTO obtenerDtoPorId(Long id) {
        Festival festival = buscarPorId(id);
        return convertirADTO(festival);
    }

    @Override
    @Transactional
    public Festival crearFestival(FestivalDTO dto) {
        validarFechas(dto);

        Festival festival = new Festival();
        festival.setNombre(dto.getNombre().trim());
        festival.setFechaInicio(dto.getFechaInicio());
        festival.setFechaFin(dto.getFechaFin());
        festival.setTemporada(obtenerTemporada(dto));

        if (dto.getUnidadesVentaIds() != null && !dto.getUnidadesVentaIds().isEmpty()) {
            List<UnidadVenta> unidades = unidadVentaRepository.findAllById(dto.getUnidadesVentaIds());
            festival.setUnidadesVentaHabilitadas(new HashSet<>(unidades));
        } else {
            festival.setUnidadesVentaHabilitadas(new HashSet<>());
        }

        return festivalRepository.save(festival);
    }

    @Override
@Transactional
public Festival actualizarFestival(Long id, FestivalDTO dto) {
    validarRangoFechas(dto);

    Festival festival = buscarPorId(id);

    // Actualizar las fechas del festival
    festival.setFechaInicio(dto.getFechaInicio());
    festival.setFechaFin(dto.getFechaFin());
    festival.setTemporada(obtenerTemporada(dto));

    // Sincronizar las fechas de todas las asignaciones del Staff
    List<AsignacionStaff> asignaciones =
            asignacionStaffRepository.findByFestival_Id(id);

    for (AsignacionStaff asignacion : asignaciones) {
        asignacion.setFechaInicio(festival.getFechaInicio());
        asignacion.setFechaFin(festival.getFechaFin());
    }

    asignacionStaffRepository.saveAll(asignaciones);

    // Actualizar únicamente las unidades seleccionadas para este festival
    festival.getUnidadesVentaHabilitadas().clear();

    if (dto.getUnidadesVentaIds() != null && !dto.getUnidadesVentaIds().isEmpty()) {
        List<UnidadVenta> unidades =
                unidadVentaRepository.findAllById(dto.getUnidadesVentaIds());

        festival.getUnidadesVentaHabilitadas().addAll(
                unidades.stream()
                        .filter(UnidadVenta::isActivo)
                        .collect(Collectors.toSet()));
    }

    return festivalRepository.save(festival);
}

    /**
     * Remueve una unidad de venta únicamente de la lista de habilitadas de un festival específico.
     * No desactiva la unidad en el sistema ni afecta a otros festivales.
     */
    @Override
    @Transactional
    public void desasociarUnidadDeFestival(Long festivalId, Long unidadVentaId) {
        Festival festival = buscarPorId(festivalId);
        if (festival.getUnidadesVentaHabilitadas() != null) {
            festival.getUnidadesVentaHabilitadas().removeIf(unidad -> unidad.getId().equals(unidadVentaId));
            festivalRepository.save(festival);
        }
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        Festival festival = buscarPorId(id);
        festival.setActivo(false);
        festivalRepository.save(festival);
    }

    @Override
    @Transactional
    public void reactivar(Long id) {
        Festival festival = buscarPorId(id);
        festival.setActivo(true);
        festivalRepository.save(festival);
    }

    @Override
    @Transactional
    public void eliminarFestival(Long id) {
        Festival festival = buscarPorId(id);

        if (pedidoRepository.existsByFestival_Id(id)) {
            // Si tiene pedidos asociados, se aplica baja lógica automática
            festival.setActivo(false);
            festivalRepository.save(festival);
            return;
        }

        festival.getUnidadesVentaHabilitadas().clear();
        festival.getStaffGeneral().clear();
        festivalRepository.delete(festival);
    }

    private void validarFechas(FestivalDTO dto) {
        LocalDate hoy = LocalDate.now();

        if (dto.getFechaInicio() != null && dto.getFechaInicio().isBefore(hoy)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser anterior a hoy.");
        }

        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(hoy)) {
            throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a hoy.");
        }

        validarRangoFechas(dto);
    }

    private void validarRangoFechas(FestivalDTO dto) {
        if (dto.getFechaInicio() != null && dto.getFechaFin() != null) {
            if (!dto.getFechaFin().isAfter(dto.getFechaInicio())) {
                throw new IllegalArgumentException(
                        "La fecha de finalización debe ser por lo menos 1 día posterior a la fecha de inicio.");
            }
        }
    }

    private String obtenerTemporada(FestivalDTO dto) {
        if (dto.getFechaInicio() != null) {
            return Festival.determinarTemporada(dto.getFechaInicio());
        }
        return dto.getTemporada() != null ? dto.getTemporada().trim() : null;
    }

    private FestivalDTO convertirADTO(Festival festival) {
        FestivalDTO dto = new FestivalDTO();
        dto.setId(festival.getId());
        dto.setNombre(festival.getNombre());
        dto.setTemporada(festival.getTemporada());
        dto.setFechaInicio(festival.getFechaInicio());
        dto.setFechaFin(festival.getFechaFin());

        Set<Long> unidadIds = new HashSet<>();
        if (festival.getUnidadesVentaHabilitadas() != null) {
            unidadIds = festival.getUnidadesVentaHabilitadas().stream()
                    .filter(UnidadVenta::isActivo)
                    .map(UnidadVenta::getId)
                    .collect(Collectors.toSet());

                // Una unidad dada de baja deja de contar en el festival.
                dto.setCantidadUnidades((int) festival.getUnidadesVentaHabilitadas().stream()
                    .filter(UnidadVenta::isActivo)
                    .count());
        } else {
            dto.setCantidadUnidades(0);
        }

        dto.setUnidadesVentaIds(unidadIds);
        dto.setActivo(festival.isActivo());
        dto.calcularEstadoYDuracion();

        return dto;
    }
}
