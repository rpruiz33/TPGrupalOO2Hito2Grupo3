package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IAdminDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final PedidoRepository pedidoRepository;

    public AdminDashboardServiceImpl(
            FestivalRepository festivalRepository,
            UsuarioRepository usuarioRepository,
            UnidadVentaRepository unidadVentaRepository,
            StaffRepository staffRepository,
            PedidoRepository pedidoRepository) {
        this.festivalRepository = festivalRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerKpisDashboard() {
        Map<String, Object> kpis = new LinkedHashMap<>();

        kpis.put("festivalesCount", festivalRepository.count());
        kpis.put("usuariosCount", usuarioRepository.count());
        kpis.put("recaudacionMensual", calcularRecaudacionMensual());
        kpis.put("unidadesCount", unidadVentaRepository.count());
        kpis.put("staffCount", staffRepository.count());
        kpis.put("primerFestivalId", resolverPrimerFestivalId());

        return kpis;
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

    // ─── Método auxiliar privado ────────────────────────────────────────────

    private Long resolverPrimerFestivalId() {
        return festivalRepository.findFirstByOrderByIdAsc()
                .map(Festival::getId)
                .orElse(1L);
    }
}
