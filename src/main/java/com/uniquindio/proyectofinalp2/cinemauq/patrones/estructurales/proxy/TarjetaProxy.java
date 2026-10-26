package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.proxy;

import java.util.List;
import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AccesoDenegadoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Movimiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Usuario;

// Proxy de protección: decide quién puede hacer qué antes de llegar a la tarjeta real.
public class TarjetaProxy implements Tarjeta {

    private final Tarjeta real;
    private final Usuario actor;

    public TarjetaProxy(Tarjeta real, Usuario actor) {
        this.real = Objects.requireNonNull(real, "La tarjeta es obligatoria");
        this.actor = Objects.requireNonNull(actor, "El usuario es obligatorio");
    }

    @Override
    public Cliente getTitular() {
        return real.getTitular();
    }

    @Override
    public double getSaldo() {
        exigirTitularOAdmin("consultar el saldo");
        return real.getSaldo();
    }

    @Override
    public boolean isActiva() {
        exigirTitularOAdmin("consultar el estado");
        return real.isActiva();
    }

    @Override
    public List<Movimiento> getMovimientos() {
        exigirTitularOAdmin("consultar los movimientos");
        return real.getMovimientos();
    }

    @Override
    public void pagar(double monto, String referencia) {
        exigirTitular("pagar");
        real.pagar(monto, referencia);
    }

    @Override
    public void recargar(double monto) {
        exigirAdmin("recargar");
        real.recargar(monto);
    }

    @Override
    public void activar() {
        exigirAdmin("activar la tarjeta");
        real.activar();
    }

    @Override
    public void desactivar() {
        exigirAdmin("desactivar la tarjeta");
        real.desactivar();
    }

    private boolean esTitular() {
        return actor.getId().equals(real.getTitular().getId());
    }

    private void exigirTitular(String operacion) {
        if (!esTitular()) {
            denegar(operacion);
        }
    }

    private void exigirAdmin(String operacion) {
        if (!actor.esAdministrador()) {
            denegar(operacion);
        }
    }

    private void exigirTitularOAdmin(String operacion) {
        if (!esTitular() && !actor.esAdministrador()) {
            denegar(operacion);
        }
    }

    private void denegar(String operacion) {
        throw new AccesoDenegadoException(actor.getNombre(), operacion);
    }
}