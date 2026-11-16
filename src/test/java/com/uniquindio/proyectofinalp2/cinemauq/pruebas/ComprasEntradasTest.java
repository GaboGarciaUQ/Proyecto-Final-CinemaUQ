package com.uniquindio.proyectofinalp2.cinemauq.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AccesoDenegadoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CancelacionNoPermitidaException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CompraInvalidaException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Usuario;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaGeneral;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.ReembolsoPorAnticipacion;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;

public class ComprasEntradasTest {

    private static final Logger LOG =
            Logger.getLogger(ComprasEntradasTest.class.getName());

    // RN-011: una compra necesita cliente, función y al menos una entrada.

    @Test
    public void RN011Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = obtenerCliente(cine);
        Funcion funcion = obtenerFuncion(cine);

        Compra compra = crearCompra(cine, cliente, funcion);

        assertEquals(cliente, compra.getCliente());
        assertEquals(1, compra.getEntradas().size());
        LOG.info("RN-011 cumple: la compra tiene cliente, función y entrada.");
    }

    @Test
    public void RN011NoCumple() {
        CineServicio cine = new CineServicio();
        Funcion funcion = obtenerFuncion(cine);

        assertThrows(CompraInvalidaException.class, () ->
                new Compra.Builder(cine.getConfig())
                        .funcion(funcion)
                        .build());

        LOG.info("RN-011 no cumple: no se permite crear una compra sin cliente.");
    }

    // RN-012: el porcentaje de reembolso depende del tiempo restante.

    @Test
    public void RN012ReembolsoTotal() {
        CineServicio cine = new CineServicio();
        Cliente cliente = obtenerCliente(cine);
        Funcion funcion = obtenerFuncion(cine);

        funcion.reprogramar(LocalDateTime.now().plusHours(30));
        Compra compra = crearCompra(cine, cliente, funcion);
        compra.marcarPagada();

        double reembolso = compra.cancelar(
                new ReembolsoPorAnticipacion(cine.getConfig()),
                LocalDateTime.now());

        assertEquals(compra.getTotal(), reembolso, 0.01);
        LOG.info("RN-012 cumple: con más de 24 horas se devuelve el 100%.");
    }

    @Test
    public void RN012ReembolsoParcial() {
        CineServicio cine = new CineServicio();
        Cliente cliente = obtenerCliente(cine);
        Funcion funcion = obtenerFuncion(cine);

        funcion.reprogramar(LocalDateTime.now().plusHours(12));
        Compra compra = crearCompra(cine, cliente, funcion);
        compra.marcarPagada();

        double reembolso = compra.cancelar(
                new ReembolsoPorAnticipacion(cine.getConfig()),
                LocalDateTime.now());

        assertEquals(compra.getTotal() * 0.8, reembolso, 0.01);
        LOG.info("RN-012 cumple: entre 2 y 24 horas se devuelve el 80%.");
    }

    @Test
    public void RN012NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = obtenerCliente(cine);
        Funcion funcion = obtenerFuncion(cine);

        funcion.reprogramar(LocalDateTime.now().plusHours(1));
        Compra compra = crearCompra(cine, cliente, funcion);
        compra.marcarPagada();

        CancelacionNoPermitidaException excepcion = assertThrows(
        CancelacionNoPermitidaException.class,
        () -> compra.cancelar(
                new ReembolsoPorAnticipacion(cine.getConfig()),
                LocalDateTime.now()));

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
        LOG.info("RN-012 no cumple: no se permite cancelar con menos de 2 horas.");
    }

    @Test
    public void RN013Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "prueba13@correo.com");
        Compra compra = crearCompraDePrueba(cine, cliente);
        double total = compra.getTotal();

        int reembolsadas = cine.cancelarFuncion(cine.getAdministrador(), compra.getFuncion());

        assertEquals(1, reembolsadas);
        assertEquals(total, compra.getMontoReembolsado(), 0.01);
        LOG.info("RN-013 cumple: El cine cancela la funcion y las compras son reembolsadas");
    }

    @Test
    public void RN013NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "prueba14@correo.com");
        Compra compra = crearCompraDePrueba(cine, cliente);

        AccesoDenegadoException excepcion = assertThrows(
                AccesoDenegadoException.class,
                () -> cine.cancelarFuncion(cliente, compra.getFuncion())
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-014: el reembolso debe llegar a la tarjeta del comprador.

    @Test
    public void RN014Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "prueba15@correo.com");
        Compra compra = crearCompraDePrueba(cine, cliente);

        double saldoAntes = cine.getTarjeta(cliente).getSaldo();

        cine.cancelarCompra(cliente, compra);

        double saldoDespues = cine.getTarjeta(cliente).getSaldo();

        assertEquals(compra.getTotal(), saldoDespues - saldoAntes, 0.01);
        LOG.info("RN-014 cumple: El cliente obtiene el monto reembolsado que le corresponde");
    }

    @Test
    public void RN014NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente comprador = cine.registrarCliente("Comprador", "prueba16@correo.com");
        Cliente otroCliente = cine.registrarCliente("Otro cliente", "prueba17@correo.com");
        Compra compra = crearCompraDePrueba(cine, comprador);

        AccesoDenegadoException excepcion = assertThrows(
                AccesoDenegadoException.class,
                () -> cine.cancelarCompra(otroCliente, compra)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-015: una compra no puede ser reembolsada dos veces.

    @Test
    public void RN015Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "prueba18@correo.com");
        Compra compra = crearCompraDePrueba(cine, cliente);

        double reembolso = cine.cancelarCompra(cliente, compra);

        assertEquals(compra.getTotal(), reembolso, 0.01);
        LOG.info("RN-015 cumple: La compra es reembolsada correctamente la unica vez permitida");
    }

    @Test
    public void RN015NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "prueba19@correo.com");
        Compra compra = crearCompraDePrueba(cine, cliente);

        cine.cancelarCompra(cliente, compra);

        EstadoInvalidoException excepcion = assertThrows(
                EstadoInvalidoException.class,
                () -> cine.cancelarCompra(cliente, compra)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    private Cliente obtenerCliente(CineServicio cine) {
        Usuario usuario = cine.autenticar("ana@correo.com").orElseThrow();
        return (Cliente) usuario;
    }

    private Funcion obtenerFuncion(CineServicio cine) {
        return cine.getFunciones().stream()
                .filter(f -> !f.estaCancelada())
                .filter(f -> f.getFechaHora().isAfter(LocalDateTime.now()))
                .filter(f -> f.getSala().getAsientos().stream()
                        .anyMatch(f::estaDisponible))
                .findFirst()
                .orElseThrow();
    }

    private Compra crearCompra(CineServicio cine, Cliente cliente, Funcion funcion) {
        Asiento asiento = funcion.getSala().getAsientos().stream()
                .filter(funcion::estaDisponible)
                .findFirst()
                .orElseThrow();

        return new Compra.Builder(cine.getConfig())
                .cliente(cliente)
                .funcion(funcion)
                .entrada(new EntradaGeneral(funcion, asiento))
                .build();
    }

    private Compra crearCompraDePrueba(CineServicio cine, Cliente cliente) {
        cine.recargarTarjeta(cine.getAdministrador(), cliente, 100000);

        Funcion funcion = cine.getFunciones().stream()
                .filter(f -> !f.estaCancelada())
                .filter(f -> f.getSala().getAsientos().stream()
                        .anyMatch(f::estaDisponible))
                .findFirst()
                .orElseThrow();

        funcion.reprogramar(LocalDateTime.now().plusDays(2));

        Asiento asiento = funcion.getSala().getAsientos().stream()
                .filter(funcion::estaDisponible)
                .findFirst()
                .orElseThrow();

        return cine.comprar(cliente, funcion, List.of(asiento), List.of(), List.of());
    }
}

