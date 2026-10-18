package com.example.hito2grupo3.entities;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Data
@NoArgsConstructor
@Entity 
@Table(name = "LiquidacionHaberes")
public class LiquidacionHaberes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private BigDecimal montoCalculado;
    private LocalDate fechaLiquidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Usuario staff;

    public LiquidacionHaberes(long id, BigDecimal montoCalculado, LocalDate fechaLiquidacion,
                               Festival festival, Usuario staff) {
        this.id = id;
        this.montoCalculado = montoCalculado;
        this.fechaLiquidacion = fechaLiquidacion;
        this.festival = festival;
        this.staff = staff;
    }
}