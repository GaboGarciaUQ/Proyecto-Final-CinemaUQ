package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminController {

    @FXML private Label lblNombre;

    private final Sesion sesion = Sesion.getInstancia();

    @FXML
    private void initialize() {
        lblNombre.setText(sesion.getAdmin().getNombre());
    }

    @FXML
    private void salir() {
        sesion.cerrarSesion();
        Navegador.ir(Vista.LOGIN);
    }
}