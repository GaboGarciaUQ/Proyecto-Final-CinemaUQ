package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CompraInvalidaException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaAccesibleFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.EntradaGeneralFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

// Facade: una sola entrada para que la interfaz compre sin conocer los demás patrones.
public class CompraFacade {

    private final ConfiguracionCine config;
    private final EntradaFactory fabricaGeneral = new EntradaGeneralFactory();
    private final EntradaFactory fabricaAccesible = new EntradaAccesibleFactory();

    public CompraFacade(ConfiguracionCine config) {
        this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
    }

    public List<Asiento> asientosDisponibles(Funcion funcion) {
        List<Asiento> libres = new ArrayList<>();
        for (Asiento a : funcion.getSala().getAsientos()) {
            if (funcion.estaDisponible(a)) {
                libres.add(a);
            }
        }
        return libres;
    }

    // Compra sin canje de puntos.
    public Compra comprar(Tarjeta tarjeta, Funcion funcion, List<Asiento> asientos,
                          Combo combo, List<Promocion> promociones) {
        return comprar(tarjeta, funcion, asientos, combo, promociones,
                new ArrayList<>(), new ArrayList<>(), null);
    }

    /*
     asientosCanjeados: asientos de esta compra que se pagan con puntos.
     confiteriaCanjeada: productos o combos de la confitería de esta compra que se pagan con puntos.
     verificacionPrevia: se ejecuta con la compra ya armada y ANTES de cobrar (puede ser null).
     Si lanza excepción no se cobra nada y los asientos se liberan.
    */
    public Compra comprar(Tarjeta tarjeta, Funcion funcion, List<Asiento> asientos,
                          Combo combo, List<Promocion> promociones,
                          List<Asiento> asientosCanjeados, List<Comprable> confiteriaCanjeada,
                          Consumer<Compra> verificacionPrevia) {
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria");
        Objects.requireNonNull(funcion, "La función es obligatoria");
        Objects.requireNonNull(asientos, "Los asientos son obligatorios");
        Objects.requireNonNull(promociones, "La lista de promociones es obligatoria");
        Objects.requireNonNull(asientosCanjeados, "La lista de asientos canjeados es obligatoria");
        Objects.requireNonNull(confiteriaCanjeada, "La lista de confitería canjeada es obligatoria");

        List<Entrada> entradas = new ArrayList<>();
        try {
            for (Asiento a : asientos) {
                EntradaFactory fabrica = a.esAccesible() ? fabricaAccesible : fabricaGeneral;
                entradas.add(fabrica.emitirEntrada(funcion, a));
            }

            Compra.Builder builder = new Compra.Builder(config)
                    .cliente(tarjeta.getTitular())
                    .funcion(funcion)
                    .combo(combo);
            for (Entrada e : entradas) {
                builder.entrada(e);
            }
            for (Promocion p : promociones) {
                builder.promocion(p);
            }
            for (Asiento a : asientosCanjeados) {
                builder.canjearEntrada(buscarEntrada(entradas, a));
            }
            for (Comprable item : confiteriaCanjeada) {
                builder.canjearConfiteria(item);
            }
            Compra compra = builder.build();

            if (verificacionPrevia != null) {
                verificacionPrevia.accept(compra);
            }
            if (compra.getTotal() > 0) {
                tarjeta.pagar(compra.getTotal(), compra.getId());
            }
            compra.marcarPagada();
            return compra;
        } catch (RuntimeException e) {
            for (Entrada entrada : entradas) {
                funcion.liberar(entrada.getAsiento());
            }
            throw e;
        }
    }

    private Entrada buscarEntrada(List<Entrada> entradas, Asiento asiento) {
        for (Entrada e : entradas) {
            if (e.getAsiento() == asiento) {
                return e;
            }
        }
        throw new CompraInvalidaException("Solo se pueden canjear asientos de esta compra");
    }
}