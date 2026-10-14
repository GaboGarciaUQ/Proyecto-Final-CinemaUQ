package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

public class Pelicula {

    private final String id;
    private final String titulo;
    private final int duracionMinutos;

    public Pelicula(String id, String titulo, int duracionMinutos) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La película necesita un id");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("La película necesita un título");
        }
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero");
        }
        
        this.id = id;
        this.titulo = titulo;
        this.duracionMinutos = duracionMinutos;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    @Override
    public String toString() {
        return titulo + " (" + duracionMinutos + " min)";
    }
}