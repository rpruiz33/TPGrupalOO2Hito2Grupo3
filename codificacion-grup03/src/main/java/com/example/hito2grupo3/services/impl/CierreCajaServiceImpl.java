package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.entities.Cajero;
import com.example.hito2grupo3.entities.CierreCaja;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.dto.VentaUnidadDiaDTO;
import com.example.hito2grupo3.repositories.CajeroRepository;
import com.example.hito2grupo3.repositories.CierreCajaRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ICierreCajaService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CierreCajaServiceImpl implements ICierreCajaService {

    private final CierreCajaRepository cierreCajaRepository;
    private final CajeroRepository cajeroRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final PedidoRepository pedidoRepository;

    public CierreCajaServiceImpl(CierreCajaRepository cierreCajaRepository,
                                 CajeroRepository cajeroRepository,
                                 UnidadVentaRepository unidadVentaRepository,
                                 PedidoRepository pedidoRepository) {
        this.cierreCajaRepository = cierreCajaRepository;
        this.cajeroRepository = cajeroRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional
    public CierreCaja registrarCierre(Long cajeroId, Long unidadVentaId, LocalDate fecha, BigDecimal montoRecaudado) {
        Cajero cajero = cajeroRepository.findById(cajeroId)
                .orElseThrow(() -> new IllegalArgumentException("Cajero no encontrado con id: " + cajeroId));

        UnidadVenta unidadVenta = unidadVentaRepository.findById(unidadVentaId)
                .orElseThrow(() -> new IllegalArgumentException("Unidad de venta no encontrada con id: " + unidadVentaId));

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        // Si el monto recaudado no se envió o es nulo, se obtiene directamente de lo vendido en la BD
        if (montoRecaudado == null || montoRecaudado.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal totalDB = pedidoRepository.calcularTotalVentasPorUnidadYFecha(unidadVentaId, fecha);
            montoRecaudado = (totalDB != null) ? totalDB : BigDecimal.ZERO;
        }

        if (montoRecaudado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto recaudado no puede ser negativo.");
        }

        // Si ya existe un cierre previo y su estado es CERRADA, se debe reabrir explícitamente antes de volver a cerrar
        Optional<CierreCaja> existente = cierreCajaRepository.findByCajeroIdAndUnidadVentaIdAndFecha(cajeroId, unidadVentaId, fecha);
        if (existente.isPresent() && "CERRADA".equalsIgnoreCase(existente.get().getEstado())) {
            throw new IllegalStateException("La caja para esta unidad en la fecha de hoy ya fue cerrada. Para continuar registrando o actualizar el arqueo, debés reabrir la caja.");
        }

        CierreCaja cierre = existente.orElseGet(CierreCaja::new);
        cierre.setFecha(fecha);
        cierre.setMontoRecaudado(montoRecaudado);
        cierre.setCajero(cajero);
        cierre.setUnidadVenta(unidadVenta);
        cierre.setEstado("CERRADA");
        cierre.setFechaHoraCierre(LocalDateTime.now());

        return cierreCajaRepository.save(cierre);
    }

    @Override
    @Transactional
    public CierreCaja reabrirCaja(Long cajeroId, Long unidadVentaId, LocalDate fecha, String motivo) {
        if (fecha == null) {
            fecha = LocalDate.now();
        }

        CierreCaja cierre = cierreCajaRepository.findByCajeroIdAndUnidadVentaIdAndFecha(cajeroId, unidadVentaId, fecha)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cierre de caja para reabrir en la fecha seleccionada."));

        if (!"CERRADA".equalsIgnoreCase(cierre.getEstado())) {
            throw new IllegalStateException("La caja no se encuentra cerrada actualmente.");
        }

        cierre.setEstado("REABIERTA");
        cierre.setFechaHoraReapertura(LocalDateTime.now());
        if (motivo != null && !motivo.isBlank()) {
            cierre.setMotivoReapertura(motivo.trim());
        }

        return cierreCajaRepository.save(cierre);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CierreCaja> obtenerCierre(Long cajeroId, Long unidadVentaId, LocalDate fecha) {
        return cierreCajaRepository.findByCajeroIdAndUnidadVentaIdAndFecha(cajeroId, unidadVentaId, fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CierreCaja> obtenerHistorialPorCajero(Long cajeroId) {
        return cierreCajaRepository.findByCajeroIdOrderByFechaDesc(cajeroId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CierreCaja> obtenerHistorialPorCajeroYFechas(Long cajeroId, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null) {
            return cierreCajaRepository.findByCajeroIdAndFechaBetween(cajeroId, desde, hasta);
        }
        return cierreCajaRepository.findByCajeroIdOrderByFechaDesc(cajeroId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularRecaudacionTotal(Long cajeroId) {
        return cierreCajaRepository.sumarRecaudacionPorCajero(cajeroId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularRecaudacionEntreFechas(Long cajeroId, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            return calcularRecaudacionTotal(cajeroId);
        }
        return cierreCajaRepository.sumarRecaudacionPorCajeroYFechas(cajeroId, desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenCierre(String emailCajero) {
        Map<String, Object> resumen = new LinkedHashMap<>();

        // Buscar cajero por email de su usuario asociado (sin fallbacks hardcodeados)
        Cajero cajero = cajeroRepository.findAll().stream()
                .filter(c -> c.getUsuario() != null
                        && emailCajero != null
                        && emailCajero.equalsIgnoreCase(c.getUsuario().getEmail()))
                .findFirst()
                .orElse(null);

        if (cajero == null) {
            resumen.put("error", "No se encontró un cajero asociado al usuario \"" + emailCajero
                    + "\". Asegurate de que tu cuenta esté vinculada a un cajero en el sistema.");
            return resumen;
        }

        // Obtener unidad de venta asignada al cajero
        UnidadVenta unidad = unidadVentaRepository.findAll().stream()
                .filter(u -> (u.getResponsable() != null && Objects.equals(u.getResponsable().getId(), cajero.getId()))
                        || (u.getStaffAsignado() != null && u.getStaffAsignado().stream()
                                .anyMatch(s -> Objects.equals(s.getId(), cajero.getId()))))
                .findFirst()
                .orElseGet(() -> unidadVentaRepository.findAll().stream().findFirst().orElse(null));

        List<CierreCaja> historial = cierreCajaRepository.findByCajeroIdOrderByFechaDesc(cajero.getId());
        BigDecimal totalAcumulado = cierreCajaRepository.sumarRecaudacionPorCajero(cajero.getId());

        resumen.put("cajeroId", cajero.getId());
        resumen.put("cajeroNombre", cajero.getNombre() + " " + cajero.getApellido());
        resumen.put("cajeroDni", cajero.getDni());
        resumen.put("cajeroTurno", cajero.getTurno() != null ? cajero.getTurno() : "N/D");
        resumen.put("cajeroNumeroCaja", cajero.getNumeroCaja() != null ? cajero.getNumeroCaja() : "N/D");
        resumen.put("unidadVentaId", unidad != null ? unidad.getId() : null);
        resumen.put("unidadVentaNombre", unidad != null ? unidad.getNombreComercial() : "No asignada");
        resumen.put("historial", historial);
        resumen.put("totalAcumulado", totalAcumulado != null ? totalAcumulado : BigDecimal.ZERO);
        resumen.put("fechaHoy", LocalDate.now());

        // Verificar estado de la caja de hoy para la unidad asignada
        Optional<CierreCaja> cierreHoy = (unidad != null)
                ? cierreCajaRepository.findByCajeroIdAndUnidadVentaIdAndFecha(cajero.getId(), unidad.getId(), LocalDate.now())
                : Optional.empty();

        boolean cajaCerradaHoy = cierreHoy.isPresent() && "CERRADA".equalsIgnoreCase(cierreHoy.get().getEstado());
        boolean cajaReabiertaHoy = cierreHoy.isPresent() && "REABIERTA".equalsIgnoreCase(cierreHoy.get().getEstado());

        resumen.put("cierreHoy", cierreHoy.orElse(null));
        resumen.put("cajaCerradaHoy", cajaCerradaHoy);
        resumen.put("cajaReabiertaHoy", cajaReabiertaHoy);

        // Ventas de hoy para la unidad asignada al cajero
        if (unidad != null) {
            LocalDate hoy = LocalDate.now();
            try {
                List<VentaUnidadDiaDTO> ventasHoy =
                        pedidoRepository.obtenerDetalleVentasPorUnidadYFecha(unidad.getId(), hoy);
                BigDecimal totalVentasHoy =
                        pedidoRepository.calcularTotalVentasPorUnidadYFecha(unidad.getId(), hoy);
                resumen.put("ventasHoy", ventasHoy != null ? ventasHoy : Collections.emptyList());
                resumen.put("totalVentasHoy", totalVentasHoy != null ? totalVentasHoy : BigDecimal.ZERO);
            } catch (Exception e) {
                resumen.put("ventasHoy", Collections.emptyList());
                resumen.put("totalVentasHoy", BigDecimal.ZERO);
            }
        } else {
            resumen.put("ventasHoy", Collections.emptyList());
            resumen.put("totalVentasHoy", BigDecimal.ZERO);
        }

        return resumen;
    }
}
