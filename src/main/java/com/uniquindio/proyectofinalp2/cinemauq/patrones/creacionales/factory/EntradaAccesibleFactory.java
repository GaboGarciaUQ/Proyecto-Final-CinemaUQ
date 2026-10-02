package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaAccesible;

public class EntradaAccesibleFactory implements EntradaFactory {

    @Override
    public Entrada crearEntrada(Funcion funcion, Asiento asiento) {
        if (asiento == null || !asiento.esAccesible()) {
            throw new IllegalArgumentException(
                    "El asiento debe ser de tipo ACCESIBLE."
            );
        }

        return new EntradaAccesible(funcion, asiento);
    }
}