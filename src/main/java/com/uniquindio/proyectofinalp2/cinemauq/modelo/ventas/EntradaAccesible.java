package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;

public class EntradaAccesible extends Entrada {

    private static final double DESCUENTO = 0.10;

    public EntradaAccesible(Funcion funcion, Asiento asiento) {
        super(funcion, validarAccesible(asiento));
    }

    private static Asiento validarAccesible(Asiento asiento) {
        if (asiento == null || !asiento.esAccesible()) {
            throw new IllegalArgumentException("El asiento debe ser de tipo ACCESIBLE.");
        }
        return asiento;
    }

    @Override
    protected double calcularPrecio() {
        return getFuncion().getPrecioFinal() * (1 - DESCUENTO);
    }
}