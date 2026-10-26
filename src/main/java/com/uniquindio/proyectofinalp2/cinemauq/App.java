package com.uniquindio.proyectofinalp2.cinemauq;

import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Navegador.iniciar(stage);
        Navegador.ir(Vista.LOGIN);
    }

    public static void main(String[] args) {
        launch();
    }
}