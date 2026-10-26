package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

public class UsuarioDuplicadoException extends CinemaException {

    private final String correo;

    public UsuarioDuplicadoException(String correo) {
        super("Ya existe una cuenta con ese correo");
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }
}