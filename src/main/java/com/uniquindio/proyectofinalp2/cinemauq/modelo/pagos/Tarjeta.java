package com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos;

import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;

// Subject del Proxy: lo que se puede hacer con una tarjeta.
public interface Tarjeta {

    Cliente getTitular();

    double getSaldo();

    boolean isActiva();

    void pagar(double monto, String referencia);

    void recargar(double monto);

    void activar();

    void desactivar();

    List<Movimiento> getMovimientos();
}