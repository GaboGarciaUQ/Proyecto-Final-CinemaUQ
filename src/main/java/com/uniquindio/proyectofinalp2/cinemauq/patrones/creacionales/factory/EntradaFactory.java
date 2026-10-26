package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AsientoNoDisponibleException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;

/*
 PATRÓN CREACIONAL: FACTORY METHOD (Creator abstracto).
 emitirEntrada define el flujo; crearEntrada es el método fábrica.
*/
public abstract class EntradaFactory {

    public final Entrada emitirEntrada(Funcion funcion, Asiento asiento) {
        if (funcion == null || asiento == null) {
            throw new IllegalArgumentException("La función y el asiento son obligatorios");
        }
        if (!funcion.estaDisponible(asiento)) {
            throw new AsientoNoDisponibleException(asiento);
        }
        Entrada entrada = crearEntrada(funcion, asiento);
        funcion.ocupar(asiento);
        return entrada;
    }

    protected abstract Entrada crearEntrada(Funcion funcion, Asiento asiento);
}