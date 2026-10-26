package com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos;

import java.time.LocalDateTime;

public class Movimiento {

    private final TipoMovimiento tipo;
    private final double monto;
    private final double saldoResultante;
    private final LocalDateTime fecha;
    private final String referencia;

    public Movimiento(TipoMovimiento tipo, double monto, double saldoResultante,
                      LocalDateTime fecha, String referencia) {
        this.tipo = tipo;
        this.monto = monto;
        this.saldoResultante = saldoResultante;
        this.fecha = fecha;
        this.referencia = referencia;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public double getSaldoResultante() {
        return saldoResultante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    @Override
    public String toString() {
        return tipo + " $" + monto + " (saldo $" + saldoResultante + ") " + referencia;
    }
}