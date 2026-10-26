package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator;

// ConcreteComponent: el punto de partida de la cadena.
public class CobroBase implements Cobro {

    private final double subtotal;

    public CobroBase(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo");
        }
        this.subtotal = subtotal;
    }

    @Override
    public double getValor() {
        return subtotal;
    }

    @Override
    public String getDescripcion() {
        return "Subtotal";
    }
}