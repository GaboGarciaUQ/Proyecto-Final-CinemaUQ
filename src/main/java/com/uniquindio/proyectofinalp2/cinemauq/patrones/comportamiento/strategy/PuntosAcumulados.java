package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy;

import java.time.LocalDate;

/*
 Registro inmutable de una acumulación: conserva la fecha de obtención y la de vencimiento
 (vigencia de un año), tal como exige la sección 4 del enunciado.
*/
public final class PuntosAcumulados {

    private final String idCompra;
    private final int puntos;
    private final LocalDate fechaObtencion;
    private final LocalDate fechaVencimiento;
    private final String politica;

    public PuntosAcumulados(String idCompra, int puntos, LocalDate fechaObtencion,
                            LocalDate fechaVencimiento, String politica) {
        this.idCompra = idCompra;
        this.puntos = puntos;
        this.fechaObtencion = fechaObtencion;
        this.fechaVencimiento = fechaVencimiento;
        this.politica = politica;
    }

    public boolean estaVigente(LocalDate hoy) {
        return hoy.isBefore(fechaVencimiento);
    }

    public String getIdCompra() {
        return idCompra;
    }

    public int getPuntos() {
        return puntos;
    }

    public LocalDate getFechaObtencion() {
        return fechaObtencion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getPolitica() {
        return politica;
    }

    @Override
    public String toString() {
        return puntos + " pts | obtenidos " + fechaObtencion + " | vencen " + fechaVencimiento
                + " | política: " + politica;
    }
}
