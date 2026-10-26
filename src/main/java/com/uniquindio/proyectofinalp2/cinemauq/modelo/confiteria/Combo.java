package com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Composite: un Combo puede contener Productos u otros Combos.
public class Combo implements Comprable {

    private final String id;
    private final String nombre;
    private final List<Comprable> componentes;
    private final double porcentajeDescuento;

    public Combo(String id, String nombre, List<? extends Comprable> componentes, double porcentajeDescuento) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "El nombre es obligatorio");
        Objects.requireNonNull(componentes, "La lista de componentes es obligatoria");
        if (componentes.isEmpty()) {
            throw new IllegalArgumentException("Un combo necesita al menos un componente");
        }
        if (porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 1");
        }
        this.componentes = List.copyOf(componentes);
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public double getPrecio() {
        double suma = 0;
        for (Comprable c : componentes) {
            suma += c.getPrecio();
        }
        return suma * (1 - porcentajeDescuento);
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    public List<Comprable> getComponentes() {
        return Collections.unmodifiableList(componentes);
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    @Override
    public String toString() {
        return nombre + " (" + componentes.size() + " componentes)";
    }
}