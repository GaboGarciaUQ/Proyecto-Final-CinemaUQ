package com.uniquindio.proyectofinalp2.cinemauq.util;

public enum Vista {
    LOGIN("LoginView.fxml"),
    CARTELERA("CarteleraView.fxml"),
    ASIENTOS("AsientosView.fxml"),
    CONFITERIA("ConfiteriaView.fxml"),
    COMPRA("CompraView.fxml");

    private final String archivo;

    Vista(String archivo) {
        this.archivo = archivo;
    }

    public String getArchivo() {
        return archivo;
    }
}