package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Funcion {

    private final String id;
    private final Pelicula pelicula;
    private final Sala sala;
    private final LocalDateTime fechaHora;
    private final double precioBase;
    private final List<Asiento> asientos;
    private final List<Asiento> ocupados = new ArrayList<>();
    private boolean cancelada;

    public Funcion(String id, Pelicula pelicula, Sala sala,
                   LocalDateTime fechaHora, double precioBase, List<Asiento> asientos) {
        this.id = id;
        this.pelicula = pelicula;
        this.sala = sala;
        this.fechaHora = fechaHora;
        this.precioBase = precioBase;
        this.asientos = new ArrayList<>(asientos);
    }

    public boolean estaDisponible(Asiento a) {
        return !cancelada && asientos.contains(a) && !ocupados.contains(a);
    }

    public void ocupar(Asiento a) {
        if (!estaDisponible(a)) {
            throw new IllegalStateException("Asiento no disponible: " + a);
        }
        ocupados.add(a);
    }

    public void liberar(Asiento a) {
        ocupados.remove(a);
    }

    public void cancelar() {
        cancelada = true;
    }

    // PROTOTYPE: copia profunda, la copia nace sin asientos ocupados
    public Funcion clonar(String nuevoId, LocalDateTime nuevaFechaHora) {
        return new Funcion(nuevoId, pelicula, sala, nuevaFechaHora, precioBase, asientos);
    }

    public String getId() {
        return id;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public Sala getSala() {
        return sala;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public boolean estaCancelada() {
        return cancelada;
    }

    public int getCantidadOcupados() {
        return ocupados.size();
    }

    public int getCapacidad() {
        return asientos.size();
    }

    @Override
    public String toString() {
        return pelicula.getTitulo() + " (" + fechaHora + ")";
    }
}