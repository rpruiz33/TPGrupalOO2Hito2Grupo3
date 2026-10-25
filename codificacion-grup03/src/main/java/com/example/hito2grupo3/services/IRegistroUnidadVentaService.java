package com.example.hito2grupo3.services;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface IRegistroUnidadVentaService {

    void registrar(
            String nombreComercial,
            BigDecimal superficieM2,
            String tipoUnidad,
            String patente,
            Boolean requiereElectricidad,
            Integer cantidadCarpas,
            Integer tiempoMontajeMin,
            String nombre,
            String apellido,
            String dni,
            LocalDate fechaNacimiento,
            LocalDate fechaIngreso,
            String email,
            String password
    );
}