package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ICanonService;
import com.example.hito2grupo3.services.IUnidadVentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio integral para operaciones y reglas de negocio sobre Unidades de Venta
 * (FoodTrucks y Puestos Desarmables).
 */
@Service
public class UnidadVentaServiceImpl implements IUnidadVentaService {

    // Regex exclusiva para autos de Argentina (AAA123 y AA123BB)
    private static final String PATENTE_ARGENTINA_REGEX = "^([A-Z]{2}\\s?\\d{3}\\s?[A-Z]{2}|[A-Z]{3}\\s?\\d{3})$";
    private static final Pattern PATTERN_PATENTE_ARG = Pattern.compile(PATENTE_ARGENTINA_REGEX);

    private final ICanonService canonService;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;

    public UnidadVentaServiceImpl(
            ICanonService canonService,
            UnidadVentaRepository unidadVentaRepository,
            StaffRepository staffRepository) {
        this.canonService = canonService;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
    }

    /**
     * Valida si una cadena cumple con los formatos de patentes de autos de la República Argentina (AAA123 o AA123BB).
     */
    public boolean validarPatenteArgentina(String patente) {
        if (patente == null || patente.isBlank()) {
            return false;
        }
        return PATTERN_PATENTE_ARG.matcher(patente.trim().toUpperCase()).matches();
    }

    @Override
    public BigDecimal calcularCanon(UnidadVenta unidad) {
        return canonService.calcularCanon(unidad);
    }

    @Override
    public BigDecimal calcularCanonFoodTruck(FoodTruck foodTruck) {
        return canonService.calcularCanonFoodTruck(foodTruck);
    }

    @Override
    public BigDecimal calcularCanonPuestoDesarmable(PuestoDesarmable puestoDesarmable) {
        return canonService.calcularCanonPuestoDesarmable(puestoDesarmable);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularCanonPorResponsableEmail(String email) {
        return canonService.calcularCanonPorResponsableEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularCanonPorUnidadId(Long unidadId) {
        return canonService.calcularCanonPorUnidadId(unidadId);
    }

    @Override
    public boolean validarCodigo(UnidadVenta unidad) {
        if (unidad == null || unidad.getCodigo() == null) {
            return false;
        }
        if (unidad instanceof FoodTruck) {
            return validarCodigoFoodTruck(unidad.getCodigo());
        }
        if (unidad instanceof PuestoDesarmable) {
            return validarCodigoPuestoDesarmable(unidad.getCodigo());
        }
        return UnidadVenta.esCodigoValido(unidad.getCodigo());
    }

    @Override
    public boolean esCodigoValido(String codigo, String tipoUnidad) {
        if (codigo == null || tipoUnidad == null) {
            return false;
        }
        if ("FOOD_TRUCK".equalsIgnoreCase(tipoUnidad)) {
            return validarCodigoFoodTruck(codigo);
        }
        if ("PUESTO_DESARMABLE".equalsIgnoreCase(tipoUnidad)) {
            return validarCodigoPuestoDesarmable(codigo);
        }
        return UnidadVenta.esCodigoValido(codigo);
    }

    @Override
    public boolean validarCodigoFoodTruck(String codigo) {
        return FoodTruck.esCodigoValidoFoodTruck(codigo);
    }

    @Override
    public boolean validarCodigoPuestoDesarmable(String codigo) {
        return PuestoDesarmable.esCodigoValidoPuesto(codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadVenta buscarPorResponsableEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        Staff responsable = staffRepository.findByUsuario_Email(email.trim()).orElse(null);
        if (responsable == null) {
            return null;
        }
        return unidadVentaRepository.findByResponsable_Id(responsable.getId()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadVenta buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return unidadVentaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadVenta> listarTodas() {
        return unidadVentaRepository.findAll();
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la unidad es obligatorio.");
        }

        int filasActualizadas = unidadVentaRepository.darDeBaja(id);

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "No se encontró una unidad activa con el ID: " + id);
        }
    }
}