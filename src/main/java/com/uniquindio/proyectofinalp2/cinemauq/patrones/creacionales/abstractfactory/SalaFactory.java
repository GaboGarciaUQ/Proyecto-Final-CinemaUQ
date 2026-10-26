package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Tarifa;

/*
 PATRÓN CREACIONAL: ABSTRACT FACTORY (fábrica abstracta).
 Cada formato de sala crea su familia: Sala + Tarifa.
*/
public interface SalaFactory {

    Sala crearSala(String id, String nombre);

    Tarifa crearTarifa();
}