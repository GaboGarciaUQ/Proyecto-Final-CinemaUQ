package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer;

// Quien quiera enterarse de lo que pasa en el cine implementa esta interfaz.
// Un solo método: recibe el evento y hace lo que le corresponda (guardarlo, mostrarlo, enviarlo...).
// Quien publica no sabe qué hace cada observador ni cuántos hay.
public interface ObservadorCine {

    void notificar(EventoCine evento);
}