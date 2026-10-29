package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.ItemPedidoDTO;
import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.ItemPedido;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.PlatoRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IPedidoService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final PlatoRepository platoRepository;
    private final StaffRepository staffRepository;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository,
            PlatoRepository platoRepository,
            StaffRepository staffRepository) {

        this.pedidoRepository = pedidoRepository;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.platoRepository = platoRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> obtenerPlatosPorUnidad(Long unidadVentaId) {
        UnidadVenta unidad = unidadVentaRepository
                .buscarPorIdConPlatos(unidadVentaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la unidad de venta seleccionada."));

        return unidad.getPlatosOfrecidos();
    }

    @Override
    @Transactional
    public Pedido registrarPedido(PedidoAltaDTO datos, Long staffId) {

        if (datos == null
                || datos.getFecha() == null
                || datos.getFestivalId() == null
                || datos.getUnidadVentaId() == null) {

            throw new IllegalArgumentException(
                    "Complete la fecha, el festival y la unidad de venta.");
        }

        if (datos.getItems() == null || datos.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un plato.");
        }

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el empleado autenticado."));

        LocalDate hoy = LocalDate.now(
                ZoneId.of("America/Argentina/Buenos_Aires"));

        if (!datos.getFecha().equals(hoy)) {
            throw new IllegalArgumentException(
                    "La fecha de la venta debe ser la fecha actual.");
        }

        Festival festival = festivalRepository
                .findById(datos.getFestivalId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El festival seleccionado no existe."));

        UnidadVenta unidad = unidadVentaRepository
                .buscarPorIdConPlatos(datos.getUnidadVentaId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La unidad de venta seleccionada no existe."));

        if (festival.getFechaInicio() == null
                || festival.getFechaFin() == null
                || hoy.isBefore(festival.getFechaInicio())
                || hoy.isAfter(festival.getFechaFin())) {

            throw new IllegalArgumentException(
                    "El festival no está activo en la fecha actual.");
        }

        boolean unidadAsignada = unidad.getStaffAsignado()
                .stream()
                .anyMatch(asignado ->
                        asignado.getId() == staff.getId());

        if (!unidadAsignada) {
            throw new IllegalArgumentException(
                    "No tenés asignada esta unidad de venta.");
        }

        boolean unidadHabilitada = festival.getUnidadesVentaHabilitadas()
                .stream()
                .anyMatch(u -> u.getId().equals(unidad.getId()));

        if (!unidadHabilitada) {
            throw new IllegalArgumentException(
                    "La unidad de venta no está habilitada para ese festival.");
        }

        Pedido pedido = new Pedido();
        pedido.setFestival(festival);
        pedido.setUnidadVenta(unidad);
        pedido.setFechaTransaccion(datos.getFecha());

        for (ItemPedidoDTO itemDTO : datos.getItems()) {

            if (itemDTO == null
                    || itemDTO.getPlatoId() == null
                    || itemDTO.getCantidad() == null
                    || itemDTO.getCantidad() < 1) {

                throw new IllegalArgumentException(
                        "Cada plato debe tener un identificador y una cantidad válida.");
            }

            Plato plato = platoRepository
                    .findById(itemDTO.getPlatoId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Uno de los platos seleccionados no existe."));

            boolean platoHabilitado = unidad.getPlatosOfrecidos()
                    .stream()
                    .anyMatch(p -> p.getId().equals(plato.getId()));

            if (!platoHabilitado) {
                throw new IllegalArgumentException(
                        "El plato " + plato.getNombre()
                                + " no está asociado a la unidad de venta.");
            }

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setPlato(plato);
            item.setCantidad(itemDTO.getCantidad());
            item.setPrecioUnitario(plato.getPrecio());

            pedido.agregarItem(item);
        }

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PlatoDTO obtenerPlatoEstrella(Long unidadVentaId, Long festivalId) {

        List<PlatoDTO> platos = pedidoRepository.obtenerPlatosEstrella(
                unidadVentaId, festivalId);

        if (platos.isEmpty()) {
            return null;
        }

        return platos.get(0);
    }
}