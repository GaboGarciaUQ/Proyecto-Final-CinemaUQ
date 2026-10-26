package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaAccesible;

public class EntradaAccesibleFactory extends EntradaFactory {

    @Override
    protected Entrada crearEntrada(Funcion funcion, Asiento asiento) {
        return new EntradaAccesible(funcion, asiento);
    }
}