package com.example.hito2grupo3.services.api.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.api.CierreCajaRequestDTO;
import com.example.hito2grupo3.dto.api.CierreCajaResponseDTO;
import com.example.hito2grupo3.dto.api.ReaperturaCajaRequestDTO;
import com.example.hito2grupo3.dto.api.ResumenCajaResponseDTO;
import com.example.hito2grupo3.entities.Cajero;
import com.example.hito2grupo3.entities.CierreCaja;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.repositories.CajeroRepository;
import com.example.hito2grupo3.repositories.CierreCajaRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ICierreCajaService;
import com.example.hito2grupo3.services.api.ICierreCajaApiService;

/**
 * Implementación del servicio REST para cierres y reaperturas de caja.
 */
@Service
public class CierreCajaApiServiceImpl implements ICierreCajaApiService {

    private final ICierreCajaService cierreCajaService;
    private final CierreCajaRepository cierreCajaRepository;
    private final CajeroRepository cajeroRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;

    public CierreCajaApiServiceImpl(
            ICierreCajaService cierreCajaService,
            CierreCajaRepository cierreCajaRepository,
            CajeroRepository cajeroRepository,
            UnidadVentaRepository unidadVentaRepository,
            StaffRepository staffRepository) {
        this.cierreCajaService = cierreCajaService;
        this.cierreCajaRepository = cierreCajaRepository;
        this.cajeroRepository = cajeroRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    @Transactional
    public CierreCajaResponseDTO registrarCierre(CierreCajaRequestDTO request) {
        if (!cajeroRepository.existsById(request.getCajeroId())) {
            throw new NoSuchElementException("No existe el cajero con id " + request.getCajeroId() + ".");
        }
        if (!unidadVentaRepository.existsById(request.getUnidadVentaId())) {
            throw new NoSuchElementException(
                    "No existe la unidad de venta con id " + request.getUnidadVentaId() + ".");
        }

        CierreCaja cierre = cierreCajaService.validarYRegistrarCierre(
                request.getCajeroId(),
                request.getUnidadVentaId(),
                request.getFecha(),
                request.getMontoRecaudado(),
                request.isEsAdmin());
        return CierreCajaResponseDTO.desde(cierre);
    }

    @Override
    @Transactional
    public CierreCajaResponseDTO reabrirCaja(ReaperturaCajaRequestDTO request) {
        if (!cajeroRepository.existsById(request.getCajeroId())) {
            throw new NoSuchElementException("No existe el cajero con id " + request.getCajeroId() + ".");
        }
        if (!unidadVentaRepository.existsById(request.getUnidadVentaId())) {
            throw new NoSuchElementException(
                    "No existe la unidad de venta con id " + request.getUnidadVentaId() + ".");
        }

        CierreCaja reabierta = cierreCajaService.reabrirCaja(
                request.getCajeroId(),
                request.getUnidadVentaId(),
                request.getFecha(),
                request.getMotivo().trim());
        return CierreCajaResponseDTO.desde(reabierta);
    }

    @Override
    @Transactional(readOnly = true)
    public CierreCajaResponseDTO obtenerPorId(Long id) {
        CierreCaja cierre = cierreCajaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el cierre de caja con id " + id + "."));
        return CierreCajaResponseDTO.desde(cierre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CierreCajaResponseDTO> buscarHistorialFiltrado(
            Long cajeroId,
            Long unidadVentaId,
            String estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            BigDecimal montoMinimo) {

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        if (cajeroId != null && !cajeroRepository.existsById(cajeroId)) {
            throw new NoSuchElementException("No existe el cajero con id " + cajeroId + ".");
        }
        if (unidadVentaId != null && !unidadVentaRepository.existsById(unidadVentaId)) {
            throw new NoSuchElementException("No existe la unidad de venta con id " + unidadVentaId + ".");
        }

        String estadoNorm = (estado == null || estado.isBlank()) ? null : estado.trim().toUpperCase(Locale.ROOT);
        if (estadoNorm != null && !estadoNorm.equals("CERRADA") && !estadoNorm.equals("REABIERTA")) {
            throw new IllegalArgumentException("El estado debe ser CERRADA o REABIERTA.");
        }

        return cierreCajaRepository.findAll().stream()
                .filter(c -> cajeroId == null
                        || (c.getCajero() != null && cajeroId.equals(c.getCajero().getId())))
                .filter(c -> unidadVentaId == null
                        || (c.getUnidadVenta() != null && unidadVentaId.equals(c.getUnidadVenta().getId())))
                .filter(c -> estadoNorm == null
                        || (c.getEstado() != null && c.getEstado().toUpperCase(Locale.ROOT).equals(estadoNorm)))
                .filter(c -> fechaDesde == null
                        || (c.getFecha() != null && !c.getFecha().isBefore(fechaDesde)))
                .filter(c -> fechaHasta == null
                        || (c.getFecha() != null && !c.getFecha().isAfter(fechaHasta)))
                .filter(c -> montoMinimo == null
                        || (c.getMontoRecaudado() != null && c.getMontoRecaudado().compareTo(montoMinimo) >= 0))
                .sorted(Comparator.comparing(CierreCaja::getFecha, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(CierreCajaResponseDTO::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCajaResponseDTO obtenerResumenPorCajero(String emailCajero, LocalDate fecha) {
        Staff staff = staffRepository.findByUsuario_Email(emailCajero)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se encontró un cajero asociado al email: " + emailCajero));

        if (!(staff instanceof Cajero cajero)) {
            throw new IllegalArgumentException("El usuario '" + emailCajero + "' no tiene perfil de Cajero.");
        }

        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        BigDecimal total = cierreCajaService.calcularRecaudacionTotal(cajero.getId());
        List<CierreCaja> historial = cierreCajaService.obtenerHistorialPorCajero(cajero.getId());

        return new ResumenCajaResponseDTO(
                emailCajero,
                cajero.getNombre() + " " + cajero.getApellido(),
                fechaConsulta,
                total != null ? total : BigDecimal.ZERO,
                historial != null ? historial.size() : 0);
    }
}

