package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BarraSuperiorController {

    @FXML private Label lblNombre;
    @FXML private Label lblSaldo;

    private final Sesion sesion = Sesion.getInstancia();

    @FXML
    private void initialize() {
        lblNombre.setText(sesion.getCliente().getNombre());
        actualizarSaldo();
    }

    public void actualizarSaldo() {
        Cliente cliente = sesion.getCliente();
        double saldo = sesion.getServicio().getTarjeta(cliente).getSaldo();
        lblSaldo.setText("Saldo " + Formato.dinero(saldo));
    }

    @FXML
    private void irACartelera() {
        sesion.limpiarCompra();
        Navegador.ir(Vista.CARTELERA);
    }

    @FXML
    private void salir() {
        sesion.cerrarSesion();
        Navegador.ir(Vista.LOGIN);
    }
}