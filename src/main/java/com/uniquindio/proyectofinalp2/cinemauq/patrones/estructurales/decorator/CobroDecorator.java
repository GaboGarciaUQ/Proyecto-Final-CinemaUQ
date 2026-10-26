package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator;

import java.util.Objects;

// Decorator: implementa Cobro y además envuelve a otro Cobro.
public abstract class CobroDecorator implements Cobro {

    protected final Cobro envuelto;

    protected CobroDecorator(Cobro envuelto) {
        this.envuelto = Objects.requireNonNull(envuelto, "El cobro a envolver es obligatorio");
    }

    @Override
    public String getDescripcion() {
        return envuelto.getDescripcion();
    }
}