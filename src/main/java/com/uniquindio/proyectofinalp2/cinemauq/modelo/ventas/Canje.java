package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import java.util.Objects;

/*
 Un beneficio canjeado con puntos dentro de una compra: qué se canjeó, cuánto vale
 en pesos y cuántos puntos costó. Los puntos se fijan al armar la compra, así que un
 cambio posterior en la tabla de equivalencias no altera compras ya hechas.
*/
public final class Canje {

    private final TipoBeneficio tipo;
    private final String descripcion;
    private final double valor;
    private final int puntos;

    public Canje(TipoBeneficio tipo, String descripcion, double valor, int puntos) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo de beneficio es obligatorio");
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción es obligatoria");
        if (valor < 0) {
            throw new IllegalArgumentException("El valor del beneficio no puede ser negativo");
        }
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos del canje deben ser mayores a cero");
        }
        this.valor = valor;
        this.puntos = puntos;
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getValor() {
        return valor;
    }

    public int getPuntos() {
        return puntos;
    }

    @Override
    public String toString() {
        return tipo + ": " + descripcion + " (" + puntos + " pts)";
    }
}