package com.example.hito2grupo3.services.impl;

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

@Service
public class FestivalServiceImpl implements IFestivalService {

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final PedidoRepository pedidoRepository;

    public FestivalServiceImpl(FestivalRepository festivalRepository,
                               UnidadVentaRepository unidadVentaRepository,
                               PedidoRepository pedidoRepository) {
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Festival> listarTodos() {
        return festivalRepository.findAllByOrderByFechaInicioAsc();
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
        validarFechas(dto);

        Festival festival = buscarPorId(id);
        festival.setNombre(dto.getNombre().trim());
        festival.setFechaInicio(dto.getFechaInicio());
        festival.setFechaFin(dto.getFechaFin());

        festival.getUnidadesVentaHabilitadas().clear();
        if (dto.getUnidadesVentaIds() != null && !dto.getUnidadesVentaIds().isEmpty()) {
            List<UnidadVenta> unidades = unidadVentaRepository.findAllById(dto.getUnidadesVentaIds());
            festival.getUnidadesVentaHabilitadas().addAll(unidades);
        }

        return festivalRepository.save(festival);
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
        if (dto.getFechaInicio() != null && dto.getFechaFin() != null) {
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a la fecha de inicio.");
            }
        }
    }

    private FestivalDTO convertirADTO(Festival festival) {
        FestivalDTO dto = new FestivalDTO();
        dto.setId(festival.getId());
        dto.setNombre(festival.getNombre());
        dto.setFechaInicio(festival.getFechaInicio());
        dto.setFechaFin(festival.getFechaFin());

        Set<Long> unidadIds = new HashSet<>();
        if (festival.getUnidadesVentaHabilitadas() != null) {
            unidadIds = festival.getUnidadesVentaHabilitadas().stream()
                    .map(UnidadVenta::getId)
                    .collect(Collectors.toSet());
            dto.setCantidadUnidades(festival.getUnidadesVentaHabilitadas().size());
        }
        dto.setUnidadesVentaIds(unidadIds);
        dto.setActivo(festival.isActivo());
        dto.calcularEstadoYDuracion();

        return dto;
    }
}
