package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ConfiteriaController {

    private static final int MAX_POR_ITEM = 10;

    @FXML private Label lblPelicula;
    @FXML private Label lblFuncion;
    @FXML private Label lblEntradas;
    @FXML private Label lblConfiteria;
    @FXML private Label lblTotal;
    @FXML private Label lblPuntos;
    @FXML private VBox listaCombos;
    @FXML private VBox listaProductos;
    @FXML private VBox lineasResumen;
    @FXML private Button btnContinuar;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();
    private final Funcion funcion = sesion.getFuncion();
    private final Map<Comprable, Integer> cantidades = new LinkedHashMap<>();
    private final Map<Comprable, Label> etiquetasCantidad = new LinkedHashMap<>();
    private double totalEntradas;

    @FXML
    private void initialize() {
        lblPelicula.setText(funcion.getPelicula().getTitulo());
        lblFuncion.setText(Formato.fechaHora(funcion.getFechaHora()) + " · " + funcion.getSala().getNombre());

        for (Asiento a : sesion.getAsientos()) {
            totalEntradas += servicio.precioEntrada(funcion, a);
        }
        lblEntradas.setText(Formato.dinero(totalEntradas));

        for (Comprable previo : sesion.getConfiteria()) {
            cantidades.merge(previo, 1, Integer::sum);
        }

        servicio.getCombos().forEach(c -> listaCombos.getChildren().add(crearFila(c, descripcionCombo(c))));
        servicio.getProductos().forEach(p -> listaProductos.getChildren().add(crearFila(p, "")));
        actualizarResumen();
    }

    private HBox crearFila(Comprable item, String detalle) {
        Label nombre = new Label(item.getNombre());
        nombre.getStyleClass().add("subtitulo");

        VBox textos = new VBox(2, nombre);
        if (!detalle.isEmpty()) {
            Label descripcion = new Label(detalle);
            descripcion.getStyleClass().add("texto-suave");
            descripcion.setWrapText(true);
            textos.getChildren().add(descripcion);
        }
        Label precio = new Label(Formato.dinero(item.getPrecio()));
        precio.getStyleClass().add("precio");
        textos.getChildren().add(precio);
        HBox.setHgrow(textos, Priority.ALWAYS);

        Label cantidad = new Label(String.valueOf(cantidades.getOrDefault(item, 0)));
        cantidad.setMinWidth(28);
        cantidad.setAlignment(Pos.CENTER);
        etiquetasCantidad.put(item, cantidad);

        Button menos = new Button("−");
        menos.getStyleClass().add("boton-cantidad");
        menos.setOnAction(e -> cambiar(item, -1));
        Button mas = new Button("+");
        mas.getStyleClass().add("boton-cantidad");
        mas.setOnAction(e -> cambiar(item, 1));

        HBox fila = new HBox(14, textos, menos, cantidad, mas);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("panel");
        fila.setPadding(new Insets(12, 16, 12, 16));
        return fila;
    }

    private String descripcionCombo(Combo combo) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Comprable c : combo.getComponentes()) {
            conteo.merge(c.getNombre(), 1, Integer::sum);
        }
        String contenido = conteo.entrySet().stream()
                .map(e -> e.getValue() > 1 ? e.getValue() + " × " + e.getKey() : e.getKey())
                .collect(Collectors.joining(", "));
        double suma = combo.getComponentes().stream().mapToDouble(Comprable::getPrecio).sum();
        double ahorro = suma - combo.getPrecio();
        return ahorro > 0 ? contenido + " · Ahorras " + Formato.dinero(ahorro) : contenido;
    }

    private void cambiar(Comprable item, int delta) {
        int nueva = Math.max(0, Math.min(MAX_POR_ITEM, cantidades.getOrDefault(item, 0) + delta));
        if (nueva == 0) {
            cantidades.remove(item);
        } else {
            cantidades.put(item, nueva);
        }
        etiquetasCantidad.get(item).setText(String.valueOf(nueva));
        actualizarResumen();
    }

    private void actualizarResumen() {
        lineasResumen.getChildren().clear();
        double totalConfiteria = 0;
        for (Map.Entry<Comprable, Integer> e : cantidades.entrySet()) {
            double subtotal = e.getKey().getPrecio() * e.getValue();
            totalConfiteria += subtotal;

            Label nombre = new Label(e.getValue() + " × " + e.getKey().getNombre());
            nombre.setWrapText(true);
            nombre.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nombre, Priority.ALWAYS);
            Label valor = new Label(Formato.dinero(subtotal));
            valor.getStyleClass().add("texto-suave");
            lineasResumen.getChildren().add(new HBox(8, nombre, valor));
        }
        if (cantidades.isEmpty()) {
            Label vacio = new Label("Sin productos agregados");
            vacio.getStyleClass().add("texto-suave");
            lineasResumen.getChildren().add(vacio);
        }
        double total = totalEntradas + totalConfiteria;
        lblConfiteria.setText(Formato.dinero(totalConfiteria));
        lblTotal.setText(Formato.dinero(total));
        lblPuntos.setText("Ganarás " + servicio.puntosPorCompra(total) + " pts con esta compra");
        btnContinuar.setDisable(sesion.getAsientos().isEmpty());
    }

    private void guardarSeleccion() {
        List<Comprable> items = new ArrayList<>();
        cantidades.forEach((item, cantidad) -> {
            for (int i = 0; i < cantidad; i++) {
                items.add(item);
            }
        });
        sesion.setConfiteria(items);
    }

    @FXML
    private void continuar() {
        guardarSeleccion();
        Navegador.ir(Vista.COMPRA);
    }

    @FXML
    private void volver() {
        guardarSeleccion();
        Navegador.ir(Vista.ASIENTOS);
    }
}