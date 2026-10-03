package com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Combo implements Comprable {

    private final String id;
    private final String nombre;
    private final List<Producto> productos;
    private final double porcentajeDescuento; 

    public Combo(String id, String nombre, List<Producto> productos, double porcentajeDescuento) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "El nombre es obligatorio");
        Objects.requireNonNull(productos, "La lista de productos es obligatoria");
        if (productos.isEmpty()) {
            throw new IllegalArgumentException("Un combo necesita al menos un producto");
        }
        if (porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 1");
        }
        this.productos = List.copyOf(productos);
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public double getPrecio() {
        double suma = 0;
        for (Producto p : productos) {
            suma += p.getPrecio();
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

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    @Override
    public String toString() {
        return nombre + " (" + productos.size() + " productos)";
    }
}