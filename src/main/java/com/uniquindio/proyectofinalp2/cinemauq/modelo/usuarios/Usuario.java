package com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios;

public abstract class Usuario {

    private final String id;
    private final String nombre;
    private final String correo;

    protected Usuario(String id, String nombre, String correo) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El usuario necesita un id");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El usuario necesita un nombre");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El usuario necesita un correo");
        }
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public abstract boolean esAdministrador();

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    @Override
    public String toString() {
        return nombre + " (" + correo + ")";
    }
}