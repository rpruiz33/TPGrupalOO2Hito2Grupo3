package com.example.hito2grupo3.services;

import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.impl.UnidadVentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnidadVentaServiceTest {

    @Mock
    private ICanonService canonService;

    @Mock
    private UnidadVentaRepository unidadVentaRepository;

    @Mock
    private StaffRepository staffRepository;

    private UnidadVentaServiceImpl unidadVentaService;

    @BeforeEach
    void setUp() {
        unidadVentaService = new UnidadVentaServiceImpl(
                canonService,
                unidadVentaRepository,
                staffRepository
        );
    }

    @Test
    void darDeBajaLogica_exitoso() {
        when(unidadVentaRepository.darDeBaja(10L)).thenReturn(1);

        assertDoesNotThrow(() -> unidadVentaService.darDeBaja(10L));
        verify(unidadVentaRepository).darDeBaja(10L);
    }

    @Test
    void darDeBajaLogica_idNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> unidadVentaService.darDeBaja(null));
        verify(unidadVentaRepository, never()).darDeBaja(any());
    }

    @Test
    void reactivar_exitoso() {
        when(unidadVentaRepository.reactivar(10L)).thenReturn(1);

        assertDoesNotThrow(() -> unidadVentaService.reactivar(10L));
        verify(unidadVentaRepository).reactivar(10L);
    }

    @Test
    void reactivar_idNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> unidadVentaService.reactivar(null));
        verify(unidadVentaRepository, never()).reactivar(any());
    }
}

