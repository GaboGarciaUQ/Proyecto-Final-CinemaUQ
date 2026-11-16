package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos;

import java.time.LocalDate;
import java.util.Objects;

/*
 Registro inmutable de puntos gastados en un canje.
 Dice de qué lote salieron (idLote = id de la compra que los otorgó) y en qué
 compra se gastaron. El lote original nunca se modifica: lo que le queda es
 sus puntos menos la suma de sus consumos.
*/
public final class ConsumoPuntos {

    private final String idLote;
    private final String idCompraCanje;
    private final int puntos;
    private final LocalDate fecha;

    public ConsumoPuntos(String idLote, String idCompraCanje, int puntos, LocalDate fecha) {
        this.idLote = Objects.requireNonNull(idLote, "El lote es obligatorio");
        this.idCompraCanje = Objects.requireNonNull(idCompraCanje, "La compra del canje es obligatoria");
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos consumidos deben ser mayores a cero");
        }
        this.puntos = puntos;
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
    }

    public String getIdLote() {
        return idLote;
    }

    public String getIdCompraCanje() {
        return idCompraCanje;
    }

    public int getPuntos() {
        return puntos;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return puntos + " pts del lote " + idLote + " | canje en compra " + idCompraCanje + " | " + fecha;
    }
}