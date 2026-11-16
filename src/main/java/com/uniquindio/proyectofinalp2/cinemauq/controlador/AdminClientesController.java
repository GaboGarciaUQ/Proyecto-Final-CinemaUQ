package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.List;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.SolicitudRecarga;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.ServicioAdministracion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminClientesController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtRecarga;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableView<SolicitudRecarga> tblSolicitudes;

    private final CineServicio servicio = Sesion.getInstancia().getServicio();
    private final ServicioAdministracion adm = servicio.getAdministracion();
    private final Administrador actor = Sesion.getInstancia().getAdmin();

    @FXML
    private void initialize() {
        Ui.columnas(tblClientes,
                Ui.<Cliente>col("Nombre", Cliente::getNombre),
                Ui.<Cliente>col("Correo", Cliente::getCorreo),
                Ui.<Cliente>col("Saldo", c -> Formato.dinero(tarjeta(c).getSaldo())),
                Ui.<Cliente>col("Tarjeta", c -> tarjeta(c).isActiva() ? "Activa" : "Bloqueada"),
                Ui.<Cliente>col("Puntos", c -> String.valueOf(servicio.getPuntosVigentes(c))));
        Ui.columnas(tblSolicitudes,
                Ui.<SolicitudRecarga>col("Cliente", s -> s.getCliente().getNombre()),
                Ui.<SolicitudRecarga>col("Monto", s -> Formato.dinero(s.getMonto())),
                Ui.<SolicitudRecarga>col("Fecha", s -> Formato.fechaHora(s.getFechaSolicitud())));
        refrescar();
    }

    private Tarjeta tarjeta(Cliente c) {
        return servicio.getTarjetaDe(actor, c);
    }

    private void refrescar() {
        List<Cliente> clientes = txtBuscar.getText() == null || txtBuscar.getText().isBlank()
                ? adm.getClientes(actor) : adm.buscarClientes(actor, txtBuscar.getText());
        Ui.items(tblClientes, clientes);
        Ui.items(tblSolicitudes, servicio.getSolicitudesRecarga().getPendientes(actor));
    }

    @FXML
    private void buscar() {
        refrescar();
    }

    @FXML
    private void recargar() {
        Cliente c = tblClientes.getSelectionModel().getSelectedItem();
        if (c != null && Ui.intentar(() -> servicio.recargarTarjeta(actor, c, Ui.numero(txtRecarga.getText())))) {
            txtRecarga.clear();
            refrescar();
        }
    }

    @FXML
    private void alternarTarjeta() {
        Cliente c = tblClientes.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }
        boolean activa = tarjeta(c).isActiva();
        if (Ui.intentar(() -> {
            if (activa) {
                servicio.desactivarTarjeta(actor, c);
            } else {
                servicio.activarTarjeta(actor, c);
            }
        })) {
            tblClientes.refresh();
        }
    }

    @FXML
    private void verCompras() {
        Cliente c = tblClientes.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }
        List<Compra> compras = adm.getComprasDeCliente(actor, c);
        Ui.info(compras.isEmpty() ? c.getNombre() + " no tiene compras."
                : compras.stream().map(x -> x.getId() + " · " + x.getFuncion().getPelicula().getTitulo()
                        + " · " + x.getEstado() + " · " + Formato.dinero(x.getTotal()))
                .collect(Collectors.joining("\n")));
    }

    @FXML
    private void aprobar() {
        SolicitudRecarga s = tblSolicitudes.getSelectionModel().getSelectedItem();
        if (s != null && Ui.intentar(() -> servicio.getSolicitudesRecarga().aprobar(actor, s))) {
            refrescar();
        }
    }

    @FXML
    private void rechazar() {
        SolicitudRecarga s = tblSolicitudes.getSelectionModel().getSelectedItem();
        if (s == null) {
            return;
        }
        String motivo = Ui.pedirTexto("Motivo del rechazo:");
        if (motivo != null && Ui.intentar(() -> servicio.getSolicitudesRecarga().rechazar(actor, s, motivo))) {
            refrescar();
        }
    }
}