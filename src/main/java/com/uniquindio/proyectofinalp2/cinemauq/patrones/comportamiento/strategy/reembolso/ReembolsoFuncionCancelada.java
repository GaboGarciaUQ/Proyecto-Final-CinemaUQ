package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso;

import java.time.Duration;
import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

public class ReembolsoFuncionCancelada implements PoliticaReembolso {

    private final ConfiguracionCine config;

    public ReembolsoFuncionCancelada(ConfiguracionCine config) {
        this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
    }

    @Override
    public boolean permiteCancelar(Duration tiempoRestante) {
        return true;
    }

    @Override
    public double calcularReembolso(double valorPagado, Duration tiempoRestante) {
        return ReembolsoPorAnticipacion.redondear(
                valorPagado * config.getPorcentajeReembolsoFuncionCancelada());
    }

    @Override
    public String getNombre() {
        return "Reembolso por función cancelada por el cine (100 %)";
    }
}