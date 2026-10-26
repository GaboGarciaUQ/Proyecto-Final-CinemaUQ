package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator;

// ConcreteDecorator: suma el IVA al valor del cobro envuelto.
public class RecargoIva extends CobroDecorator {

    private final double iva;

    public RecargoIva(Cobro envuelto, double iva) {
        super(envuelto);
        if (iva < 0) {
            throw new IllegalArgumentException("El IVA no puede ser negativo");
        }
        this.iva = iva;
    }

    @Override
    public double getValor() {
        double base = envuelto.getValor();
        return base + (base * iva);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " + IVA " + (int) (iva * 100) + "%";
    }
}