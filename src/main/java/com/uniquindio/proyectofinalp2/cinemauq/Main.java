package com.uniquindio.proyectofinalp2.cinemauq;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CinemaException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.TarjetaVirtual;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory2D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory3D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory4DX;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaAccesibleFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaGeneralFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.GeneradorCsv;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.GeneradorHtml;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.GeneradorPdf;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.GeneradorReporte;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.GeneradorTxt;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConsecutivoFactura;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.facade.CompraFacade;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.proxy.TarjetaProxy;

public class Main {

    private static Asiento buscar(Sala sala, char fila, int numero) {
        for (Asiento a : sala.getAsientos()) {
            if (a.getFila() == fila && a.getNumero() == numero) {
                return a;
            }
        }
        throw new IllegalArgumentException("No existe el asiento " + fila + numero);
    }

    public static void main(String[] args) {

        System.out.println("=== SINGLETON ===");
        ConsecutivoFactura a = ConsecutivoFactura.getInstancia();
        ConsecutivoFactura b = ConsecutivoFactura.getInstancia();
        System.out.println("a == b: " + (a == b));
        System.out.println("Factura " + a.siguiente());
        System.out.println("Factura " + b.siguiente());

        ConfiguracionCine config = ConfiguracionCine.getInstancia();
        System.out.println("Puntos por $25.500: " + config.calcularPuntos(25500));

        System.out.println("\n=== ABSTRACT FACTORY ===");
        SalaFactory[] fabricasSala = {new SalaFactory2D(), new SalaFactory3D(), new SalaFactory4DX()};
        int numeroSala = 1;
        for (SalaFactory fabrica : fabricasSala) {
            Sala sala = fabrica.crearSala("S" + numeroSala, "Sala " + numeroSala);
            System.out.println(sala + " - capacidad " + sala.getCapacidad()
                    + " - tarifa " + sala.getTarifa());
            numeroSala++;
        }

        Pelicula peliculaInterstellar = new Pelicula("P1", "Interestelar", 169);
        Sala sala3D = new SalaFactory3D().crearSala("S3D", "Sala 3D");
        Funcion funcion = new Funcion("F1", peliculaInterstellar, sala3D,
                LocalDateTime.now().plusDays(2), 15000);
        System.out.println("Precio base: $" + funcion.getPrecioBase()
                + " - precio final en 3D: $" + funcion.getPrecioFinal());

        System.out.println("\n=== PROTOTYPE ===");
        funcion.ocupar(buscar(sala3D, 'A', 10));
        Funcion copia = funcion.clonar("F2", LocalDateTime.now().plusDays(3));
        Funcion copiaSimple = funcion.clonar();
        System.out.println("Original ocupados: " + funcion.getCantidadOcupados());
        System.out.println("Copia ocupados: " + copia.getCantidadOcupados());
        System.out.println("Copia es otro objeto: " + (copia != funcion));
        System.out.println("Id de clonar(): " + copiaSimple.getId());

        System.out.println("\n=== FACTORY METHOD: ENTRADAS ===");
        EntradaFactory fabricaGeneral = new EntradaGeneralFactory();
        EntradaFactory fabricaAccesible = new EntradaAccesibleFactory();

        Asiento asientoA1 = buscar(sala3D, 'A', 1);
        Asiento asientoA2 = buscar(sala3D, 'A', 2);
        Asiento asientoAccesible = buscar(sala3D, 'F', 5);

        Entrada entrada1 = fabricaGeneral.emitirEntrada(funcion, asientoA1);
        Entrada entrada2 = fabricaGeneral.emitirEntrada(funcion, asientoA2);
        Entrada entradaAcc = fabricaAccesible.emitirEntrada(funcion, asientoAccesible);
        System.out.println(entrada1);
        System.out.println(entradaAcc);

        try {
            fabricaAccesible.emitirEntrada(funcion, buscar(sala3D, 'A', 3));
        } catch (IllegalArgumentException e) {
            System.out.println("Accesible en asiento general: " + e.getMessage());
        }

        try {
            fabricaGeneral.emitirEntrada(funcion, asientoA1);
        } catch (CinemaException e) {
            System.out.println("Asiento vendido dos veces: " + e.getMessage());
        }

        System.out.println("\n=== FACTORY METHOD: REPORTES ===");
        List<String> detalleReporte = new ArrayList<>();
        detalleReporte.add("Interestelar - 3 entradas - $45000");
        detalleReporte.add("Avatar - 2 entradas - $30000");
        GeneradorReporte[] generadores = {new GeneradorTxt(), new GeneradorCsv(), new GeneradorPdf(), new GeneradorHtml()};
        for (GeneradorReporte g : generadores) {
            try {
                Path archivo = g.generar("Ventas por periodo", "01/10/2026 - 14/10/2026",
                        detalleReporte, 75000, Paths.get("reportes"));
                System.out.println("Generado: " + archivo);
            } catch (IOException e) {
                System.out.println("Error al generar: " + e.getMessage());
            }
        }

        System.out.println("\n=== BUILDER ===");
        Cliente cliente = new Cliente("1", "Ana", "ana@correo.com");

        List<Producto> productosCombo = new ArrayList<>();
        productosCombo.add(new Producto("PR1", "Palomitas", 12000));
        productosCombo.add(new Producto("PR2", "Gaseosa", 8000));
        Combo combo = new Combo("C1", "Combo Pareja", productosCombo, 0.15);
        Promocion promocion = new Promocion("Martes 10%", 0.10);

        Compra minima = new Compra.Builder(config)
                .cliente(cliente)
                .funcion(funcion)
                .entrada(entrada1)
                .build();
        System.out.println("Compra mínima: $" + minima.getTotal() + " - " + minima.getEstado());

        Compra completa = new Compra.Builder(config)
                .cliente(cliente)
                .funcion(funcion)
                .entrada(entrada2)
                .entrada(entradaAcc)
                .combo(combo)
                .promocion(promocion)
                .puntosRedimidos(50)
                .build();
        System.out.println("Compra completa: $" + completa.getTotal() + " - " + completa.getEstado());
        completa.marcarPagada();
        System.out.println("Después de pagar: " + completa.getEstado());
        System.out.println("Puntos ganados: " + config.calcularPuntos(completa.getTotal()));

        System.out.println("\n=== VALIDACIONES DEL BUILDER ===");
        try {
            new Compra.Builder(config).cliente(cliente).funcion(funcion).build();
        } catch (CinemaException e) {
            System.out.println("Sin entradas: " + e.getMessage());
        }

        try {
            new Compra.Builder(config).funcion(funcion).entrada(entrada1).build();
        } catch (CinemaException e) {
            System.out.println("Sin cliente: " + e.getMessage());
        }

        try {
            new Compra.Builder(config).cliente(cliente).funcion(copia).entrada(entrada1).build();
        } catch (CinemaException e) {
            System.out.println("Función distinta: " + e.getMessage());
        }

        try {
            new Compra.Builder(config).cliente(cliente).funcion(funcion).entrada(entrada1)
                    .puntosRedimidos(100000).build();
        } catch (CinemaException e) {
            System.out.println("Demasiados puntos: " + e.getMessage());
        }
                System.out.println("\n=== PROXY ===");
        Administrador admin = new Administrador("A1", "Carlos", "admin@cinemauq.com");
        Cliente intruso = new Cliente("2", "Luis", "luis@correo.com");
        TarjetaVirtual tarjetaReal = new TarjetaVirtual("T1", cliente);

        Tarjeta tarjetaCliente = new TarjetaProxy(tarjetaReal, cliente);
        Tarjeta tarjetaAdmin = new TarjetaProxy(tarjetaReal, admin);
        Tarjeta tarjetaIntruso = new TarjetaProxy(tarjetaReal, intruso);

        tarjetaAdmin.recargar(100000);
        System.out.println("Saldo tras la recarga del admin: $" + tarjetaCliente.getSaldo());
        tarjetaCliente.pagar(minima.getTotal(), minima.getId());
        minima.marcarPagada();
        System.out.println("Saldo tras pagar la compra: $" + tarjetaCliente.getSaldo());
        System.out.println("Movimientos: " + tarjetaAdmin.getMovimientos().size());

        try {
            tarjetaCliente.recargar(50000);
        } catch (CinemaException e) {
            System.out.println("Cliente intenta recargar: " + e.getMessage());
        }
        try {
            tarjetaIntruso.getSaldo();
        } catch (CinemaException e) {
            System.out.println("Otro cliente consulta el saldo: " + e.getMessage());
        }
        try {
            tarjetaAdmin.pagar(1000, "X");
        } catch (CinemaException e) {
            System.out.println("Admin intenta pagar: " + e.getMessage());
        }

        tarjetaAdmin.desactivar();
        try {
            tarjetaCliente.pagar(1000, "X");
        } catch (CinemaException e) {
            System.out.println("Tarjeta inactiva: " + e.getMessage());
        }
        tarjetaAdmin.activar();
        try {
            tarjetaCliente.pagar(10000000, "X");
        } catch (CinemaException e) {
            System.out.println("Pago sin saldo: " + e.getMessage());
        }
                System.out.println("\n=== FACADE ===");
        CompraFacade fachada = new CompraFacade(config);
        Funcion funcionFachada = new Funcion("F3", peliculaInterstellar, sala3D,
                LocalDateTime.now().plusDays(4), 15000);
        System.out.println("Asientos libres: " + fachada.asientosDisponibles(funcionFachada).size());

        List<Asiento> elegidos = List.of(buscar(sala3D, 'B', 1), buscar(sala3D, 'B', 2));
        Compra compraFachada = fachada.comprar(tarjetaCliente, funcionFachada, elegidos,
                combo, List.of(promocion), 0);
        System.out.println("Compra: $" + compraFachada.getTotal() + " - " + compraFachada.getEstado());
        System.out.println("Detalle: " + compraFachada.getDescripcionCobro());
        System.out.println("Saldo de la tarjeta: $" + tarjetaCliente.getSaldo());
        System.out.println("Asientos libres: " + fachada.asientosDisponibles(funcionFachada).size());

        List<Asiento> otros = List.of(buscar(sala3D, 'C', 1), buscar(sala3D, 'C', 2));
        try {
            fachada.comprar(tarjetaCliente, funcionFachada, otros, null, List.of(), 0);
        } catch (CinemaException e) {
            System.out.println("Compra sin saldo: " + e.getMessage());
        }
        try {
            fachada.comprar(tarjetaIntruso, funcionFachada, otros, null, List.of(), 0);
        } catch (CinemaException e) {
            System.out.println("Compra con tarjeta ajena: " + e.getMessage());
        }
        System.out.println("Asientos libres tras los fallos: "
                + fachada.asientosDisponibles(funcionFachada).size());
    }
}