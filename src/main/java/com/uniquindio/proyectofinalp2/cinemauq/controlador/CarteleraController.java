package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class CarteleraController {

    private static final int VARIANTES_AFICHE = 6;

    @FXML private Label lblResumen;
    @FXML private FlowPane contenedorPeliculas;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();

    @FXML
    private void initialize() {
        List<Pelicula> peliculas = servicio.getPeliculasEnCartelera();
        if (peliculas.isEmpty()) {
            lblResumen.setText("No hay funciones programadas por ahora.");
            return;
        }
        lblResumen.setText("Elige un horario para seleccionar tus asientos.");
        for (int i = 0; i < peliculas.size(); i++) {
            contenedorPeliculas.getChildren().add(crearTarjeta(peliculas.get(i), i));
        }
    }

    private VBox crearTarjeta(Pelicula pelicula, int indice) {
        Label tituloAfiche = new Label(pelicula.getTitulo());
        tituloAfiche.setWrapText(true);
        tituloAfiche.setTextAlignment(TextAlignment.CENTER);
        tituloAfiche.getStyleClass().add("afiche-titulo");

        StackPane afiche = new StackPane(tituloAfiche);
        afiche.getStyleClass().addAll("afiche", "afiche-" + (indice % VARIANTES_AFICHE));
        afiche.setPadding(new Insets(16));
        afiche.setPrefHeight(200);

        Label duracion = new Label(Formato.duracion(pelicula.getDuracionMinutos()));
        duracion.getStyleClass().add("texto-suave");

        VBox tarjeta = new VBox(10, afiche, duracion);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPrefWidth(250);
        tarjeta.setMaxWidth(250);

        for (Map.Entry<LocalDate, List<Funcion>> dia : agruparPorDia(servicio.getFuncionesDe(pelicula)).entrySet()) {
            Label etiquetaDia = new Label(Formato.dia(dia.getKey()));
            etiquetaDia.getStyleClass().add("etiqueta-campo");

            FlowPane horarios = new FlowPane(8, 8);
            for (Funcion funcion : dia.getValue()) {
                horarios.getChildren().add(crearHorario(funcion));
            }
            tarjeta.getChildren().addAll(etiquetaDia, horarios);
        }
        return tarjeta;
    }

    private Map<LocalDate, List<Funcion>> agruparPorDia(List<Funcion> funciones) {
        Map<LocalDate, List<Funcion>> porDia = new LinkedHashMap<>();
        for (Funcion f : funciones) {
            porDia.computeIfAbsent(f.getFechaHora().toLocalDate(), d -> new ArrayList<>()).add(f);
        }
        return porDia;
    }

    private Button crearHorario(Funcion funcion) {
        int libres = funcion.getCapacidad() - funcion.getCantidadOcupados();
        String formato = funcion.getSala().getTarifa().getNombre();

        Button horario = new Button(Formato.hora(funcion.getFechaHora()) + " · " + formato);
        horario.getStyleClass().add("chip");
        horario.setTooltip(new Tooltip(funcion.getSala().getNombre() + " · " + libres
                + " asientos libres · desde " + Formato.dinero(funcion.getPrecioFinal())));
        if (libres == 0) {
            horario.setText("Agotada");
            horario.setDisable(true);
        }
        horario.setOnAction(e -> elegirFuncion(funcion));
        return horario;
    }

    private void elegirFuncion(Funcion funcion) {
        sesion.iniciarCompra(funcion);
        Navegador.ir(Vista.ASIENTOS);
    }
}