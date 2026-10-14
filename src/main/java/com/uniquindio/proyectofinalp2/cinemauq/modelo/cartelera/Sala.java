package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

public class Sala {

    private final String id;
    private final String nombre;

    public Sala(String id, String nombre) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La sala necesita un id");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La sala necesita un nombre");
        }
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}