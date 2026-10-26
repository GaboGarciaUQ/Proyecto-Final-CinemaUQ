package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory;

import java.util.ArrayList;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Tarifa;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.TipoAsiento;

public class SalaFactory4DX implements SalaFactory {

    private static final int FILAS = 4;
    private static final int ASIENTOS_POR_FILA = 8;

    @Override
    public Sala crearSala(String id, String nombre) {
        List<Asiento> asientos = new ArrayList<>();
        for (int f = 0; f < FILAS; f++) {
            char fila = (char) ('A' + f);
            for (int n = 1; n <= ASIENTOS_POR_FILA; n++) {
                boolean accesible = fila == 'D' && (n == 4 || n == 5);
                asientos.add(new Asiento(fila, n,
                        accesible ? TipoAsiento.ACCESIBLE : TipoAsiento.GENERAL));
            }
        }
        return new Sala(id, nombre, asientos, crearTarifa());
    }

    @Override
    public Tarifa crearTarifa() {
        return new Tarifa("4DX", 0.50);
    }
}