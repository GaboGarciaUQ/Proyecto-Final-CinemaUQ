package com.uniquindio.proyectofinalp2.cinemauq.util;

import java.time.format.DateTimeParseException;
import java.util.function.Function;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.util.StringConverter;

// Utilidades de UI: columnas, diálogos y manejo uniforme de errores.
public final class Ui {

    private static final String CSS = "/com/uniquindio/proyectofinalp2/cinemauq/css/cinemauq.css";

    private Ui() {
    }

    public static <T> TableColumn<T, String> col(String titulo, Function<T, String> valor) {
        TableColumn<T, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        return c;
    }

    @SafeVarargs
    public static <T> void columnas(TableView<T> tabla, TableColumn<T, ?>... columnas) {
        tabla.getColumns().setAll(columnas);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public static <T> void items(TableView<T> tabla, java.util.List<T> datos) {
        tabla.setItems(FXCollections.observableArrayList(datos));
        tabla.refresh();
    }

    public static <T> void texto(ComboBox<T> combo, Function<T, String> f) {
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T t) {
                return t == null ? "" : f.apply(t);
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });
    }

    // Ejecuta una acción mostrando cualquier error de forma amigable. Devuelve true si salió bien.
    public static boolean intentar(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (RuntimeException e) {
            error(mensaje(e));
            return false;
        }
    }

    public static String mensaje(Throwable e) {
        if (e instanceof NumberFormatException) {
            return "Escribe un número válido";
        }
        if (e instanceof DateTimeParseException) {
            return "Fecha u hora inválida (usa HH:mm)";
        }
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    public static double numero(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje).showAndWait();
    }

    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "CinemaUQ", mensaje).showAndWait();
    }

    public static boolean confirmar(String mensaje) {
        return mostrar(Alert.AlertType.CONFIRMATION, "Confirmar", mensaje)
                .showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    public static String pedirTexto(String mensaje) {
        TextInputDialog d = new TextInputDialog();
        d.setHeaderText(null);
        d.setTitle("CinemaUQ");
        d.setContentText(mensaje);
        d.getDialogPane().getStylesheets().add(Ui.class.getResource(CSS).toExternalForm());
        return d.showAndWait().orElse(null);
    }

    private static Alert mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo, mensaje);
        a.setHeaderText(null);
        a.setTitle(titulo);
        a.getDialogPane().getStylesheets().add(Ui.class.getResource(CSS).toExternalForm());
        return a;
    }
}