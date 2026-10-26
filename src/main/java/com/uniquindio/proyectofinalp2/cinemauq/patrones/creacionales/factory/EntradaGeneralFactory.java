package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaGeneral;

public class EntradaGeneralFactory extends EntradaFactory {

    @Override
    protected Entrada crearEntrada(Funcion funcion, Asiento asiento) {
        return new EntradaGeneral(funcion, asiento);
    }
}