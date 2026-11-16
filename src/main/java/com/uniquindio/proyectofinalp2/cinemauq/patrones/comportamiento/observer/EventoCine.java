package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer;

import java.time.LocalDateTime;
import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;

// Lo que se le avisa a los observadores. Es inmutable: nadie puede alterarlo después de publicado.
public final class EventoCine {

    public enum Tipo {
        COMPRA_PAGADA,
        COMPRA_REEMBOLSADA,
        FUNCION_CANCELADA,
        TARJETA_RECARGADA
    }

    private final Tipo tipo;
    private final Cliente cliente;
    private final String mensaje;
    private final LocalDateTime fecha;

    public EventoCine(Tipo tipo, Cliente cliente, String mensaje) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo de evento es obligatorio");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El evento necesita un mensaje");
        }
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return "[" + fecha + "] " + tipo + " - " + cliente.getNombre() + ": " + mensaje;
    }
}