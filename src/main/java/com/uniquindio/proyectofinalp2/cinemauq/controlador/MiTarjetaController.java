package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Movimiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.SolicitudRecarga;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class MiTarjetaController {

    @FXML private Label lblSaldo;
    @FXML private Label lblEstado;
    @FXML private Label lblMensaje;
    @FXML private TextField txtMonto;
    @FXML private TableView<Movimiento> tblMovimientos;
    @FXML private TableView<SolicitudRecarga> tblSolicitudes;
    @FXML private ListView<String> lstNotificaciones;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();
    private final Cliente cliente = sesion.getCliente();

    @FXML
    private void initialize() {
        Ui.columnas(tblMovimientos,
                Ui.<Movimiento>col("Fecha", m -> Formato.fechaHora(m.getFecha())),
                Ui.<Movimiento>col("Tipo", m -> m.getTipo().name()),
                Ui.<Movimiento>col("Monto", m -> Formato.dinero(m.getMonto())),
                Ui.<Movimiento>col("Saldo", m -> Formato.dinero(m.getSaldoResultante())),
                Ui.<Movimiento>col("Referencia", m -> String.valueOf(m.getReferencia())));
        Ui.columnas(tblSolicitudes,
                Ui.<SolicitudRecarga>col("Fecha", s -> Formato.fechaHora(s.getFechaSolicitud())),
                Ui.<SolicitudRecarga>col("Monto", s -> Formato.dinero(s.getMonto())),
                Ui.<SolicitudRecarga>col("Estado", s -> s.getEstado().name()),
                Ui.<SolicitudRecarga>col("Motivo", s -> s.getMotivoRechazo() == null ? "" : s.getMotivoRechazo()));
        tblMovimientos.setPlaceholder(new Label("Sin movimientos"));
        tblSolicitudes.setPlaceholder(new Label("Sin solicitudes"));
        refrescar();
    }

    private void refrescar() {
        Tarjeta tarjeta = servicio.getTarjeta(cliente);
        lblSaldo.setText(Formato.dinero(tarjeta.getSaldo()));
        lblEstado.setText(tarjeta.isActiva() ? "Tarjeta activa" : "Tarjeta bloqueada");

        List<Movimiento> movimientos = new ArrayList<>(tarjeta.getMovimientos());
        Collections.reverse(movimientos);
        Ui.items(tblMovimientos, movimientos);

        List<SolicitudRecarga> solicitudes = new ArrayList<>(servicio.getSolicitudesRecarga().getDe(cliente));
        Collections.reverse(solicitudes);
        Ui.items(tblSolicitudes, solicitudes);

        List<String> avisos = servicio.getNotificaciones(cliente).stream()
                .map(e -> Formato.fechaHora(e.getFecha()) + " · " + e.getMensaje())
                .collect(Collectors.toList());
        Collections.reverse(avisos);
        lstNotificaciones.setItems(FXCollections.observableArrayList(avisos));
    }

    @FXML
    private void solicitar() {
        boolean ok = Ui.intentar(() -> servicio.getSolicitudesRecarga().solicitar(cliente, Ui.numero(txtMonto.getText())));
        if (ok) {
            txtMonto.clear();
            lblMensaje.setText("Solicitud enviada. Un administrador la revisará.");
            refrescar();
        }
    }
}