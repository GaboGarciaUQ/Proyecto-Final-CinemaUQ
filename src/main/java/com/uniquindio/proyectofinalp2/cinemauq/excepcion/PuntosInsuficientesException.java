package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

public class PuntosInsuficientesException extends CinemaException {

    private final int disponibles;
    private final int requeridos;

    public PuntosInsuficientesException(int disponibles, int requeridos) {
        super("Puntos insuficientes: tienes " + disponibles + " vigentes y necesitas " + requeridos);
        this.disponibles = disponibles;
        this.requeridos = requeridos;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public int getRequeridos() {
        return requeridos;
    }
}