package com.uniquindio.proyectofinalp2.cinemauq.servicio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AccesoDenegadoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.EstadoSolicitudRecarga;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.SolicitudRecarga;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Usuario;

/*
 El cliente pide una recarga y el administrador la aprueba o la rechaza.
 Al aprobar, la recarga real la hace CineServicio.recargarTarjeta, que pasa por el Proxy
 de la tarjeta, deja el movimiento registrado y avisa al cliente (Observer).
 Regla del grupo: un cliente solo puede tener una solicitud pendiente a la vez.
*/
public class ServicioSolicitudesRecarga {

    private final CineServicio cine;
    private final List<SolicitudRecarga> solicitudes = new ArrayList<>();
    private int consecutivo;

    ServicioSolicitudesRecarga(CineServicio cine) {
        this.cine = Objects.requireNonNull(cine, "El servicio es obligatorio");
    }

    public SolicitudRecarga solicitar(Cliente cliente, double monto) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        boolean tienePendiente = solicitudes.stream()
                .anyMatch(s -> s.getCliente().getId().equals(cliente.getId())
                        && s.getEstado() == EstadoSolicitudRecarga.PENDIENTE);
        if (tienePendiente) {
            throw new EstadoInvalidoException("Ya tienes una solicitud de recarga pendiente");
        }
        SolicitudRecarga solicitud = new SolicitudRecarga("SR" + (++consecutivo), cliente, monto,
                LocalDateTime.now());
        solicitudes.add(solicitud);
        return solicitud;
    }

    // Si la recarga falla, la solicitud sigue pendiente. Una solicitud resuelta no se vuelve a ejecutar.
    public void aprobar(Usuario actor, SolicitudRecarga solicitud) {
        exigirAdministrador(actor, "aprobar una recarga");
        exigirPendiente(solicitud);
        cine.recargarTarjeta(actor, solicitud.getCliente(), solicitud.getMonto());
        solicitud.aprobar(LocalDateTime.now());
    }

    public void rechazar(Usuario actor, SolicitudRecarga solicitud, String motivo) {
        exigirAdministrador(actor, "rechazar una recarga");
        exigirPendiente(solicitud);
        solicitud.rechazar(motivo == null || motivo.isBlank() ? "Sin motivo" : motivo.trim(),
                LocalDateTime.now());
    }

    public List<SolicitudRecarga> getPendientes(Usuario actor) {
        exigirAdministrador(actor, "consultar las solicitudes de recarga");
        return solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitudRecarga.PENDIENTE)
                .collect(Collectors.toList());
    }

    public List<SolicitudRecarga> getTodas(Usuario actor) {
        exigirAdministrador(actor, "consultar las solicitudes de recarga");
        return Collections.unmodifiableList(solicitudes);
    }

    public List<SolicitudRecarga> getDe(Cliente cliente) {
        return solicitudes.stream()
                .filter(s -> s.getCliente().getId().equals(cliente.getId()))
                .collect(Collectors.toList());
    }

    private void exigirPendiente(SolicitudRecarga solicitud) {
        if (solicitud == null || !solicitudes.contains(solicitud)) {
            throw new IllegalArgumentException("La solicitud no existe");
        }
        if (solicitud.getEstado() != EstadoSolicitudRecarga.PENDIENTE) {
            throw new EstadoInvalidoException("La solicitud ya fue " + solicitud.getEstado().name().toLowerCase());
        }
    }

    private void exigirAdministrador(Usuario actor, String accion) {
        if (actor == null || !actor.esAdministrador()) {
            throw new AccesoDenegadoException(actor == null ? "Anónimo" : actor.getNombre(), accion);
        }
    }
}