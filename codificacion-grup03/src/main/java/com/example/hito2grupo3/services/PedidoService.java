package com.example.hito2grupo3.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.ItemPedidoDTO;
import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.ItemPedido;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.PlatoRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final PlatoRepository platoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository,
            PlatoRepository platoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.platoRepository = platoRepository;
    }

    @Transactional(readOnly = true)
    public List<Plato> obtenerPlatosPorUnidad(Long unidadVentaId) {

        UnidadVenta unidad = unidadVentaRepository
                .buscarPorIdConPlatos(unidadVentaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la unidad de venta seleccionada."));

        return unidad.getPlatosOfrecidos();
    }

    @Transactional
    public Pedido registrarPedido(PedidoAltaDTO datos) {

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

        LocalDate hoy = LocalDate.now();

        if (!datos.getFecha().equals(hoy)) {
            throw new IllegalArgumentException(
                    "La fecha de la venta debe ser la fecha actual.");
        }

        // Buscar el festival y la unidad.
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

        // Validar el período del festival.
        if (festival.getFechaInicio() == null
                || festival.getFechaFin() == null
                || datos.getFecha().isBefore(festival.getFechaInicio())
                || datos.getFecha().isAfter(festival.getFechaFin())) {

            throw new IllegalArgumentException(
                    "La fecha de venta esta fuera del periodo del festival.");
        }

        // La unidad debe estar habilitada para el festival.
        boolean unidadHabilitada = festival.getUnidadesVentaHabilitadas()
                .stream()
                .anyMatch(u -> u.getId().equals(unidad.getId()));

        if (!unidadHabilitada) {
            throw new IllegalArgumentException(
                    "La unidad de venta no esta habilitada para ese festival.");
        }

        // Crear el pedido.
        Pedido pedido = new Pedido();
        pedido.setFestival(festival);
        pedido.setUnidadVenta(unidad);
        pedido.setFechaTransaccion(datos.getFecha());

        // Agregar los ítems y obtener los precios desde la base de datos.
        for (ItemPedidoDTO itemDTO : datos.getItems()) {

            if (itemDTO == null
                    || itemDTO.getPlatoId() == null
                    || itemDTO.getCantidad() == null
                    || itemDTO.getCantidad() < 1) {

                throw new IllegalArgumentException(
                        "Cada plato debe tener un identificador y una cantidad valida.");
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
                                + " no esta asociado a la unidad de venta.");
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
}
