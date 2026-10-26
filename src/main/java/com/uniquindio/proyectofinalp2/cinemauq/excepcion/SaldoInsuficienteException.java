package com.uniquindio.proyectofinalp2.cinemauq.excepcion;

public class SaldoInsuficienteException extends CinemaException {

    private final double saldo;
    private final double requerido;

    public SaldoInsuficienteException(double saldo, double requerido) {
        super("Saldo insuficiente");
        this.saldo = saldo;
        this.requerido = requerido;
    }

    public double getSaldo() {
        return saldo;
    }

    public double getRequerido() {
        return requerido;
    }

    public double getFaltante() {
        return requerido - saldo;
    }
}