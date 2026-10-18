package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.services.IPasswordGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class PasswordGeneratorImpl implements IPasswordGenerator {

    private static final int LONGITUD_MINIMA = 12;

    // Se excluyen caracteres ambiguos: '0', 'O', '1', 'I', 'l'
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
    private static final String NUMEROS = "23456789";
    private static final String SIMBOLOS = "!@#$%&*-_+=?";

    private static final String TODOS_CARACTERES = MAYUSCULAS + MINUSCULAS + NUMEROS + SIMBOLOS;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generarPassword() {
        return generarPassword(LONGITUD_MINIMA);
    }

    @Override
    public String generarPassword(int longitud) {
        if (longitud < LONGITUD_MINIMA) {
            throw new IllegalArgumentException("La longitud mínima de la contraseña debe ser " + LONGITUD_MINIMA);
        }

        List<Character> caracteres = new ArrayList<>(longitud);

        // Garantizar al menos un caracter de cada categoria obligatoria
        caracteres.add(MAYUSCULAS.charAt(random.nextInt(MAYUSCULAS.length())));
        caracteres.add(MINUSCULAS.charAt(random.nextInt(MINUSCULAS.length())));
        caracteres.add(NUMEROS.charAt(random.nextInt(NUMEROS.length())));
        caracteres.add(SIMBOLOS.charAt(random.nextInt(SIMBOLOS.length())));

        // Completar la longitud restante con el conjunto total permitido
        for (int i = 4; i < longitud; i++) {
            caracteres.add(TODOS_CARACTERES.charAt(random.nextInt(TODOS_CARACTERES.length())));
        }

        // Mezclar aleatoriamente todos los caracteres para evitar patrones de posicion
        Collections.shuffle(caracteres, random);

        StringBuilder sb = new StringBuilder(longitud);
        for (char c : caracteres) {
            sb.append(c);
        }

        return sb.toString();
    }
}
