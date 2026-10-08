package com.example.hito2grupo3.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "festivales")
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<UnidadVenta> unidadesVenta = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<UnidadVenta> getUnidadesVenta() {
        return unidadesVenta;
    }

    public void setUnidadesVenta(List<UnidadVenta> unidadesVenta) {
        this.unidadesVenta = unidadesVenta;
    }
}
