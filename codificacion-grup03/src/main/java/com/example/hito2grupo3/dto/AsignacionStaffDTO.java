package com.example.hito2grupo3.dto;

import java.time.LocalDate;

public class AsignacionStaffDTO {

    private String nombreFestival;
    private String nombreUnidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public AsignacionStaffDTO(
            String nombreFestival,
            String nombreUnidad,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        this.nombreFestival = nombreFestival;
        this.nombreUnidad = nombreUnidad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public String getNombreFestival() {
        return nombreFestival;
    }

    public String getNombreUnidad() {
        return nombreUnidad;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}