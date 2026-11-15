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

    // Devuelve a la tarjeta el dinero de una compra cancelada. La referencia es el id de la compra.
    void reembolsar(double monto, String referencia);

    void activar();

    void desactivar();

    List<Movimiento> getMovimientos();
}