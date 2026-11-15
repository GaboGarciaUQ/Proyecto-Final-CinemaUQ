package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.state;

import java.time.LocalDateTime;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.PoliticaReembolso;

public final class EstadoPendiente implements EstadoDeCompra {

    public static final EstadoPendiente INSTANCIA = new EstadoPendiente();

    private EstadoPendiente() {
    }

    @Override
    public EstadoCompra getEstado() {
        return EstadoCompra.PENDIENTE;
    }

    @Override
    public void pagar(Compra compra) {
        compra.cambiarEstado(EstadoPagada.INSTANCIA);
    }

    @Override
    public double cancelar(Compra compra, PoliticaReembolso politica, LocalDateTime ahora) {
        compra.liberarEntradas();
        compra.cambiarEstado(EstadoCancelada.INSTANCIA);
        return 0.0;
    }
}