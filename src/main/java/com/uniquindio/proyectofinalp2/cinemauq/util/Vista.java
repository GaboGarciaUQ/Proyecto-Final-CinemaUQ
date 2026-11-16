package com.uniquindio.proyectofinalp2.cinemauq.util;

public enum Vista {
    LOGIN("LoginView.fxml"),
    CARTELERA("CarteleraView.fxml"),
    ASIENTOS("AsientosView.fxml"),
    CONFITERIA("ConfiteriaView.fxml"),
    COMPRA("CompraView.fxml"),
    MI_TARJETA("MiTarjetaView.fxml"),
    MIS_COMPRAS("MisComprasView.fxml"),
    PUNTOS("PuntosView.fxml"),
    ADMIN("AdminView.fxml");

    private final String archivo;

    Vista(String archivo) {
        this.archivo = archivo;
    }

    public String getArchivo() {
        return archivo;
    }
}