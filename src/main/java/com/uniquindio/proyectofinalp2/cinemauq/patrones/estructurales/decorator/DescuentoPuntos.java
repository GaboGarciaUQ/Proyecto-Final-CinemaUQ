package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CompraInvalidaException;

// ConcreteDecorator: descuenta el valor de los beneficios canjeados con puntos.
public class DescuentoPuntos extends CobroDecorator {

    private final int puntos;
    private final double valorDescontado;

    public DescuentoPuntos(Cobro envuelto, int puntos, double valorDescontado) {
        super(envuelto);
        if (puntos < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }
        if (valorDescontado < 0) {
            throw new IllegalArgumentException("El valor descontado no puede ser negativo");
        }
        if (valorDescontado > envuelto.getValor() + 0.01) {
            throw new CompraInvalidaException("Los puntos redimidos superan el valor de la compra");
        }
        this.puntos = puntos;
        this.valorDescontado = valorDescontado;
    }

    @Override
    public double getValor() {
        return Math.max(0, envuelto.getValor() - valorDescontado);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - " + puntos + " puntos";
    }
}