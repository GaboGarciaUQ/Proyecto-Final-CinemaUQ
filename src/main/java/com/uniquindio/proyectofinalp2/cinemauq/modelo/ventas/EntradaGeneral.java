package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;


/*Entrada para un asiento general.
PRODUCTO CONCRETO del patrón Factory Method.
Se cobra al precio base de la función, sin descuentos.
*/
public class EntradaGeneral extends Entrada {

    // Público porque las fábricas están en otro paquete.
    public EntradaGeneral(Funcion funcion, Asiento asiento) {
        super(funcion, asiento);
    }

    @Override
    protected double calcularPrecio() {
        return getFuncion().getPrecioBase();
    }
}