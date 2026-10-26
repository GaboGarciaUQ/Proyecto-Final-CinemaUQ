package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.prototype;

/*
 PATRÓN CREACIONAL: PROTOTYPE.
 Un objeto que sabe copiarse a sí mismo.
 Hoy lo implementa Funcion. Su uso desde la interfaz queda para la
 gestión de funciones del administrador (entrega final).
*/
public interface Prototipo<T> {

    T clonar();
}