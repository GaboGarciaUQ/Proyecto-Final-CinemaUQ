package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso;

import java.time.Duration;

public interface PoliticaReembolso {

    boolean permiteCancelar(Duration tiempoRestante);

    double calcularReembolso(double valorPagado, Duration tiempoRestante);

    String getNombre();
}