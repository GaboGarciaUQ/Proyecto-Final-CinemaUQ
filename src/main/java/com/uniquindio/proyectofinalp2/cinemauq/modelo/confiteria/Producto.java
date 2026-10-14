package com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria;

import java.util.Objects;

public class Producto implements Comprable {

    private final String id;
    private final String nombre;
    private final double precio;

    public Producto(String id, String nombre, double precio) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El producto necesita un id");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El producto necesita un nombre");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    @Override
    public double getPrecio() {
        return precio;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Producto otro = (Producto) o;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " ($" + precio + ")";
    }
}