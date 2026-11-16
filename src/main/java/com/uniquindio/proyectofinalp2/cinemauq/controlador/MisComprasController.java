package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class MisComprasController {

    @FXML private TableView<Compra> tblCompras;
    @FXML private Label lblDetalle;
    @FXML private Button btnCancelar;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();

    @FXML
    private void initialize() {
        Ui.columnas(tblCompras,
                Ui.<Compra>col("Compra", Compra::getId),
                Ui.<Compra>col("Película", c -> c.getFuncion().getPelicula().getTitulo()),
                Ui.<Compra>col("Función", c -> Formato.fechaHora(c.getFuncion().getFechaHora())),
                Ui.<Compra>col("Estado", c -> c.getEstado().name()),
                Ui.<Compra>col("Total", c -> Formato.dinero(c.getTotal())));
        tblCompras.setPlaceholder(new Label("Aun no tienes compras"));
        List<Compra> compras = servicio.getComprasDe(sesion.getCliente()).stream()
                .sorted(Comparator.comparing(Compra::getFecha).reversed())
                .collect(Collectors.toList());
        Ui.items(tblCompras, compras);
        tblCompras.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> mostrar(c));
    }

    private void mostrar(Compra c) {
        btnCancelar.setDisable(c == null || c.getEstado() != EstadoCompra.PAGADA);
        if (c == null) {
            lblDetalle.setText("");
            return;
        }
        String asientos = c.getEntradas().stream()
                .map(Entrada::getAsiento)
                .map(a -> "" + a.getFila() + a.getNumero())
                .collect(Collectors.joining(", "));
        StringBuilder sb = new StringBuilder();
        sb.append(c.getFuncion().getPelicula().getTitulo()).append(" · ")
                .append(c.getFuncion().getSala().getNombre()).append('\n')
                .append(Formato.fechaHora(c.getFuncion().getFechaHora())).append('\n')
                .append("Asientos: ").append(asientos).append('\n');
        if (c.getCombo() != null) {
            sb.append("Combo: ").append(c.getCombo().getNombre()).append('\n');
        }
        if (!c.getCanjes().isEmpty()) {
            sb.append("Canjes: ").append(c.getCanjes().stream().map(Object::toString)
                    .collect(Collectors.joining(", "))).append('\n');
        }
        sb.append('\n').append(c.getDescripcionCobro()).append('\n')
                .append("Total: ").append(Formato.dinero(c.getTotal()));
        if (c.getMontoReembolsado() > 0) {
            sb.append("\nReembolsado: ").append(Formato.dinero(c.getMontoReembolsado()));
        }
        lblDetalle.setText(sb.toString());
    }

    @FXML
    private void cancelar() {
        Compra c = tblCompras.getSelectionModel().getSelectedItem();
        if (c == null || !Ui.confirmar("¿Cancelar la compra " + c.getId() + "?")) {
            return;
        }
        double[] monto = new double[1];
        if (Ui.intentar(() -> monto[0] = servicio.cancelarCompra(sesion.getCliente(), c))) {
            Ui.info("Compra cancelada. Se devolvieron " + Formato.dinero(monto[0]) + " a tu tarjeta.");
            Navegador.ir(Vista.MIS_COMPRAS);
        }
    }
}