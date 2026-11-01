
package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Plato;

import java.util.List;

public interface IPedidoService {

    List<Plato> obtenerPlatosPorUnidad(Long unidadVentaId);

    Pedido registrarPedido(PedidoAltaDTO datos, Long staffId);

    PlatoDTO obtenerPlatoEstrella(Long unidadVentaId, Long festivalId);

    // Historial de pedidos de las unidades asignadas al empleado
    List<Pedido> obtenerHistorialPorStaff(Long staffId);

    // Anulación interna de un pedido
    void anularPedido(Long pedidoId, Long staffId, String motivo);
    Pedido obtenerPedidoPorStaff(Long pedidoId, Long staffId);
}
