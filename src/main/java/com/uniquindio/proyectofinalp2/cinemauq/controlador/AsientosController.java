package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class AsientosController {

    private static final int MAX_ASIENTOS = 8;

    @FXML private Label lblEncabezado;
    @FXML private Label lblPelicula;
    @FXML private Label lblFuncion;
    @FXML private Label lblCantidad;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Label lblPuntos;
    @FXML private Label lblError;
    @FXML private Label lblVacio;
    @FXML private StackPane zonaMapa;
    @FXML private HBox leyenda;
    @FXML private FlowPane chipsSeleccionados;
    @FXML private Button btnContinuar;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();
    private final Funcion funcion = sesion.getFuncion();
    private final List<Asiento> seleccionados = new ArrayList<>();

    @FXML
    private void initialize() {
        lblEncabezado.setText(funcion.getSala().getNombre() + " · " + funcion.getSala().getTarifa().getNombre());
        lblPelicula.setText(funcion.getPelicula().getTitulo());
        lblFuncion.setText(Formato.fechaHora(funcion.getFechaHora()) + " · " + funcion.getSala().getNombre());
        zonaMapa.getChildren().add(construirMapa());
        construirLeyenda();
        actualizarResumen();
    }

    private GridPane construirMapa() {
        GridPane mapa = new GridPane();
        mapa.setHgap(6);
        mapa.setVgap(6);
        mapa.setAlignment(Pos.TOP_CENTER);
        mapa.setPadding(new Insets(8));

        TreeMap<Character, List<Asiento>> filas = new TreeMap<>();
        TreeSet<Integer> columnas = new TreeSet<>();
        for (Asiento a : funcion.getSala().getAsientos()) {
            filas.computeIfAbsent(a.getFila(), f -> new ArrayList<>()).add(a);
            columnas.add(a.getNumero());
        }

        int fila = 0;
        for (var entrada : filas.entrySet()) {
            Label letra = new Label(String.valueOf(entrada.getKey()));
            letra.getStyleClass().add("fila-letra");
            letra.setMinWidth(24);
            mapa.add(letra, 0, fila);
            for (Asiento a : entrada.getValue()) {
                mapa.add(crearBoton(a), a.getNumero(), fila);
            }
            fila++;
        }
        return mapa;
    }

    private Button crearBoton(Asiento asiento) {
        Button boton = new Button(asiento.esAccesible() ? "♿" : String.valueOf(asiento.getNumero()));
        boton.getStyleClass().add("asiento");
        boton.setMinSize(38, 38);
        boton.setPrefSize(38, 38);
        boton.setTooltip(new Tooltip(etiqueta(asiento) + " · " + Formato.dinero(servicio.precioEntrada(funcion, asiento))));

        if (!funcion.estaDisponible(asiento)) {
            boton.getStyleClass().add("asiento-ocupado");
            boton.setDisable(true);
            return boton;
        }
        boton.getStyleClass().add(asiento.esAccesible() ? "asiento-accesible" : "asiento-disponible");
        boton.setOnAction(e -> alternar(asiento, boton));
        if (sesion.getAsientos().contains(asiento)) {
            seleccionados.add(asiento);
            boton.getStyleClass().add("asiento-seleccionado");
        }
        return boton;
    }

    private void alternar(Asiento asiento, Button boton) {
        lblError.setText("");
        if (seleccionados.remove(asiento)) {
            boton.getStyleClass().remove("asiento-seleccionado");
        } else {
            if (seleccionados.size() >= MAX_ASIENTOS) {
                lblError.setText("Máximo " + MAX_ASIENTOS + " asientos por compra");
                return;
            }
            seleccionados.add(asiento);
            boton.getStyleClass().add("asiento-seleccionado");
        }
        actualizarResumen();
    }

    private void actualizarResumen() {
        chipsSeleccionados.getChildren().clear();
        double total = 0;
        for (Asiento a : seleccionados) {
            Label chip = new Label(etiqueta(a));
            chip.getStyleClass().add("insignia");
            chipsSeleccionados.getChildren().add(chip);
            total += servicio.precioEntrada(funcion, a);
        }
        boolean hay = !seleccionados.isEmpty();
        lblVacio.setVisible(!hay);
        lblVacio.setManaged(!hay);
        lblCantidad.setText(seleccionados.size() + (seleccionados.size() == 1 ? " entrada" : " entradas"));
        lblSubtotal.setText(Formato.dinero(total));
        lblTotal.setText(Formato.dinero(total));
        lblPuntos.setText(hay ? "Ganarás " + servicio.puntosPorCompra(total) + " pts con esta compra" : "");
        btnContinuar.setDisable(!hay);
    }

    private void construirLeyenda() {
        leyenda.getChildren().addAll(
                itemLeyenda("Disponible", "asiento-disponible"),
                itemLeyenda("Seleccionado", "asiento-seleccionado"),
                itemLeyenda("Ocupado", "asiento-ocupado"),
                itemLeyenda("Accesible", "asiento-accesible"));
    }

    private HBox itemLeyenda(String texto, String estilo) {
        Region cuadro = new Region();
        cuadro.getStyleClass().addAll("leyenda-cuadro", estilo);
        cuadro.setMinSize(16, 16);
        cuadro.setPrefSize(16, 16);
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("texto-suave");
        HBox item = new HBox(6, cuadro, etiqueta);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private String etiqueta(Asiento a) {
        return "" + a.getFila() + a.getNumero();
    }

    @FXML
    private void continuar() {
        if (seleccionados.isEmpty()) {
            lblError.setText("Elige al menos un asiento");
            return;
        }
        sesion.setAsientos(seleccionados);
        Navegador.ir(Vista.CONFITERIA);
    }

    @FXML
    private void volver() {
        sesion.limpiarCompra();
        Navegador.ir(Vista.CARTELERA);
    }
}