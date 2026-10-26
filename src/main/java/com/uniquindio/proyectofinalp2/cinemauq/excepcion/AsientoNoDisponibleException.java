package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;

public class AsientoNoDisponibleException extends CinemaException {

    private final Asiento asiento;

    public AsientoNoDisponibleException(Asiento asiento) {
        super("Asiento no disponible: " + asiento.getFila() + asiento.getNumero());
        this.asiento = asiento;
    }

    public Asiento getAsiento() {
        return asiento;
    }
}