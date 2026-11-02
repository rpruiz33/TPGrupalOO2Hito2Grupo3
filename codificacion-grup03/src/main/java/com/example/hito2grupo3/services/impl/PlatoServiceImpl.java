package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.PlatoRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IPlatoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlatoServiceImpl implements IPlatoService {

    private final PlatoRepository platoRepository;
    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public PlatoServiceImpl(
            PlatoRepository platoRepository,
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.platoRepository = platoRepository;
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> listarPorUnidad(Long unidadVentaId) {
        return platoRepository.findByUnidadVenta_IdOrderByNombreAsc(unidadVentaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Plato buscarPorIdYUnidad(Long platoId, Long unidadVentaId) {
        Plato plato = platoRepository.findById(platoId).orElse(null);
        if (plato == null
                || plato.getUnidadVenta() == null
                || !plato.getUnidadVenta().getId().equals(unidadVentaId)) {
            throw new IllegalArgumentException(
                    "El plato no existe o no pertenece a tu unidad de venta.");
        }
        return plato;
    }

    @Override
    @Transactional
    public Plato crearPlato(Plato plato, Long unidadVentaId) {
        UnidadVenta unidad = unidadVentaRepository.findById(unidadVentaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la unidad de venta con ID: " + unidadVentaId));

        validarPrecioVsCosto(plato.getPrecio(), plato.getCostoProduccion());

        plato.setId(null);
        plato.setUnidadVenta(unidad);
        plato.setActivo(true);

        return platoRepository.save(plato);
    }

    @Override
    @Transactional
    public Plato actualizarPlato(Long platoId, Plato datos, Long unidadVentaId) {
        Plato plato = buscarPorIdYUnidad(platoId, unidadVentaId);

        validarPrecioVsCosto(datos.getPrecio(), datos.getCostoProduccion());

        plato.setNombre(datos.getNombre());
        plato.setPrecio(datos.getPrecio());
        plato.setCostoProduccion(datos.getCostoProduccion());
        plato.setActivo(datos.isActivo());

        return platoRepository.save(plato);
    }

    @Override
    public void validarPrecioVsCosto(BigDecimal precio, BigDecimal costoProduccion) {
        if (precio != null
                && costoProduccion != null
                && precio.compareTo(costoProduccion) <= 0) {
            throw new IllegalArgumentException(
                    "El precio de venta debe ser mayor que el costo de producción.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadVenta obtenerUnidadDelResponsable(String email) {
        Staff responsable = staffRepository
                .findByUsuario_Email(email)
                .orElse(null);
        if (responsable == null) {
            return null;
        }
        return unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);
    }
}
