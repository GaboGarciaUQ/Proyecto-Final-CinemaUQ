package com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos;

import java.time.LocalDateTime;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;

// Pedido de recarga de un cliente. Solo el administrador lo resuelve, y una sola vez.
public class SolicitudRecarga {

    private final String id;
    private final Cliente cliente;
    private final double monto;
    private final LocalDateTime fechaSolicitud;
    private EstadoSolicitudRecarga estado = EstadoSolicitudRecarga.PENDIENTE;
    private LocalDateTime fechaResolucion;
    private String motivoRechazo;

    public SolicitudRecarga(String id, Cliente cliente, double monto, LocalDateTime fechaSolicitud) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La solicitud necesita un id");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("La solicitud necesita un cliente");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        this.id = id;
        this.cliente = cliente;
        this.monto = monto;
        this.fechaSolicitud = fechaSolicitud;
    }

    public void aprobar(LocalDateTime fecha) {
        exigirPendiente();
        estado = EstadoSolicitudRecarga.APROBADA;
        fechaResolucion = fecha;
    }

    public void rechazar(String motivo, LocalDateTime fecha) {
        exigirPendiente();
        estado = EstadoSolicitudRecarga.RECHAZADA;
        fechaResolucion = fecha;
        motivoRechazo = motivo;
    }

    private void exigirPendiente() {
        if (estado != EstadoSolicitudRecarga.PENDIENTE) {
            throw new EstadoInvalidoException("La solicitud ya fue " + estado.name().toLowerCase());
        }
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public EstadoSolicitudRecarga getEstado() {
        return estado;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    @Override
    public String toString() {
        return id + " " + cliente.getNombre() + " $" + monto + " " + estado;
    }
}