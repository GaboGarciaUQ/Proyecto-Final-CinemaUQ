
package com.uniquindio.proyectofinalp2.cinemauq.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AsientoNoDisponibleException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaAccesible;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;

public class CarteleraFuncionesTest {

    private static final Logger LOG = Logger.getLogger(CarteleraFuncionesTest.class.getName());
    private CineServicio cineservicio = new CineServicio();

    // RN-003: Una funcion debe tener todos sus datos requeridos

    @Test
    void RN003Cumple() {
        LOG.info("Probando RN-003: Creacion de una funcion con sus datos obligatorios");

        Pelicula pelicula = cineservicio.getPeliculas().get(0);
        var sala = cineservicio.getSalas().get(0);

        Funcion funcion = cineservicio.getAdministracion().programarFuncion(
                cineservicio.getAdministrador(),
                pelicula,
                sala,
                LocalDateTime.now().plusMonths(2),
                15000
        );

        assertNotNull(funcion);
        assertEquals(pelicula, funcion.getPelicula());
        assertEquals(sala, funcion.getSala());
    }

    @Test
    void RN003NoCumple() {
        LOG.info("Probando RN-003: Creacion de una funcion sin sala asociada");

        Pelicula pelicula = cineservicio.getPeliculas().get(0);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> cineservicio.getAdministracion().programarFuncion(
                        cineservicio.getAdministrador(),
                        pelicula,
                        null,
                        LocalDateTime.now().plusMonths(2),
                        15000
                )
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-004: Un asiento solo se puede ocupar si este no ha sido ocupado aun o no esta deshabilitado

    @Test
    void RN004Cumple() {
        LOG.info("Probando RN-004: Ocupacion de un asiento disponible");

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(0);
        Asiento asiento = funcion.getSala().getAsientos().get(0);

        funcion.ocupar(asiento);

        assertFalse(funcion.estaDisponible(asiento));
    }

    @Test
    void RN004NoCumple() {
        LOG.info("Probando RN-004: Intento de ocupar un asiento vendido");

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(0);
        Asiento asiento = funcion.getSala().getAsientos().get(0);

        funcion.ocupar(asiento);

        AsientoNoDisponibleException excepcion = assertThrows(
                AsientoNoDisponibleException.class,
                () -> funcion.ocupar(asiento)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-005: Los asientos a ocupar deben pertenecer a la sala a la que corresponden

    @Test
    void RN005Cumple() {
        LOG.info("Probando RN-005: Respeto de la capacidad de una sala");

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(1);

        assertEquals(funcion.getSala().getCapacidad(), funcion.getCapacidad());
    }

    @Test
    void RN005NoCumple() {
        LOG.info("Probando RN-005: Intento de ocupar un asiento ajeno a la sala");

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(1);

        // La función utiliza la sala 3D; este asiento pertenece a la sala 2D.
        Asiento asientoAjeno = cineservicio.getSalas().get(0)
                .getAsiento('G', 1)
                .orElseThrow();

        AsientoNoDisponibleException excepcion = assertThrows(
                AsientoNoDisponibleException.class,
                () -> funcion.ocupar(asientoAjeno)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-006: Los asientos a ocupar deben ser de una funcion disponible y activa.

    @Test
    void RN006Cumple() {
        LOG.info("Probando RN-006: Ocupacion de asiento en una funcion activa");

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(0);
        Asiento asiento = funcion.getSala().getAsientos().get(0);

        funcion.ocupar(asiento);

        assertFalse(funcion.estaCancelada());
        assertFalse(funcion.estaDisponible(asiento));
    }

    @Test
    void RN006NoCumple() {
        LOG.info("Probando RN-006: Intento de ocupar asiento en una funcion cancelada");

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(0);
        Asiento asiento = funcion.getSala().getAsientos().get(0);

        funcion.cancelar();

        AsientoNoDisponibleException excepcion = assertThrows(
                AsientoNoDisponibleException.class,
                () -> funcion.ocupar(asiento)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-007 Los asientos accesibles solo se pueden ocupar con entradas accesibles (y viceversa)

    @Test
    void RN007Cumple() {
        LOG.info("Probando RN-007: Asiento accesible con entrada accesible");

        Cliente cliente = (Cliente) cineservicio
                .autenticar("ana@correo.com")
                .orElseThrow();

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(0);
        Asiento asiento = funcion.getSala().getAsiento('H', 7).orElseThrow();

        Compra compra = cineservicio.simularCompra(
                cliente,
                funcion,
                List.of(asiento),
                List.of(),
                List.of()
        );

        Entrada entrada = compra.getEntradas().get(0);

        assertTrue(asiento.esAccesible());
        assertInstanceOf(EntradaAccesible.class, entrada);
        assertEquals(asiento, entrada.getAsiento());
    }

    @Test
    void RN007NoCumple() {
        LOG.info("Probando RN-007: Intento de crear entrada accesible con asiento general");

        Pelicula pelicula = cineservicio.getPeliculasEnCartelera().get(0);
        Funcion funcion = cineservicio.getFuncionesDe(pelicula).get(0);
        Asiento asiento = funcion.getSala().getAsientos().get(0);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new EntradaAccesible(funcion, asiento)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }
}

