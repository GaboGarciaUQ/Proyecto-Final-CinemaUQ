package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.state;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CancelacionNoPermitidaException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.PoliticaReembolso;

public final class EstadoPagada implements EstadoDeCompra {

    public static final EstadoPagada INSTANCIA = new EstadoPagada();

    private EstadoPagada() {
    }

    @Override
    public EstadoCompra getEstado() {
        return EstadoCompra.PAGADA;
    }

    @Override
    public void pagar(Compra compra) {
        throw new EstadoInvalidoException("La compra ya está pagada");
    }

    @Override
    public double cancelar(Compra compra, PoliticaReembolso politica, LocalDateTime ahora) {
        Objects.requireNonNull(politica, "La política de reembolso es obligatoria");
        Objects.requireNonNull(ahora, "La fecha de la cancelación es obligatoria");

        Duration restante = Duration.between(ahora, compra.getFuncion().getFechaHora());
        if (!politica.permiteCancelar(restante)) {
            throw new CancelacionNoPermitidaException(
                    "No se puede cancelar: faltan " + Math.max(restante.toMinutes(), 0)
                            + " min para la función (" + politica.getNombre() + ")");
        }
        double monto = politica.calcularReembolso(compra.getTotal(), restante);

        compra.liberarEntradas();
        compra.registrarReembolso(monto);
        compra.cambiarEstado(EstadoReembolsada.INSTANCIA);
        return monto;
    }
}