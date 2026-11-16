package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

public class Pelicula {

    private final String id;
    private String titulo;
    private int duracionMinutos;
    private String posterUrl;

    public Pelicula(String id, String titulo, int duracionMinutos) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La película necesita un id");
        }
        validar(titulo, duracionMinutos);
        this.id = id;
        this.titulo = titulo.trim();
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

    public String getPosterUrl() {
        return posterUrl;
    }

    // URL del póster; vacío significa "sin póster".
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl == null || posterUrl.isBlank() ? null : posterUrl.trim();
    }

    public void setTitulo(String titulo) {
        validar(titulo, duracionMinutos);
        this.titulo = titulo.trim();
    }

    public void setDuracionMinutos(int duracionMinutos) {
        validar(titulo, duracionMinutos);
        this.duracionMinutos = duracionMinutos;
    }

    private static void validar(String titulo, int duracionMinutos) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("La película necesita un título");
        }
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero");
        }
    }

    @Override
    public String toString() {
        return titulo + " (" + duracionMinutos + " min)";
    }
}