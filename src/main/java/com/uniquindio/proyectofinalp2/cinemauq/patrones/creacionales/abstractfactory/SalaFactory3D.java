package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory;

import java.util.ArrayList;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Tarifa;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.TipoAsiento;

public class SalaFactory3D implements SalaFactory {

    private static final int FILAS = 6;
    private static final int ASIENTOS_POR_FILA = 10;

    @Override
    public Sala crearSala(String id, String nombre) {
        List<Asiento> asientos = new ArrayList<>();
        for (int f = 0; f < FILAS; f++) {
            char fila = (char) ('A' + f);
            for (int n = 1; n <= ASIENTOS_POR_FILA; n++) {
                boolean accesible = fila == 'F' && (n == 5 || n == 6);
                asientos.add(new Asiento(fila, n,
                        accesible ? TipoAsiento.ACCESIBLE : TipoAsiento.GENERAL));
            }
        }
        return new Sala(id, nombre, asientos, crearTarifa());
    }

    @Override
    public Tarifa crearTarifa() {
        return new Tarifa("3D", 0.20);
    }
}