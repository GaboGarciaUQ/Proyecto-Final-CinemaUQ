package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.state;

import java.time.LocalDateTime;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.PoliticaReembolso;

public interface EstadoDeCompra {

    EstadoCompra getEstado();

    void pagar(Compra compra);

    double cancelar(Compra compra, PoliticaReembolso politica, LocalDateTime ahora);
}