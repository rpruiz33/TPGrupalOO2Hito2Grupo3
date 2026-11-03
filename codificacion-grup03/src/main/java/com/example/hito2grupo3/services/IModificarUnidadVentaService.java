package com.example.hito2grupo3.services;

import java.math.BigDecimal;
public interface IModificarUnidadVentaService {
    
    void modificar(
            Long id,
            String nombreComercial,
            BigDecimal superficieM2,
            String patente,
            Boolean requiereElectricidad,
            Integer cantidadCarpas,
            Integer tiempoMontajeMin
    );
}
