package com.example.hito2grupo3.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void shouldKeepMessageAndBeRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Festival no encontrado");

        assertEquals("Festival no encontrado", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }
}
