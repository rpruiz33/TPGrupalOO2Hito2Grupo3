package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.configuration.SueldoProperties;
import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.ICategoriaCocinaService;
import com.example.hito2grupo3.services.IEmpleadoService;
import com.example.hito2grupo3.services.IFestivalService;
import com.example.hito2grupo3.services.IUnidadVentaService;
import com.example.hito2grupo3.services.IUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioControllerTest {

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private IEmpleadoService empleadoService;

    @Mock
    private ICategoriaCocinaService categoriaCocinaService;

    @Mock
    private IUnidadVentaService unidadVentaService;

    @Mock
    private IFestivalService festivalService;

    @Mock
    private SueldoProperties sueldoProperties;

    @Mock
    private Authentication authentication;

    private AdminUsuarioController controller;

    private Festival festival;
    private UnidadVenta unidad;

    @BeforeEach
    void setUp() {
        controller = new AdminUsuarioController(
                usuarioService,
                empleadoService,
                categoriaCocinaService,
                unidadVentaService,
                festivalService,
                sueldoProperties
        );

        festival = new Festival();
        festival.setId(1L);
        festival.setNombre("Epicentro Gourmet 2026");
        festival.setFechaInicio(LocalDate.of(2026, 11, 1));
        festival.setFechaFin(LocalDate.of(2026, 11, 30));

        unidad = new UnidadVenta() {};
        unidad.setId(10L);
        unidad.setNombreComercial("Food Truck 1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("responsable@epicentro.com");
        when(unidadVentaService.buscarPorResponsableEmail("responsable@epicentro.com")).thenReturn(unidad);
        when(festivalService.buscarFestivalesActivosPorUnidadVenta(10L)).thenReturn(List.of(festival));
    }

    @Test
    void guardarEmpleado_dniInvalido_rechazaYVuelveAFormulario() {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setDni("1234"); // Menos de 7 números
        dto.setEmail("juan@epicentro.com");
        dto.setFechaNacimiento(LocalDate.of(1995, 5, 20));
        dto.setFechaIngreso(LocalDate.of(2026, 11, 5));
        dto.setSueldo(new BigDecimal("150000"));

        BindingResult result = new BeanPropertyBindingResult(dto, "empleado");
        Model model = new ConcurrentModel();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String vista = controller.guardarEmpleado(authentication, dto, result, model, redirect);

        assertEquals("responsable/usuario-form", vista);
        assertTrue(result.hasFieldErrors("dni"));
        verify(empleadoService, never()).crearEmpleado(any());
    }

    @Test
    void guardarEmpleado_menorDe18Anios_rechazaYVuelveAFormulario() {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setDni("40123456");
        dto.setEmail("juan@epicentro.com");
        // Menor de 18 años
        dto.setFechaNacimiento(LocalDate.now().minusYears(17));
        dto.setFechaIngreso(LocalDate.of(2026, 11, 5));
        dto.setSueldo(new BigDecimal("150000"));

        BindingResult result = new BeanPropertyBindingResult(dto, "empleado");
        Model model = new ConcurrentModel();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String vista = controller.guardarEmpleado(authentication, dto, result, model, redirect);

        assertEquals("responsable/usuario-form", vista);
        assertTrue(result.hasFieldErrors("fechaNacimiento"));
        verify(empleadoService, never()).crearEmpleado(any());
    }

    @Test
    void guardarEmpleado_fechaIngresoAnteriorAlFestival_rechazaYVuelveAFormulario() {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setDni("40123456");
        dto.setEmail("juan@epicentro.com");
        dto.setFechaNacimiento(LocalDate.of(1995, 5, 20));
        // Festival inicia el 2026-11-01, fecha de ingreso anterior:
        dto.setFechaIngreso(LocalDate.of(2026, 10, 25));
        dto.setSueldo(new BigDecimal("150000"));

        BindingResult result = new BeanPropertyBindingResult(dto, "empleado");
        Model model = new ConcurrentModel();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String vista = controller.guardarEmpleado(authentication, dto, result, model, redirect);

        assertEquals("responsable/usuario-form", vista);
        assertTrue(result.hasFieldErrors("fechaIngreso"));
        assertEquals("fechaIngreso.anteriorAlFestival", result.getFieldError("fechaIngreso").getCode());
        verify(empleadoService, never()).crearEmpleado(any());
    }

    @Test
    void guardarEmpleado_datosValidos_creaEmpleadoExitosamente() {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setDni("40123456");
        dto.setEmail("juan@epicentro.com");
        dto.setFechaNacimiento(LocalDate.of(1995, 5, 20));
        dto.setFechaIngreso(LocalDate.of(2026, 11, 5));
        dto.setSueldo(new BigDecimal("150000"));

        Staff staffMock = new Staff();
        staffMock.setId(100L);
        when(empleadoService.crearEmpleado(dto)).thenReturn(staffMock);

        BindingResult result = new BeanPropertyBindingResult(dto, "empleado");
        Model model = new ConcurrentModel();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String vista = controller.guardarEmpleado(authentication, dto, result, model, redirect);

        assertEquals("redirect:/responsable/usuarios", vista);
        assertFalse(result.hasErrors());
        verify(empleadoService).crearEmpleado(dto);
        verify(empleadoService).asociarEmpleadoAUnidadDelCreador("responsable@epicentro.com", staffMock);
    }
}

