package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;

import java.util.List;
import java.util.Map;

public interface IPedidoService {

    List<Plato> obtenerPlatosPorUnidad(Long unidadVentaId);

    Pedido registrarPedido(PedidoAltaDTO datos, Long staffId);

    PlatoDTO obtenerPlatoEstrella(Long unidadVentaId, Long festivalId);

    // Historial de pedidos de las unidades asignadas al empleado
    List<Pedido> obtenerHistorialPorStaff(Long staffId);

    // Anulación interna de un pedido
    void anularPedido(Long pedidoId, Long staffId, String motivo);

    Pedido obtenerPedidoPorStaff(Long pedidoId, Long staffId);

    Staff obtenerStaffPorEmail(String email);

    List<Festival> obtenerFestivalesDisponiblesParaAlta(Long staffId);

    List<UnidadVenta> obtenerUnidadesDisponiblesPorFestival(Long festivalId, Long staffId);

    void verificarAccesoUnidad(Long unidadId, Long staffId);

    PlatoDTO obtenerPlatoEstrellaValidado(Long unidadVentaId, Long festivalId, Long staffId);

    Map<String, Object> obtenerDatosHistorial(Long staffId);

    List<Festival> obtenerFestivalesPorUnidadYStaff(Long unidadId, Long staffId);
}
