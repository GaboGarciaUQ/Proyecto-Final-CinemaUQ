package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.state;

import java.time.LocalDateTime;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.PoliticaReembolso;

public final class EstadoReembolsada implements EstadoDeCompra {

    public static final EstadoReembolsada INSTANCIA = new EstadoReembolsada();

    private EstadoReembolsada() {
    }

    @Override
    public EstadoCompra getEstado() {
        return EstadoCompra.REEMBOLSADA;
    }

    @Override
    public void pagar(Compra compra) {
        throw new EstadoInvalidoException("No se puede pagar una compra reembolsada");
    }

    @Override
    public double cancelar(Compra compra, PoliticaReembolso politica, LocalDateTime ahora) {
        throw new EstadoInvalidoException("La compra ya fue reembolsada; no se puede reembolsar dos veces");
    }
}