package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Plato;
import java.util.List;

public interface IPedidoService {

    List<Plato> obtenerPlatosPorUnidad(Long unidadVentaId);

    Pedido registrarPedido(PedidoAltaDTO datos, Long staffId);
}
