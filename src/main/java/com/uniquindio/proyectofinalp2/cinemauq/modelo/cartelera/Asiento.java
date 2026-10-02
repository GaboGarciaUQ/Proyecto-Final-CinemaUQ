package com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera;

import java.util.Objects;

public class Asiento {

    private char fila;
    private int numero;
    private TipoAsiento tipo;

    public Asiento() {
    }

    public Asiento(char fila, int numero, TipoAsiento tipo) {
        this.fila = fila;
        this.numero = numero;
        this.tipo = tipo;
    }

    public char getFila() {
        return fila;
    }

    public int getNumero() {
        return numero;
    }

    public TipoAsiento getTipo() {
        return tipo;
    }

    public boolean esAccesible() {
        return tipo == TipoAsiento.ACCESIBLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Asiento asiento = (Asiento) o;

        return fila == asiento.fila
                && numero == asiento.numero
                && tipo == asiento.tipo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fila, numero, tipo);
    }

    @Override
    public String toString() {
        return "Asiento{" +
                "fila=" + fila +
                ", numero=" + numero +
                ", tipo=" + tipo +
                '}';
    }
}