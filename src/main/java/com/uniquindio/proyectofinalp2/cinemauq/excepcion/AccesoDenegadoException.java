package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

public class AccesoDenegadoException extends CinemaException {

    public AccesoDenegadoException(String usuario, String operacion) {
        super("Acceso denegado: " + usuario + " no puede " + operacion);
    }
}