package com.uniquindio.proyectofinalp2.cinemauq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.TipoAsiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaAccesibleFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaGeneralFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConsecutivoFactura;

public class Main {

    public static void main(String[] args) {

        // ===== SINGLETON =====
        System.out.println("=== SINGLETON ===");
        ConsecutivoFactura a = ConsecutivoFactura.getInstancia();
        ConsecutivoFactura b = ConsecutivoFactura.getInstancia();
        System.out.println("a == b: " + (a == b));
        System.out.println("Factura " + a.siguiente());
        System.out.println("Factura " + b.siguiente());
        // new ConsecutivoFactura(); // no compila: constructor privado

        // Único getInstancia() de configuración: se inyecta desde aquí
        ConfiguracionCine config = ConfiguracionCine.getInstancia();
        System.out.println("Puntos por $25.500: " + config.calcularPuntos(25500));

        // ===== Datos base =====
        Pelicula pelicula = new Pelicula("P1", "Interestelar", 169);
        Sala sala = new Sala("S1", "Sala 1");

        List<Asiento> asientos = new ArrayList<>();
        char[] filas = {'A', 'B'};
        for (char fila : filas) {
            for (int n = 1; n <= 5; n++) {
                asientos.add(new Asiento(fila, n, TipoAsiento.GENERAL));
            }
        }
        Asiento accesible = new Asiento('C', 1, TipoAsiento.ACCESIBLE);
        asientos.add(accesible);

        Funcion funcion = new Funcion("F1", pelicula, sala,
                LocalDateTime.now().plusDays(2), 15000, asientos);

        // ===== PROTOTYPE =====
        System.out.println("\n=== PROTOTYPE ===");
        Funcion copia = funcion.clonar("F2", LocalDateTime.now().plusDays(3));
        funcion.ocupar(asientos.get(0));
        System.out.println("Original ocupados: " + funcion.getCantidadOcupados());
        System.out.println("Copia ocupados: " + copia.getCantidadOcupados());
        System.out.println("Copia es otro objeto: " + (copia != funcion));

        // ===== FACTORY METHOD =====
        System.out.println("\n=== FACTORY METHOD ===");
        EntradaFactory fabricaGeneral = new EntradaGeneralFactory();
        EntradaFactory fabricaAccesible = new EntradaAccesibleFactory();

        Entrada entrada1 = fabricaGeneral.crearEntrada(funcion, asientos.get(1));
        Entrada entrada2 = fabricaGeneral.crearEntrada(funcion, asientos.get(2));
        Entrada entradaAcc = fabricaAccesible.crearEntrada(funcion, accesible);
        System.out.println(entrada1);
        System.out.println(entradaAcc);

        // ===== BUILDER =====
        System.out.println("\n=== BUILDER ===");
        Cliente cliente = new Cliente("1", "Ana", "ana@correo.com");

        Producto palomitas = new Producto("PR1", "Palomitas", 12000);
        Producto gaseosa = new Producto("PR2", "Gaseosa", 8000);
        List<Producto> productosCombo = new ArrayList<>();
        productosCombo.add(palomitas);
        productosCombo.add(gaseosa);
        Combo combo = new Combo("C1", "Combo Pareja", productosCombo, 0.15);

        Promocion promocion = new Promocion("Martes 10%", 0.10);

        Compra minima = new Compra.Builder(config)
                .cliente(cliente)
                .funcion(funcion)
                .entrada(entrada1)
                .build();
        System.out.println("Compra mínima: $" + minima.getTotal()
                + " - " + minima.getEstado());

        Compra completa = new Compra.Builder(config)
                .cliente(cliente)
                .funcion(funcion)
                .entrada(entrada2)
                .entrada(entradaAcc)
                .combo(combo)
                .promocion(promocion)
                .puntosRedimidos(50)
                .build();
        System.out.println("Compra completa: $" + completa.getTotal()
                + " - " + completa.getEstado());
        completa.marcarPagada();
        System.out.println("Después de pagar: " + completa.getEstado());
        System.out.println("Puntos ganados: " + config.calcularPuntos(completa.getTotal()));

        // ===== Validaciones del Builder =====
        System.out.println("\n=== VALIDACIONES ===");
        try {
            new Compra.Builder(config).cliente(cliente).funcion(funcion).build();
        } catch (IllegalStateException e) {
            System.out.println("Sin entradas: " + e.getMessage());
        }

        try {
            new Compra.Builder(config).funcion(funcion).entrada(entrada1).build();
        } catch (IllegalStateException e) {
            System.out.println("Sin cliente: " + e.getMessage());
        }

        try {
            new Compra.Builder(config)
                    .cliente(cliente)
                    .funcion(copia)
                    .entrada(entrada1)
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("Función distinta: " + e.getMessage());
        }

        try {
            new Compra.Builder(config)
                    .cliente(cliente)
                    .funcion(funcion)
                    .entrada(entrada1)
                    .puntosRedimidos(100000)
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("Demasiados puntos: " + e.getMessage());
        }
    }
}