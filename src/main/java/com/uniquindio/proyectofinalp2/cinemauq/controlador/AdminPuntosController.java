package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AdminPuntosController {

    @FXML private TextField txtValor;
    @FXML private TextField txtPuntos;
    @FXML private TextField txtEntrada;
    @FXML private TextField txtProducto;
    @FXML private TextField txtCombo;
    @FXML private Label lblMensaje;

    private final CineServicio servicio = Sesion.getInstancia().getServicio();
    private final Administrador actor = Sesion.getInstancia().getAdmin();

    @FXML
    private void initialize() {
        ConfiguracionCine c = servicio.getConfig();
        txtValor.setText(String.valueOf(Math.round(c.getValorPorBloque())));
        txtPuntos.setText(String.valueOf(c.getPuntosPorBloque()));
        txtEntrada.setText(String.valueOf(c.getPuntosParaEntrada()));
        txtProducto.setText(String.valueOf(c.getPuntosParaProducto()));
        txtCombo.setText(String.valueOf(c.getPuntosParaCombo()));
    }

    @FXML
    private void guardarRegla() {
        if (Ui.intentar(() -> servicio.getAdministracion().configurarAcumulacion(actor,
                Ui.numero(txtValor.getText()), Integer.parseInt(txtPuntos.getText().trim())))) {
            lblMensaje.setText("Regla de acumulación guardada.");
        }
    }

    @FXML
    private void guardarEquivalencias() {
        if (Ui.intentar(() -> servicio.getAdministracion().configurarEquivalencias(actor,
                Integer.parseInt(txtEntrada.getText().trim()), Integer.parseInt(txtProducto.getText().trim()),
                Integer.parseInt(txtCombo.getText().trim())))) {
            lblMensaje.setText("Equivalencias guardadas.");
        }
    }
}