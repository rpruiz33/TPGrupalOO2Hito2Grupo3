package com.example.hito2grupo3.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Setter
@Getter
@Entity
@Table(name = "unidades_venta")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UnidadVenta {

    public static final int LONGITUD_CODIGO = 10;
    public static final String REGEX_CODIGO_GENERICO = "^[A-Z]{2}-[0-9]{7}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.validation.constraints.NotNull(message = "El código de la unidad de venta es obligatorio")
    @jakarta.validation.constraints.Size(
        min = LONGITUD_CODIGO,
        max = LONGITUD_CODIGO,
        message = "El código debe tener exactamente 10 caracteres"
    )
    @jakarta.validation.constraints.Pattern(
        regexp = REGEX_CODIGO_GENERICO,
        message = "El código debe tener exactamente 10 caracteres en formato XX-NNNNNNN (ej: FT-0000001 o PD-0000001)"
    )
    @Column(name = "codigo", nullable = false, unique = true, length = LONGITUD_CODIGO)
    private String codigo;

    private String nombreComercial;

    private BigDecimal superficieM2;

    public static boolean esCodigoValido(String codigo) {
        if (codigo == null) {
            return false;
        }

        String limpio = codigo.trim();

        return limpio.length() == LONGITUD_CODIGO
                && limpio.matches(REGEX_CODIGO_GENERICO);
    }

    public boolean validarCodigo(String codigoAValidar) {
        return esCodigoValido(codigoAValidar);
    }

    public boolean validarCodigo() {
        return validarCodigo(this.codigo);
    }

    public void setCodigo(String codigo) {
        if (!validarCodigo(codigo)) {
            throw new IllegalArgumentException(
                "Código de unidad de venta inválido: '" + codigo
                + "'. Debe tener exactamente " + LONGITUD_CODIGO
                + " caracteres y cumplir el formato correspondiente (ej: FT-0000001 o PD-0000001)."
            );
        }

        this.codigo = codigo != null ? codigo.trim() : null;
    }

    // Relación OneToMany con Plato
    @OneToMany(mappedBy = "unidadVenta")
    private List<Plato> platosOfrecidos = new ArrayList<>();

    // Staff asignado: Muchos a Muchos
    @ManyToMany
    @JoinTable(
        name = "unidad_venta_staff",
        joinColumns = @JoinColumn(name = "unidad_venta_id"),
        inverseJoinColumns = @JoinColumn(name = "staff_id")
    )
    private Set<Staff> staffAsignado = new HashSet<>();

    // Responsable: Muchos a Uno
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Staff responsable;

    @ManyToMany(mappedBy = "unidadesVentaHabilitadas")
    private Set<Festival> festivales = new HashSet<>();

    @OneToMany(mappedBy = "unidadVenta")
    private List<Pedido> pedidos = new ArrayList<>();

    public abstract BigDecimal calcularCanon();

}