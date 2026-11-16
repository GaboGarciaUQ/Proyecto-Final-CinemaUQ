package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.state;

import java.time.LocalDateTime;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.PoliticaReembolso;

public final class EstadoCancelada implements EstadoDeCompra {

    public static final EstadoCancelada INSTANCIA = new EstadoCancelada();

    private EstadoCancelada() {
    }

    @Override
    public EstadoCompra getEstado() {
        return EstadoCompra.CANCELADA;
    }

    @Override
    public void pagar(Compra compra) {
        throw new EstadoInvalidoException("No se puede pagar una compra cancelada");
    }

    @Override
    public double cancelar(Compra compra, PoliticaReembolso politica, LocalDateTime ahora) {
        throw new EstadoInvalidoException("La compra ya está cancelada");
    }
}