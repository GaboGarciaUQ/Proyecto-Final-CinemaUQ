package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Sala {

    private final String id;
    private final String nombre;
    private final List<Asiento> asientos;
    private final Tarifa tarifa;

    public Sala(String id, String nombre, List<Asiento> asientos, Tarifa tarifa) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La sala necesita un id");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La sala necesita un nombre");
        }
        if (asientos == null || asientos.isEmpty()) {
            throw new IllegalArgumentException("La sala necesita al menos un asiento");
        }
        if (tarifa == null) {
            throw new IllegalArgumentException("La sala necesita una tarifa");
        }
        this.id = id;
        this.nombre = nombre;
        this.asientos = new ArrayList<>(asientos);
        this.tarifa = tarifa;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Asiento> getAsientos() {
        return Collections.unmodifiableList(asientos);
    }

    public Optional<Asiento> getAsiento(char fila, int numero) {
        for (Asiento a : asientos) {
            if (a.getFila() == fila && a.getNumero() == numero) {
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    public int getCapacidad() {
        return asientos.size();
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    @Override
    public String toString() {
        return nombre + " [" + tarifa.getNombre() + "]";
    }
}