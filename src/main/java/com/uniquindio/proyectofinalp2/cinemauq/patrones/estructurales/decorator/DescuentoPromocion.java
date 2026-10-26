package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator;

import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;

// ConcreteDecorator: aplica una promoción al valor del cobro envuelto.
public class DescuentoPromocion extends CobroDecorator {

    private final Promocion promocion;

    public DescuentoPromocion(Cobro envuelto, Promocion promocion) {
        super(envuelto);
        this.promocion = Objects.requireNonNull(promocion, "La promoción es obligatoria");
    }

    @Override
    public double getValor() {
        return promocion.aplicar(envuelto.getValor());
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - " + promocion.getNombre();
    }
}