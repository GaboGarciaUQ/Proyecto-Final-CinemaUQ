package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.prototype;

/*
 PATRÓN CREACIONAL: PROTOTYPE.
 Un objeto que sabe copiarse a sí mismo.
*/
public interface Prototipo<T> {

    T clonar();
}