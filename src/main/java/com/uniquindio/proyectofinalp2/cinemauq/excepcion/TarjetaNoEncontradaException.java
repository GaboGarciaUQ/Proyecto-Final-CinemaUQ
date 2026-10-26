package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

public class TarjetaNoEncontradaException extends CinemaException {

    public TarjetaNoEncontradaException() {
        super("El cliente no tiene tarjeta virtual");
    }
}