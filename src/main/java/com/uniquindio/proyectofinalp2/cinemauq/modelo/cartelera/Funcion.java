package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AsientoNoDisponibleException;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.prototype.Prototipo;

public class Funcion implements Prototipo<Funcion> {

    private final String id;
    private final Pelicula pelicula;
    private final Sala sala;
    private final LocalDateTime fechaHora;
    private final double precioBase;
    private final List<Asiento> ocupados = new ArrayList<>();
    private boolean cancelada;

    public Funcion(String id, Pelicula pelicula, Sala sala, LocalDateTime fechaHora, double precioBase) {
        this.id = id;
        this.pelicula = pelicula;
        this.sala = sala;
        this.fechaHora = fechaHora;
        this.precioBase = precioBase;
    }

    public boolean estaDisponible(Asiento a) {
        return !cancelada && sala.getAsientos().contains(a) && !ocupados.contains(a);
    }

    public void ocupar(Asiento a) {
        if (!estaDisponible(a)) {
            throw new AsientoNoDisponibleException(a);
        }
        ocupados.add(a);
    }

    public void liberar(Asiento a) {
        ocupados.remove(a);
    }

    public void cancelar() {
        cancelada = true;
    }

    public double getPrecioFinal() {
        return sala.getTarifa().aplicar(precioBase);
    }

    /*
     Copia como plantilla: conserva película, sala y precio, pero nace
     sin asientos ocupados y sin cancelar. Película y sala se comparten
     porque son inmutables.
    */
    @Override
    public Funcion clonar() {
        return clonar(id + "-copia", fechaHora);
    }

    public Funcion clonar(String nuevoId, LocalDateTime nuevaFechaHora) {
        return new Funcion(nuevoId, pelicula, sala, nuevaFechaHora, precioBase);
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
        return sala.getCapacidad();
    }

    @Override
    public String toString() {
        return pelicula.getTitulo() + " (" + fechaHora + ")";
    }
}