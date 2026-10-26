package com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios;

public class Cliente extends Usuario {

    public Cliente(String id, String nombre, String correo) {
        super(id, nombre, correo);
    }

    @Override
    public boolean esAdministrador() {
        return false;
    }
}