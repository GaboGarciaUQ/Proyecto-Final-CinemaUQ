package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.TipoAsiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.ServicioAdministracion;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.ServicioAdministracion.TipoSala;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminFuncionesController {

    @FXML private ComboBox<Pelicula> cmbPelicula;
    @FXML private ComboBox<Sala> cmbSala;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtPrecio;
    @FXML private TableView<Funcion> tblFunciones;
    @FXML private TextField txtNombreSala;
    @FXML private ComboBox<TipoSala> cmbTipoSala;
    @FXML private TableView<Sala> tblSalas;
    @FXML private TextField txtFila;
    @FXML private TextField txtNumero;
    @FXML private ComboBox<TipoAsiento> cmbTipoAsiento;
    @FXML private Label lblAccesibles;

    private final CineServicio servicio = Sesion.getInstancia().getServicio();
    private final ServicioAdministracion adm = servicio.getAdministracion();
    private final Administrador actor = Sesion.getInstancia().getAdmin();

    @FXML
    private void initialize() {
        Ui.texto(cmbPelicula, Pelicula::getTitulo);
        Ui.texto(cmbSala, Sala::toString);
        Ui.texto(cmbTipoSala, t -> t.name().replace("SALA_", ""));
        cmbTipoSala.setItems(FXCollections.observableArrayList(TipoSala.values()));
        Ui.columnas(tblFunciones,
                Ui.<Funcion>col("Película", f -> f.getPelicula().getTitulo()),
                Ui.<Funcion>col("Sala", f -> f.getSala().getNombre()),
                Ui.<Funcion>col("Formato", f -> f.getSala().getTarifa().getNombre()),
                Ui.<Funcion>col("Fecha y hora", f -> Formato.fechaHora(f.getFechaHora())),
                Ui.<Funcion>col("Precio base", f -> Formato.dinero(f.getPrecioBase())),
                Ui.<Funcion>col("Ocupación", f -> f.getCantidadOcupados() + "/" + f.getCapacidad()),
                Ui.<Funcion>col("Estado", f -> f.estaCancelada() ? "Cancelada"
                        : f.getFechaHora().isBefore(LocalDateTime.now()) ? "Finalizada" : "Programada"));
        Ui.columnas(tblSalas,
                Ui.<Sala>col("Sala", Sala::getNombre),
                Ui.<Sala>col("Formato", s -> s.getTarifa().getNombre()),
                Ui.<Sala>col("Capacidad", s -> String.valueOf(s.getCapacidad())));
        Ui.texto(cmbTipoAsiento, t -> t == TipoAsiento.ACCESIBLE ? "Accesible" : "General");
        cmbTipoAsiento.setItems(FXCollections.observableArrayList(TipoAsiento.values()));
        cmbTipoAsiento.setValue(TipoAsiento.ACCESIBLE);
        tblSalas.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> mostrarAccesibles(s));
        tblFunciones.getSelectionModel().selectedItemProperty().addListener((o, a, f) -> {
            if (f != null) {
                cmbPelicula.setValue(f.getPelicula());
                cmbSala.setValue(f.getSala());
                dpFecha.setValue(f.getFechaHora().toLocalDate());
                txtHora.setText(f.getFechaHora().toLocalTime().withSecond(0).withNano(0).toString());
                txtPrecio.setText(String.valueOf(Math.round(f.getPrecioBase())));
            }
        });
        // Al abrir cada desplegable se actualiza solo ese, para ver películas y salas creadas en otras pestañas.
        cmbPelicula.setOnShowing(e -> recargar(cmbPelicula, servicio.getPeliculas()));
        cmbSala.setOnShowing(e -> recargar(cmbSala, servicio.getSalas()));
        refrescar();
    }

    private void refrescar() {
        recargarCombos();
        Ui.items(tblFunciones, servicio.getFunciones());
        Ui.items(tblSalas, servicio.getSalas());
    }

    // Recarga la lista conservando lo que ya estaba elegido en ese desplegable.
    private <T> void recargar(ComboBox<T> combo, List<T> datos) {
        T elegido = combo.getValue();
        combo.getItems().setAll(datos);
        combo.setValue(elegido);
    }

    private void recargarCombos() {
        recargar(cmbPelicula, servicio.getPeliculas());
        recargar(cmbSala, servicio.getSalas());
    }

    private LocalDateTime fechaHora() {
        if (dpFecha.getValue() == null) {
            throw new IllegalArgumentException("Elige la fecha de la función");
        }
        return LocalDateTime.of(dpFecha.getValue(), LocalTime.parse(txtHora.getText().trim()));
    }

    @FXML
    private void programar() {
        if (Ui.intentar(() -> adm.programarFuncion(actor, cmbPelicula.getValue(), cmbSala.getValue(),
                fechaHora(), Ui.numero(txtPrecio.getText())))) {
            refrescar();
        }
    }

    @FXML
    private void reprogramar() {
        Funcion f = tblFunciones.getSelectionModel().getSelectedItem();
        if (f != null && Ui.intentar(() -> adm.reprogramarFuncion(actor, f, fechaHora(), Ui.numero(txtPrecio.getText())))) {
            refrescar();
        }
    }

    @FXML
    private void cancelarFuncion() {
        Funcion f = tblFunciones.getSelectionModel().getSelectedItem();
        if (f == null || !Ui.confirmar("¿Cancelar esta función? Se reembolsará el 100 % a los clientes.")) {
            return;
        }
        int[] n = new int[1];
        if (Ui.intentar(() -> n[0] = servicio.cancelarFuncion(actor, f))) {
            Ui.info("Función cancelada. Compras reembolsadas: " + n[0]);
            refrescar();
        }
    }

    @FXML
    private void eliminarFuncion() {
        Funcion f = tblFunciones.getSelectionModel().getSelectedItem();
        if (f != null && Ui.confirmar("¿Eliminar la función?") && Ui.intentar(() -> adm.eliminarFuncion(actor, f))) {
            refrescar();
        }
    }

    private void mostrarAccesibles(Sala sala) {
        if (sala == null) {
            lblAccesibles.setText("");
            return;
        }
        String lista = sala.getAsientos().stream().filter(Asiento::esAccesible)
                .map(a -> "" + a.getFila() + a.getNumero()).collect(Collectors.joining(", "));
        lblAccesibles.setText("Asientos accesibles de " + sala.getNombre() + ": " + (lista.isEmpty() ? "ninguno" : lista));
    }

    @FXML
    private void aplicarTipoAsiento() {
        Sala sala = tblSalas.getSelectionModel().getSelectedItem();
        if (sala == null) {
            Ui.error("Selecciona primero una sala de la tabla");
            return;
        }
        if (Ui.intentar(() -> {
            String fila = txtFila.getText().trim().toUpperCase();
            if (fila.length() != 1) {
                throw new IllegalArgumentException("La fila es una sola letra (ej. H)");
            }
            adm.definirTipoAsiento(actor, sala, fila.charAt(0), Integer.parseInt(txtNumero.getText().trim()),
                    cmbTipoAsiento.getValue());
        })) {
            mostrarAccesibles(sala);
        }
    }

    @FXML
    private void crearSala() {
        if (Ui.intentar(() -> adm.crearSala(actor, cmbTipoSala.getValue(), txtNombreSala.getText()))) {
            txtNombreSala.clear();
            refrescar();
        }
    }

    @FXML
    private void eliminarSala() {
        Sala s = tblSalas.getSelectionModel().getSelectedItem();
        if (s != null && Ui.confirmar("¿Eliminar la sala " + s.getNombre() + "?") && Ui.intentar(() -> adm.eliminarSala(actor, s))) {
            refrescar();
        }
    }
}