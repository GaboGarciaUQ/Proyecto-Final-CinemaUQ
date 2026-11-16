package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.ConsumoPuntos;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.PuntosAcumulados;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class PuntosController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblRegla;
    @FXML private Label lblVigentes;
    @FXML private Label lblRedimidos;
    @FXML private Label lblEquivalencias;
    @FXML private TableView<PuntosAcumulados> tblLotes;
    @FXML private TableView<ConsumoPuntos> tblConsumos;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();
    private final Cliente cliente = sesion.getCliente();

    @FXML
    private void initialize() {
        ConfiguracionCine cfg = servicio.getConfig();
        lblRegla.setText("Ganas " + cfg.getPuntosPorBloque() + " puntos por cada " + Formato.dinero(cfg.getValorPorBloque())
                + " gastados. Vencen a los " + cfg.getVigenciaPuntosAnios() + " año(s); se usan primero los más próximos a vencer.");
        lblVigentes.setText(String.valueOf(servicio.getPuntosVigentes(cliente)));
        lblRedimidos.setText(String.valueOf(servicio.getPuntosRedimidos(cliente)));
        lblEquivalencias.setText("Entrada: " + cfg.getPuntosParaEntrada() + " pts\n"
                + "Producto: " + cfg.getPuntosParaProducto() + " pts\n"
                + "Combo: " + cfg.getPuntosParaCombo() + " pts");

        Ui.columnas(tblLotes,
                Ui.<PuntosAcumulados>col("Compra", PuntosAcumulados::getIdCompra),
                Ui.<PuntosAcumulados>col("Puntos", p -> String.valueOf(p.getPuntos())),
                Ui.<PuntosAcumulados>col("Obtenidos", p -> p.getFechaObtencion().format(FECHA)),
                Ui.<PuntosAcumulados>col("Vencen", p -> p.getFechaVencimiento().format(FECHA)),
                Ui.<PuntosAcumulados>col("Restantes", p -> String.valueOf(servicio.getRestantes(cliente, p))),
                Ui.<PuntosAcumulados>col("Estado", this::estado));
        Ui.columnas(tblConsumos,
                Ui.<ConsumoPuntos>col("Fecha", c -> c.getFecha().format(FECHA)),
                Ui.<ConsumoPuntos>col("Puntos", c -> String.valueOf(c.getPuntos())),
                Ui.<ConsumoPuntos>col("Compra", ConsumoPuntos::getIdCompraCanje),
                Ui.<ConsumoPuntos>col("Lote", ConsumoPuntos::getIdLote));
        tblLotes.setPlaceholder(new Label("Aun no has acumulado puntos"));
        tblConsumos.setPlaceholder(new Label("Aun no has usado puntos"));
        Ui.items(tblLotes, servicio.getHistorialPuntos(cliente));
        Ui.items(tblConsumos, servicio.getHistorialConsumos(cliente));
    }

    private String estado(PuntosAcumulados p) {
        if (servicio.getRestantes(cliente, p) == 0) {
            return "Usado";
        }
        return p.estaVigente(LocalDate.now()) ? "Vigente" : "Vencido";
    }
}