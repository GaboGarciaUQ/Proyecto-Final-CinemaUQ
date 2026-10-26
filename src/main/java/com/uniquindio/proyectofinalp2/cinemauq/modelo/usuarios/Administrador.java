package com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios;

public class Administrador extends Usuario {

    public Administrador(String id, String nombre, String correo) {
        super(id, nombre, correo);
    }

    @Override
    public boolean esAdministrador() {
        return true;
    }
}