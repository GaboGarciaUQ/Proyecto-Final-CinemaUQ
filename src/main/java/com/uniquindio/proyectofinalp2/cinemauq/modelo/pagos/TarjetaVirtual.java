package com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;

// RealSubject: aplica las reglas del saldo; no sabe quién la usa.
public class TarjetaVirtual implements Tarjeta {

    private final String id;
    private final Cliente titular;
    private final List<Movimiento> movimientos = new ArrayList<>();
    private double saldo;
    private boolean activa = true;

    public TarjetaVirtual(String id, Cliente titular) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La tarjeta necesita un id");
        }
        if (titular == null) {
            throw new IllegalArgumentException("La tarjeta necesita un titular");
        }
        this.id = id;
        this.titular = titular;
    }

    @Override
    public void pagar(double monto, String referencia) {
        validarMonto(monto);
        if (!activa) {
            throw new IllegalStateException("La tarjeta está inactiva");
        }
        if (monto > saldo) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        saldo -= monto;
        registrar(TipoMovimiento.PAGO, monto, referencia);
    }

    @Override
    public void recargar(double monto) {
        validarMonto(monto);
        saldo += monto;
        registrar(TipoMovimiento.RECARGA, monto, "Recarga");
    }

    @Override
    public void activar() {
        activa = true;
    }

    @Override
    public void desactivar() {
        activa = false;
    }

    private void validarMonto(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    private void registrar(TipoMovimiento tipo, double monto, String referencia) {
        movimientos.add(new Movimiento(tipo, monto, saldo, LocalDateTime.now(), referencia));
    }

    public String getId() {
        return id;
    }

    @Override
    public Cliente getTitular() {
        return titular;
    }

    @Override
    public double getSaldo() {
        return saldo;
    }

    @Override
    public boolean isActiva() {
        return activa;
    }

    @Override
    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}