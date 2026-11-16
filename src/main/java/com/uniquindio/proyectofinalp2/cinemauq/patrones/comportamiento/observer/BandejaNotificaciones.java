package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;

// Observador concreto: guarda cada evento en la bandeja de su cliente.
// La interfaz gráfica podrá leer de aquí para mostrar "Mis notificaciones".
public class BandejaNotificaciones implements ObservadorCine {

    private final Map<String, List<EventoCine>> porCliente = new HashMap<>();

    @Override
    public void notificar(EventoCine evento) {
        porCliente.computeIfAbsent(evento.getCliente().getId(), id -> new ArrayList<>())
                .add(evento);
    }

    // Las más recientes primero. La lista devuelta no se puede modificar desde fuera.
    public List<EventoCine> getNotificaciones(Cliente cliente) {
        List<EventoCine> eventos = new ArrayList<>(porCliente.getOrDefault(cliente.getId(), List.of()));
        Collections.reverse(eventos);
        return Collections.unmodifiableList(eventos);
    }

    public int cantidad(Cliente cliente) {
        return porCliente.getOrDefault(cliente.getId(), List.of()).size();
    }
}