package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso;

import java.time.Duration;
import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

public class ReembolsoPorAnticipacion implements PoliticaReembolso {

    private final ConfiguracionCine config;

    public ReembolsoPorAnticipacion(ConfiguracionCine config) {
        this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
    }

    @Override
    public boolean permiteCancelar(Duration tiempoRestante) {
        return config.permiteCancelacion(tiempoRestante);
    }

    @Override
    public double calcularReembolso(double valorPagado, Duration tiempoRestante) {
        return redondear(valorPagado * config.calcularPorcentajeReembolso(tiempoRestante));
    }

    @Override
    public String getNombre() {
        return "Reembolso por anticipación (100 % / 80 % / no permitido)";
    }

    static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}