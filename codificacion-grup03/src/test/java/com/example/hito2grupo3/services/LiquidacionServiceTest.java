package com.example.hito2grupo3.services;

import com.example.hito2grupo3.configuration.SueldoProperties;
import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.impl.LiquidacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquidacionServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private UnidadVentaRepository unidadVentaRepository;

    @Mock
    private StaffRepository staffRepository;

    private SueldoProperties sueldoProperties;
    private LiquidacionServiceImpl liquidacionService;

    private Festival festival;

    @BeforeEach
    void setUp() {
        sueldoProperties = new SueldoProperties(
                new BigDecimal("100000"),
                new BigDecimal("5000"),
                new BigDecimal("150000"),
                new BigDecimal("100000"),
                new BigDecimal("50000"),
                15,
                new BigDecimal("0.50")
        );

        liquidacionService = new LiquidacionServiceImpl(
                festivalRepository,
                unidadVentaRepository,
                staffRepository,
                sueldoProperties
        );

        festival = new Festival();
        festival.setId(1L);
        festival.setNombre("Epicentro Gourmet 2026");
        // Festival dura 20 días -> factor 1.0 (2 períodos de 15 días: ceil(20/15) = 2 -> 2 * 0.50 = 1.00)
        festival.setFechaInicio(LocalDate.of(2026, 10, 1));
        festival.setFechaFin(LocalDate.of(2026, 10, 20));
    }

    @Test
    void liquidarEmpleadoGenerico_exitosoConAntiguedad() {
        // Staff genérico (instancia base de Staff, no Cocinero ni Cajero)
        Staff staffGenerico = new Staff();
        staffGenerico.setId(10L);
        staffGenerico.setNombre("Javier");
        staffGenerico.setApellido("Martinez");
        staffGenerico.setDni("33412876");
        staffGenerico.setFechaNacimiento(LocalDate.of(1988, 10, 9));
        staffGenerico.setFechaIngreso(LocalDate.of(2024, 8, 1)); // 2 años al 2026-10-01
        staffGenerico.setSueldo(new BigDecimal("150000"));

        Usuario user = new Usuario();
        user.setEmail("responsable@epicentro.com");
        Rol rol = new Rol();
        rol.setNombre("ROLE_RESPONSABLE");
        user.setRol(rol);
        staffGenerico.setUsuario(user);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(staffRepository.findByUsuario_Email("responsable@epicentro.com")).thenReturn(Optional.of(staffGenerico));

        UnidadVenta uv = new PuestoDesarmable();
        uv.setId(100L);
        uv.setNombreComercial("Sabores Criollos");
        uv.setResponsable(staffGenerico);
        when(unidadVentaRepository.findAll()).thenReturn(List.of(uv));

        ILiquidacionService.ResultadoLiquidacion resultado = liquidacionService.liquidarEmpleado("responsable@epicentro.com", 1L);

        assertNotNull(resultado);
        assertEquals(20L, resultado.diasFestival());
        assertEquals("Sabores Criollos", resultado.unidad());
        assertEquals(1, resultado.liquidaciones().size());

        LiquidacionDTO dto = resultado.liquidaciones().iterator().next();
        assertEquals("Javier Martinez", dto.nombreCompleto());
        assertEquals("Responsable", dto.rol());
        assertEquals(2L, dto.aniosAntiguedad());
        // Sueldo = 150000 * 1.0 = 150000
        assertEquals(0, new BigDecimal("150000").compareTo(dto.sueldoBase()));
        // Antigüedad = 2 años * 5000 = 10000 * 1.0 = 10000
        assertEquals(0, new BigDecimal("10000").compareTo(dto.montoAntiguedad()));
        // Plus categoría = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.plusCategoria()));
        // Total = 150000 + 10000 = 160000
        assertEquals(0, new BigDecimal("160000").compareTo(dto.total()));
    }

    @Test
    void liquidarEmpleado_rechazaBusquedaParcial() {
        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(staffRepository.findByUsuario_Email("javier")).thenReturn(Optional.empty());

        // Debe fallar directamente al no encontrar coincidencia exacta por email
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                liquidacionService.liquidarEmpleado("javier", 1L)
        );

        assertTrue(ex.getMessage().contains("No se encontró un empleado asociado al usuario"));
    }

    @Test
    void liquidarCocinero_aplicaPlusYNoAntiguedad() {
        Cocinero cocinero = new Cocinero();
        cocinero.setId(20L);
        cocinero.setNombre("Mateo");
        cocinero.setApellido("Sanchez");
        cocinero.setDni("44987652");
        cocinero.setFechaIngreso(LocalDate.of(2023, 1, 1)); // 3 años de antigüedad
        cocinero.setSueldo(new BigDecimal("100000"));

        CategoriaCocina cat = new CategoriaCocina();
        cat.setNombre("Cocinero");
        cat.setPlusFijo(new BigDecimal("150000"));
        cocinero.setCategoriaCocina(cat);

        Usuario user = new Usuario();
        user.setEmail("cocinero@epicentro.com");
        Rol rol = new Rol();
        rol.setNombre("ROLE_COCINERO");
        user.setRol(rol);
        cocinero.setUsuario(user);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(staffRepository.findByUsuario_Email("cocinero@epicentro.com")).thenReturn(Optional.of(cocinero));
        when(unidadVentaRepository.findAll()).thenReturn(Collections.emptyList());

        ILiquidacionService.ResultadoLiquidacion resultado = liquidacionService.liquidarEmpleado("cocinero@epicentro.com", 1L);

        LiquidacionDTO dto = resultado.liquidaciones().iterator().next();
        assertEquals("Cocinero", dto.rol());
        // Cocinero no percibe antigüedad según fórmula del TP: Sueldo Base + Plus Categoria
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.montoAntiguedad()));
        assertEquals(0, new BigDecimal("150000").compareTo(dto.plusCategoria()));
        // Total = 100000 + 150000 = 250000 * 1.0 = 250000
        assertEquals(0, new BigDecimal("250000").compareTo(dto.total()));
    }

    @Test
    void liquidarCajero_aplicaAntiguedadYNoPlus() {
        Cajero cajero = new Cajero();
        cajero.setId(30L);
        cajero.setNombre("Alejandro");
        cajero.setApellido("Ramirez");
        cajero.setDni("43987651");
        cajero.setFechaIngreso(LocalDate.of(2023, 10, 1)); // 3 años de antigüedad
        cajero.setSueldo(new BigDecimal("100000"));

        Usuario user = new Usuario();
        user.setEmail("cajero@epicentro.com");
        Rol rol = new Rol();
        rol.setNombre("ROLE_CAJERO");
        user.setRol(rol);
        cajero.setUsuario(user);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(staffRepository.findByUsuario_Email("cajero@epicentro.com")).thenReturn(Optional.of(cajero));
        when(unidadVentaRepository.findAll()).thenReturn(Collections.emptyList());

        ILiquidacionService.ResultadoLiquidacion resultado = liquidacionService.liquidarEmpleado("cajero@epicentro.com", 1L);

        LiquidacionDTO dto = resultado.liquidaciones().iterator().next();
        assertEquals("Cajero", dto.rol());
        assertEquals(3L, dto.aniosAntiguedad());
        assertEquals(0, new BigDecimal("15000").compareTo(dto.montoAntiguedad()));
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.plusCategoria()));
        // Total = 100000 + 15000 = 115000 * 1.0 = 115000
        assertEquals(0, new BigDecimal("115000").compareTo(dto.total()));
    }

    @Test
    void liquidarPorUnidadEntreFechas_calculaProporcional() {
        Staff staff = new Staff();
        staff.setId(10L);
        staff.setNombre("Javier");
        staff.setApellido("Martinez");
        staff.setDni("33412876");
        staff.setFechaIngreso(LocalDate.of(2026, 1, 1)); // 0 años
        staff.setSueldo(new BigDecimal("100000"));

        UnidadVenta uv = new PuestoDesarmable();
        uv.setId(100L);
        uv.setNombreComercial("Sabores Criollos");
        uv.setResponsable(staff);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(unidadVentaRepository.findById(100L)).thenReturn(Optional.of(uv));

        // Período de 10 días: 2026-10-01 al 2026-10-10 -> 10 días <= 15 -> factor 0.50
        ILiquidacionService.ResultadoLiquidacion res = liquidacionService.liquidarPorUnidadEntreFechas(
                100L, 1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10));

        assertNotNull(res);
        assertEquals(10L, res.diasFestival());
        // Sueldo = 100000 * 0.50 = 50000
        assertEquals(0, new BigDecimal("50000.00").compareTo(res.totalGeneral()));
    }
}

