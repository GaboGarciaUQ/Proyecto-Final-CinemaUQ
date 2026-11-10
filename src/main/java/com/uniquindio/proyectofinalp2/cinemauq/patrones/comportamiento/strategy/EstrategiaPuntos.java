package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy;

/*
 PATRÓN COMPORTAMENTAL: STRATEGY.
 Strategy (interfaz): contrato común de todas las políticas de acumulación de puntos CinemaUQ.
 Cada política encapsula una fórmula distinta e intercambiable.
*/
public interface EstrategiaPuntos {

    /* Puntos que gana el cliente por un valor pagado (nunca negativo). */
    int calcular(double valorPagado);

    /* Nombre legible de la política (para mostrar en pantalla o en bitácoras). */
    String getNombre();
}
