package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator;

// ConcreteDecorator: descuenta el valor de los puntos redimidos.
public class DescuentoPuntos extends CobroDecorator {

    private final int puntos;
    private final double valorPunto;

    public DescuentoPuntos(Cobro envuelto, int puntos, double valorPunto) {
        super(envuelto);
        if (puntos < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }
        if (valorPunto <= 0) {
            throw new IllegalArgumentException("El valor del punto debe ser mayor a cero");
        }
        if (puntos * valorPunto > envuelto.getValor()) {
            throw new IllegalStateException("Los puntos redimidos superan el valor de la compra");
        }
        this.puntos = puntos;
        this.valorPunto = valorPunto;
    }

    @Override
    public double getValor() {
        return envuelto.getValor() - (puntos * valorPunto);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - " + puntos + " puntos";
    }
}