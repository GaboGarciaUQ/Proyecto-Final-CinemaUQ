package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy;

import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

/*
 ConcreteStrategy: campaña promocional; el cliente gana el doble de la regla estándar.
 Reutiliza la regla de ConfiguracionCine, así si el administrador la cambia, la campaña la sigue.
*/
public class PuntosDobles implements EstrategiaPuntos {

    private static final int MULTIPLICADOR = 2;

    private final ConfiguracionCine config;

    public PuntosDobles(ConfiguracionCine config) {
        this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
    }

    @Override
    public int calcular(double valorPagado) {
        return config.calcularPuntos(valorPagado) * MULTIPLICADOR;
    }

    @Override
    public String getNombre() {
        return "Puntos dobles";
    }
}
