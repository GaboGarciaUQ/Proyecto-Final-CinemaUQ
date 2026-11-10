package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy;

import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CinemaException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;

/*
 Client del Strategy: demo en consola (sin JavaFX).
 Ejecutar: MainStrategyPuntos
*/
public class MainStrategyPuntos {

    public static void main(String[] args) {
        ConfiguracionCine config = ConfiguracionCine.getInstancia();

        System.out.println("=== STRATEGY: cálculo de puntos ===");
        double[] valores = {25500, 49000, 999, 0};
        EstrategiaPuntos[] politicas = {new PuntosBase(config), new PuntosDobles(config), new SinPuntos()};
        for (EstrategiaPuntos e : politicas) {
            System.out.println("-- " + e.getNombre());
            for (double v : valores) {
                System.out.println("   $" + (long) v + " -> " + e.calcular(v) + " pts");
            }
        }

        System.out.println();
        System.out.println("=== STRATEGY integrado a la confirmación de compra ===");
        CineServicio servicio = new CineServicio();
        Cliente ana = (Cliente) servicio.autenticar("ana@correo.com").get();
        Funcion funcion = servicio.getFuncionesDe(servicio.getPeliculasEnCartelera().get(0)).get(0);
        List<Asiento> libres = servicio.getFuncionesDe(funcion.getPelicula()).get(0)
                .getSala().getAsientos();
        Asiento[] elegidos = elegirLibres(funcion, libres, 3);

        comprar(servicio, ana, funcion, elegidos[0], "Política base");

        servicio.setEstrategiaPuntos(new PuntosDobles(config));
        comprar(servicio, ana, funcion, elegidos[1], "Campaña puntos dobles (cambio en tiempo de ejecución)");

        servicio.setEstrategiaPuntos(new SinPuntos());
        comprar(servicio, ana, funcion, elegidos[2], "Acumulación suspendida");

        System.out.println();
        System.out.println("Historial de puntos de " + ana.getNombre() + ":");
        for (PuntosAcumulados p : servicio.getHistorialPuntos(ana)) {
            System.out.println("   " + p);
        }
        System.out.println("Puntos vigentes: " + servicio.getPuntosVigentes(ana));

        System.out.println();
        System.out.println("=== Doble acumulación sobre la misma compra ===");
        ServicioPuntos sp = new ServicioPuntos(config, new PuntosBase(config));
        Compra c = servicio.getComprasDe(ana).get(0);
        sp.confirmarCompra(c);
        try {
            sp.confirmarCompra(c);
        } catch (CinemaException e) {
            System.out.println("Rechazado: " + e.getMessage());
        }
    }

    private static Asiento[] elegirLibres(Funcion funcion, List<Asiento> todos, int cantidad) {
        Asiento[] r = new Asiento[cantidad];
        int i = 0;
        for (Asiento a : todos) {
            if (i < cantidad && funcion.estaDisponible(a) && !a.esAccesible()) {
                r[i++] = a;
            }
        }
        return r;
    }

    private static void comprar(CineServicio servicio, Cliente cliente, Funcion funcion,
                                Asiento asiento, String titulo) {
        Tarjeta t = servicio.getTarjeta(cliente);
        double saldoAntes = t.getSaldo();
        Compra compra = servicio.comprar(cliente, funcion, List.of(asiento),
                List.of(), List.of());
        System.out.println("[" + titulo + "]");
        System.out.println("   Total pagado: $" + (long) compra.getTotal()
                + " | Estado: " + compra.getEstado()
                + " | Puntos ganados: " + servicio.puntosPorCompra(compra.getTotal())
                + " | Saldo: $" + (long) saldoAntes + " -> $" + (long) t.getSaldo());
    }
}
