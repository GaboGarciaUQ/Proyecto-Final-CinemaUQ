package com.uniquindio.proyectofinalp2.cinemauq.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Navegador {

    private static final String RUTA_VISTAS = "/com/uniquindio/proyectofinalp2/cinemauq/vista/";
    private static final String RUTA_CSS = "/com/uniquindio/proyectofinalp2/cinemauq/css/cinemauq.css";

    private static Stage stage;
    private static Scene scene;

    private Navegador() {
    }

    public static void iniciar(Stage ventana) {
        stage = ventana;
        stage.setTitle("CinemaUQ");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
    }

    public static void ir(Vista vista) {
        try {
            URL fxml = Navegador.class.getResource(RUTA_VISTAS + vista.getArchivo());
            Parent raiz = new FXMLLoader(fxml).load();
            if (scene == null) {
                scene = new Scene(raiz, 1200, 760);
                aplicarEstilos(scene);
                stage.setScene(scene);
                stage.show();
            } else {
                scene.setRoot(raiz);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la vista " + vista, e);
        }
    }

    private static void aplicarEstilos(Scene escena) {
        URL css = Navegador.class.getResource(RUTA_CSS);
        if (css == null) {
            throw new IllegalStateException("No se encontró la hoja de estilos en " + RUTA_CSS);
        }
        escena.getStylesheets().add(css.toExternalForm());
    }
}