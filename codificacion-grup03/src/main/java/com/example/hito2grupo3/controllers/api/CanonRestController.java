
package com.example.hito2grupo3.controllers.api;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hito2grupo3.services.ICanonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/canon")
@Tag(name = "Canon", description = "Consultas y cálculo del canon de unidades de venta")
public class CanonRestController {

    private final ICanonService canonService;

    public CanonRestController(ICanonService canonService) {
        this.canonService = canonService;
    }

    @GetMapping("/unidad/{unidadId}")
    @Operation(
        summary = "Calcular canon de una unidad de venta",
        description = "Calcula el canon correspondiente a una unidad de venta según su tipo, superficie y características."
    )
    public ResponseEntity<BigDecimal> calcularCanon(
            @Parameter(description = "ID de la unidad de venta", example = "1")
            @PathVariable Long unidadId) {

        BigDecimal canon = canonService.calcularCanonPorUnidadId(unidadId);

        return ResponseEntity.ok(canon);
    }
}
