package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;

// Producto concreto del Factory Method de entradas.
public class EntradaGeneral extends Entrada {

    public EntradaGeneral(Funcion funcion, Asiento asiento) {
        super(funcion, asiento);
    }

    @Override
    protected double calcularPrecio() {
        return getFuncion().getPrecioFinal();
    }
}