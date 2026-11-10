package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy;

/*
 ConcreteStrategy: la compra no acumula puntos (por ejemplo, cuando la acumulación está suspendida).
*/
public class SinPuntos implements EstrategiaPuntos {

    @Override
    public int calcular(double valorPagado) {
        return 0;
    }

    @Override
    public String getNombre() {
        return "Sin puntos";
    }
}
