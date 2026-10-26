package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

public class TarjetaInactivaException extends CinemaException {

    public TarjetaInactivaException() {
        super("La tarjeta está inactiva");
    }
}