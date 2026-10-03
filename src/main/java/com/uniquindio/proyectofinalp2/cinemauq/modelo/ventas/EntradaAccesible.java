package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;


public class EntradaAccesible extends Entrada {

    /** 10 % de descuento. Es constante estática, por eso ya existe cuando el
     *  constructor de Entrada llama a calcularPrecio(). */
    private static final double DESCUENTO = 0.10;

    // Público porque las fábricas están en otro paquete.
    public EntradaAccesible(Funcion funcion, Asiento asiento) {
        super(funcion, asiento);
    }

    @Override
    protected double calcularPrecio() {
        return getFuncion().getPrecioBase() * (1 - DESCUENTO);
    }
}