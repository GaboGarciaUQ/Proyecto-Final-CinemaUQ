package com.uniquindio.proyectofinalp2.cinemauq.pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;

public class ConfiteriaTest {

    private static final Logger LOG = Logger.getLogger(ConfiteriaTest.class.getName());

    // RN-008: El precio de un producto debe ser mayor a cero

    @Test
    void RN008Cumple() {
        LOG.info("Probando RN-008: Creacion de producto con precio mayor a cero");

        assertDoesNotThrow(() -> new Producto("P-001", "Gaseosa", 5000));
    }

    @Test
    void RN008NoCumple() {
        LOG.info("Probando RN-008: Creacion de producto con precio igual a cero");

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new Producto("P-002", "Gaseosa", 0)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-009: Un combo debe tener almenos un producto

    @Test
    void RN009Cumple() {
        LOG.info("Probando RN-009: Creacion de combo con al menos un producto");

        Producto producto = new Producto("P-003", "Palomitas", 8000);

        assertDoesNotThrow(() -> new Combo(
                "C-001", "Combo sencillo", List.of(producto), 0.15
        ));
    }

    @Test
    void RN009NoCumple() {
        LOG.info("Probando RN-009: Creacion de combo sin componentes");

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new Combo("C-002", "Combo vacio", List.of(), 0.15)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-010: Un combo debe de tener un descuento valido.

    @Test
    void RN010Cumple() {
        LOG.info("Probando RN-010: Creacion de combo con descuento valido");

        Producto producto = new Producto("P-004", "Gaseosa", 5000);

        assertDoesNotThrow(() -> new Combo(
                "C-003", "Combo promocional", List.of(producto), 0.15
        ));
    }

    @Test
    void RN010NoCumple() {
        LOG.info("Probando RN-010: Creacion de combo con descuento igual a uno");

        Producto producto = new Producto("P-005", "Palomitas", 8000);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new Combo("C-004", "Combo gratuito", List.of(producto), 1.0)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }
}
