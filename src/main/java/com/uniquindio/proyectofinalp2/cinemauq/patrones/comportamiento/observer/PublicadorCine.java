package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// El "sujeto" del Observer: guarda quién quiere enterarse y le reparte cada evento.
// Solo conoce la interfaz ObservadorCine, nunca las clases concretas.
public class PublicadorCine {

    private final List<ObservadorCine> observadores = new ArrayList<>();

    public void registrar(ObservadorCine observador) {
        Objects.requireNonNull(observador, "El observador es obligatorio");
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void quitar(ObservadorCine observador) {
        observadores.remove(observador);
    }

    public void publicar(EventoCine evento) {
        Objects.requireNonNull(evento, "El evento es obligatorio");
        // Se recorre una copia: un observador podría registrarse o quitarse mientras recibe el aviso.
        for (ObservadorCine observador : new ArrayList<>(observadores)) {
            try {
                observador.notificar(evento);
            } catch (RuntimeException e) {
                // Un observador que falla no debe impedir que los demás se enteren,
                // ni deshacer una compra que ya se hizo.
                System.err.println("Observador falló al recibir " + evento.getTipo() + ": " + e.getMessage());
            }
        }
    }

    public int cantidadObservadores() {
        return observadores.size();
    }
}