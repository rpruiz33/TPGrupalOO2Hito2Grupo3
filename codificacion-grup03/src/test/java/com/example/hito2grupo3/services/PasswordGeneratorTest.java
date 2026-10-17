package com.example.hito2grupo3.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void generarPassword_DebeTenerLongitudMinimaPorDefectoYComposicionValida() {
        String password = passwordGenerator.generarPassword();

        assertNotNull(password);
        assertEquals(12, password.length(), "La longitud por defecto debe ser 12");

        // Al menos una mayúscula
        assertTrue(password.chars().anyMatch(c -> Character.isUpperCase(c)), "Debe contener al menos una mayúscula");

        // Al menos una minúscula
        assertTrue(password.chars().anyMatch(c -> Character.isLowerCase(c)), "Debe contener al menos una minúscula");

        // Al menos un dígito
        assertTrue(password.chars().anyMatch(c -> Character.isDigit(c)), "Debe contener al menos un número");

        // Al menos un símbolo
        assertTrue(password.chars().anyMatch(c -> "!@#$%&*-_+=?".indexOf(c) >= 0), "Debe contener al menos un símbolo");

        // No debe contener caracteres ambiguos: '0', 'O', '1', 'I', 'l'
        Set<Character> caracteresAmbiguos = Set.of('0', 'O', '1', 'I', 'l');
        for (char c : password.toCharArray()) {
            assertFalse(caracteresAmbiguos.contains(c), "No debe contener el caracter ambiguo: " + c);
        }
    }

    @Test
    void generarPassword_ConLongitudPersonalizadaMayor() {
        int longitud = 16;
        String password = passwordGenerator.generarPassword(longitud);

        assertNotNull(password);
        assertEquals(longitud, password.length());
    }

    @Test
    void generarPassword_LanzaExcepcionSiLongitudEsMenorA12() {
        assertThrows(IllegalArgumentException.class, () -> passwordGenerator.generarPassword(11));
    }

    @Test
    void generarPassword_DosLlamadasConsecutivasGeneranPasswordsDistintos() {
        String pass1 = passwordGenerator.generarPassword();
        String pass2 = passwordGenerator.generarPassword();

        assertNotNull(pass1);
        assertNotNull(pass2);
        assertNotEquals(pass1, pass2, "Dos llamadas deben producir contraseñas aleatorias distintas");
    }
}
