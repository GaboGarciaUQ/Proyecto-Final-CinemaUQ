package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy;

import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

/*
 ConcreteStrategy: regla estándar del grupo (por defecto, 1 punto por cada $1.000 pagados).
 El valor del bloque y los puntos por bloque los define ConfiguracionCine (Singleton inyectado).
*/
public class PuntosBase implements EstrategiaPuntos {

    private final ConfiguracionCine config;

    public PuntosBase(ConfiguracionCine config) {
        this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
    }

    @Override
    public int calcular(double valorPagado) {
        return config.calcularPuntos(valorPagado);
    }

    @Override
    public String getNombre() {
        return "Puntos base";
    }
}
