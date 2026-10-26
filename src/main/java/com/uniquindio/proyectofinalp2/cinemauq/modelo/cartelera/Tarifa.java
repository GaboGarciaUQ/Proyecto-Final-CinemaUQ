package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

public class Tarifa {

    private final String nombre;
    private final double recargo; // 0.20 = 20 %

    public Tarifa(String nombre, double recargo) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La tarifa necesita un nombre");
        }
        if (recargo < 0) {
            throw new IllegalArgumentException("El recargo no puede ser negativo");
        }
        this.nombre = nombre;
        this.recargo = recargo;
    }

    public double aplicar(double precioBase) {
        return precioBase * (1 + recargo);
    }

    public String getNombre() {
        return nombre;
    }

    public double getRecargo() {
        return recargo;
    }

    @Override
    public String toString() {
        return nombre + " (+" + (int) (recargo * 100) + "%)";
    }
}