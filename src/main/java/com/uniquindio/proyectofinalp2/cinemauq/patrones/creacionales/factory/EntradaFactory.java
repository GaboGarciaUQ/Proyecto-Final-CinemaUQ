package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;

/*=====FACTORY METHOD=====*/

public abstract class EntradaFactory {

    public final Entrada emitirEntrada(Funcion funcion, Asiento asiento){
        if(funcion==null||asiento==null){
            throw new IllegalStateException("La función y el asiento son obligatorios");
        }
        if (!funcion.estaDisponible(asiento)) {
            throw new IllegalStateException("El asiento no esta disponible")
        }
        return entrada;
    }
}