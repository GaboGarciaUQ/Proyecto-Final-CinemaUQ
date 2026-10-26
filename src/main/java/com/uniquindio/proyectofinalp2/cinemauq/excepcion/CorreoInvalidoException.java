package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

public class CorreoInvalidoException extends CinemaException {

    public CorreoInvalidoException() {
        super("Escribe un correo válido");
    }
}