package com.uniquindio.proyectofinalp2.cinemauq.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AccesoDenegadoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.SaldoInsuficienteException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.TarjetaInactivaException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.PuntosInsuficientesException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CompraInvalidaException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.TarjetaVirtual;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaGeneral;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.PuntosBase;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.ServicioPuntos;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;

public class PagosTarjetasTest {

    private static final Logger LOG =
            Logger.getLogger(PagosTarjetasTest.class.getName());

    // RN-016: todo cliente registrado debe tener una tarjeta virtual asociada.

    @Test
    public void RN016Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago16@correo.com");

        Tarjeta tarjeta = cine.getTarjeta(cliente);

        assertNotNull(tarjeta);
        assertEquals(cliente, tarjeta.getTitular());

        LOG.info("RN-016 cumple: el cliente tiene una tarjeta virtual asociada.");
    }

    @Test
    public void RN016NoCumple() {
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new TarjetaVirtual("T-INVALIDA", null)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-017: una tarjeta inactiva no puede realizar pagos.

    @Test
    public void RN017Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago17@correo.com");

        cine.recargarTarjeta(cine.getAdministrador(), cliente, 50000);

        Tarjeta tarjeta = cine.getTarjeta(cliente);
        tarjeta.pagar(10000, "Compra de prueba");

        LOG.info("RN-017 cumple: la tarjeta activa permite realizar pagos.");
    }

    @Test
    public void RN017NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago17b@correo.com");

        cine.recargarTarjeta(cine.getAdministrador(), cliente, 50000);
        cine.desactivarTarjeta(cine.getAdministrador(), cliente);

        Tarjeta tarjeta = cine.getTarjeta(cliente);

        TarjetaInactivaException excepcion = assertThrows(
                TarjetaInactivaException.class,
                () -> tarjeta.pagar(10000, "Compra de prueba")
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-018: el pago no puede superar el saldo disponible.

    @Test
    public void RN018Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago18@correo.com");

        cine.recargarTarjeta(cine.getAdministrador(), cliente, 50000);

        Tarjeta tarjeta = cine.getTarjeta(cliente);
        tarjeta.pagar(20000, "Compra de prueba");

        assertEquals(30000, tarjeta.getSaldo(), 0.01);

        LOG.info("RN-018 cumple: el pago se realiza y el saldo se descuenta correctamente.");
    }

    @Test
    public void RN018NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago18b@correo.com");

        cine.recargarTarjeta(cine.getAdministrador(), cliente, 10000);

        Tarjeta tarjeta = cine.getTarjeta(cliente);

        SaldoInsuficienteException excepcion = assertThrows(
                SaldoInsuficienteException.class,
                () -> tarjeta.pagar(20000, "Compra de prueba")
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-019: solamente el administrador puede realizar recargas.

    @Test
    public void RN019Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago19@correo.com");

        cine.recargarTarjeta(cine.getAdministrador(), cliente, 50000);

        assertEquals(50000, cine.getTarjeta(cliente).getSaldo(), 0.01);
        assertEquals(1, cine.getTarjeta(cliente).getMovimientos().size());

        LOG.info("RN-019 cumple: el administrador recarga la tarjeta y registra el movimiento.");
    }

    @Test
    public void RN019NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago19b@correo.com");

        AccesoDenegadoException excepcion = assertThrows(
                AccesoDenegadoException.class,
                () -> cine.recargarTarjeta(cliente, cliente, 50000)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-020: se acumula un punto por cada $1.000 gastados.

    @Test
    public void RN020Cumple() {
        CineServicio cine = new CineServicio();

        int puntos = cine.puntosPorCompra(10000);

        assertEquals(10, puntos);

        LOG.info("RN-020 cumple: una compra de $10.000 genera 10 puntos.");
    }

    @Test
    public void RN020NoCumple() {
        CineServicio cine = new CineServicio();

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> cine.puntosPorCompra(-1000)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-021: solamente se pueden redimir puntos vigentes.

    @Test
    public void RN021Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago21@correo.com");

        ServicioPuntos puntos = new ServicioPuntos(
                cine.getConfig(), new PuntosBase(cine.getConfig()));

        Clock reloj = Clock.fixed(
                Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("UTC"));

        puntos.setReloj(reloj);

        Compra compra = crearCompraPuntos(cine, cliente);
        compra.marcarPagada();
        puntos.confirmarCompra(compra);

        assertEquals(1, puntos.getHistorial(cliente).size());
        puntos.verificarDisponibles(cliente, 1);

        LOG.info("RN-021 cumple: los puntos vigentes pueden ser redimidos.");
    }

    @Test
    public void RN021NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago21b@correo.com");

        ServicioPuntos puntos = new ServicioPuntos(
                cine.getConfig(), new PuntosBase(cine.getConfig()));

        puntos.setReloj(Clock.fixed(
                Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("UTC")));

        Compra compra = crearCompraPuntos(cine, cliente);
        compra.marcarPagada();
        puntos.confirmarCompra(compra);

        puntos.setReloj(Clock.fixed(
                Instant.parse("2028-01-15T12:00:00Z"), ZoneId.of("UTC")));

        PuntosInsuficientesException excepcion = assertThrows(
                PuntosInsuficientesException.class,
                () -> puntos.verificarDisponibles(cliente, 1)
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // RN-022: los puntos se redimen respetando los beneficios permitidos
    // y consumiendo primero los lotes con vencimiento más próximo.

    @Test
    public void RN022Cumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago22@correo.com");

        ServicioPuntos puntos = new ServicioPuntos(
                cine.getConfig(), new PuntosBase(cine.getConfig()));

        puntos.setReloj(Clock.fixed(
                Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("UTC")));

        Compra primera = crearCompraPuntos(cine, cliente);
        primera.marcarPagada();
        puntos.confirmarCompra(primera);

        puntos.setReloj(Clock.fixed(
                Instant.parse("2026-12-15T12:00:00Z"), ZoneId.of("UTC")));

        Compra segunda = crearCompraPuntos(cine, cliente);
        segunda.marcarPagada();
        puntos.confirmarCompra(segunda);

        puntos.redimir(cliente, "CANJE-001", 5);

        assertEquals(
                primera.getId(),
                puntos.getHistorialConsumos(cliente).get(0).getIdLote()
        );

        LOG.info("RN-022 cumple: se consumen primero los puntos con vencimiento más próximo.");
    }

    @Test
    public void RN022NoCumple() {
        CineServicio cine = new CineServicio();
        Cliente cliente = cine.registrarCliente("Cliente prueba", "pago22b@correo.com");
        Funcion funcion = obtenerFuncion(cine);

        Asiento asiento = funcion.getSala().getAsientos().get(0);
        EntradaGeneral entrada = new EntradaGeneral(funcion, asiento);

        Producto producto = new Producto("P-TEST", "Producto de prueba", 5000);
        Producto productoAjeno = new Producto("P-AJENO", "Producto ajeno", 3000);

        Combo combo = new Combo(
                "C-TEST", "Combo de prueba", List.of(producto), 0.10);

        CompraInvalidaException excepcion = assertThrows(
                CompraInvalidaException.class,
                () -> new Compra.Builder(cine.getConfig())
                        .cliente(cliente)
                        .funcion(funcion)
                        .entrada(entrada)
                        .combo(combo)
                        .canjearConfiteria(productoAjeno)
                        .build()
        );

        LOG.info("Excepción capturada: " + excepcion);
        LOG.info("Mensaje de la excepción: " + excepcion.getMessage());
    }

    // Métodos auxiliares.

    private Funcion obtenerFuncion(CineServicio cine) {
        return cine.getFunciones().stream()
                .filter(f -> !f.estaCancelada())
                .findFirst()
                .orElseThrow();
    }

    private Compra crearCompraPuntos(CineServicio cine, Cliente cliente) {
        Funcion funcion = obtenerFuncion(cine);

        Asiento asiento = funcion.getSala().getAsientos().get(0);

        return new Compra.Builder(cine.getConfig())
                .cliente(cliente)
                .funcion(funcion)
                .entrada(new EntradaGeneral(funcion, asiento))
                .build();
    }
}
