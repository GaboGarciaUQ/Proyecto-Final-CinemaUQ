package com.uniquindio.proyectofinalp2.cinemauq.pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AccesoDenegadoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.UsuarioDuplicadoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;

public class GestionUsuariosTest {

    private static final Logger LOG = Logger.getLogger(GestionUsuariosTest.class.getName());
    private CineServicio cineservicio = new CineServicio();

    // RN-001: Registrar un Cliente requiere un correo nuevo y que no se haya usado antes en el sistema

    @Test
    void RN001Cumple() {
        LOG.info("Probando RN-001: Creacion de cliente con correo unico");

        assertDoesNotThrow(() ->
                cineservicio.registrarCliente("Jose Carlos", "josecarlos@correo.com")
        );
    }

    @Test
    void RN001NoCumple() {
        LOG.info("Probando RN-001: Creacion de cliente con correo duplicado");

        cineservicio.registrarCliente("Jose Carlos", "josecarlos@correo.com");

        UsuarioDuplicadoException excepcion = assertThrows(
                UsuarioDuplicadoException.class,
                () -> cineservicio.registrarCliente("Jose Carlos", "josecarlos@correo.com")
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-002: Las funcionalidad administrativas deben ser realizadas por un Usuario Administrador

    @Test
    void RN002Cumple() {
        LOG.info("Probando RN-002: Operacion administrativa con administrador");

        assertDoesNotThrow(() ->
                cineservicio.getAdministracion().crearPelicula(
                        cineservicio.getAdministrador(),
                        "Pelicula de prueba",
                        120
                )
        );
    }

    @Test
    void RN002NoCumple() {
        LOG.info("Probando RN-002: Operacion administrativa con cliente");

        Cliente cliente = (Cliente) cineservicio
                .autenticar("ana@correo.com")
                .orElseThrow();

        AccesoDenegadoException excepcion = assertThrows(
                AccesoDenegadoException.class,
                () -> cineservicio.getAdministracion().crearPelicula(
                        cliente,
                        "Pelicula no autorizada",
                        120
                )
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }
}

