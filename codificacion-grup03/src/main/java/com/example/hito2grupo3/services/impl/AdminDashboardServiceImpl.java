package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IAdminDashboardService;
import com.example.hito2grupo3.services.IRentabilidadNetaService;
import com.example.hito2grupo3.services.IReporteVentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final PedidoRepository pedidoRepository;
    private final IReporteVentaService reporteVentaService;
    private final IRentabilidadNetaService rentabilidadNetaService;

    public AdminDashboardServiceImpl(
            FestivalRepository festivalRepository,
            UsuarioRepository usuarioRepository,
            UnidadVentaRepository unidadVentaRepository,
            StaffRepository staffRepository,
            PedidoRepository pedidoRepository,
            IReporteVentaService reporteVentaService,
            IRentabilidadNetaService rentabilidadNetaService) {
        this.festivalRepository = festivalRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
        this.pedidoRepository = pedidoRepository;
        this.reporteVentaService = reporteVentaService;
        this.rentabilidadNetaService = rentabilidadNetaService;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerKpisDashboard() {
        return obtenerKpisDashboard(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerKpisDashboard(Long festivalIdParam) {
        Map<String, Object> kpis = new LinkedHashMap<>();

        List<Festival> festivales = festivalRepository.findAll();
        Festival festivalActual;

        if (festivalIdParam != null) {
            festivalActual = festivalRepository.findById(festivalIdParam)
                    .orElseGet(this::resolverFestivalActual);
        } else {
            festivalActual = resolverFestivalActual();
        }

        Long festivalId = festivalActual != null ? festivalActual.getId() : null;

        BigDecimal recaudacion = BigDecimal.ZERO;
        BigDecimal rentabilidadNeta = BigDecimal.ZERO;

        if (festivalId != null) {
            // Recaudación según el festival seleccionado
            try {
                ReporteVentaDTO reporteVenta = reporteVentaService.obtenerTotalesFestival(festivalId);
                if (reporteVenta != null && reporteVenta.getRecaudacionTotal() != null) {
                    recaudacion = reporteVenta.getRecaudacionTotal();
                }
            } catch (Exception ignored) {
                // Si el festival no tiene pedidos, la recaudación es 0
            }

            // Rentabilidad neta según el festival seleccionado (descontando platos, sueldos y canon)
            try {
                Map<String, Object> repRentabilidad = rentabilidadNetaService.obtenerReporteRentabilidad(
                        festivalId,
                        null,
                        festivalActual.getFechaInicio(),
                        festivalActual.getFechaFin());
                if (repRentabilidad != null && repRentabilidad.get("rentabilidadNeta") instanceof BigDecimal rn) {
                    rentabilidadNeta = rn;
                }
            } catch (Exception ignored) {
            }
        } else {
            recaudacion = calcularRecaudacionMensual();
        }

        kpis.put("festivalesCount", festivalRepository.count());
        kpis.put("usuariosCount", usuarioRepository.count());
        kpis.put("recaudacionMensual", recaudacion);
        kpis.put("rentabilidadNetaTotal", rentabilidadNeta);
        kpis.put("festivalActualNombre", festivalActual != null ? festivalActual.getNombre() : null);
        kpis.put("festivalId", festivalId);
        kpis.put("festivales", festivales);
        kpis.put("unidadesCount", unidadVentaRepository.countByActivoTrue());
        kpis.put("staffCount", staffRepository.count());
        kpis.put("primerFestivalId", festivalId != null ? festivalId : resolverPrimerFestivalId());

        return kpis;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularRecaudacionFestivalActual() {
        Festival festivalActual = resolverFestivalActual();
        if (festivalActual != null && festivalActual.getId() != null) {
            try {
                ReporteVentaDTO reporteVenta = reporteVentaService.obtenerTotalesFestival(festivalActual.getId());
                if (reporteVenta != null && reporteVenta.getRecaudacionTotal() != null) {
                    return reporteVenta.getRecaudacionTotal();
                }
            } catch (Exception ignored) {
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularRecaudacionMensual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        BigDecimal recaudacionMes = pedidoRepository.calcularRecaudacionEntreFechas(inicioMes, finMes);
        BigDecimal recaudacionTotal = pedidoRepository.calcularRecaudacionTotal();

        return (recaudacionMes != null && recaudacionMes.compareTo(BigDecimal.ZERO) > 0)
                ? recaudacionMes
                : (recaudacionTotal != null ? recaudacionTotal : BigDecimal.ZERO);
    }

    // ─── Métodos auxiliares privados ────────────────────────────────────────

    private Festival resolverFestivalActual() {
        List<Festival> festivales = festivalRepository.findAll();
        if (festivales == null || festivales.isEmpty()) {
            return null;
        }

        LocalDate hoy = LocalDate.now();
        // Mismo criterio que RentabilidadNetaController y ReporteController:
        // 1. Festival actualmente en curso (hoy entre fechaInicio y fechaFin)
        // 2. Si no hay en curso, el festival con fecha de inicio más reciente
        return festivales.stream()
                .filter(f -> f.getFechaInicio() != null && f.getFechaFin() != null
                        && !hoy.isBefore(f.getFechaInicio()) && !hoy.isAfter(f.getFechaFin()))
                .findFirst()
                .orElseGet(() -> festivales.stream()
                        .filter(f -> f.getFechaInicio() != null)
                        .max(Comparator.comparing(Festival::getFechaInicio))
                        .orElse(festivales.get(0)));
    }

    private Long resolverPrimerFestivalId() {
        return festivalRepository.findFirstByOrderByIdAsc()
                .map(Festival::getId)
                .orElse(1L);
    }
}
