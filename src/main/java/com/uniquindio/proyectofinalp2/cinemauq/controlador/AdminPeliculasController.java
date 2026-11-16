package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.ServicioAdministracion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminPeliculasController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtDuracion;
    @FXML private TextField txtPoster;
    @FXML private TableView<Pelicula> tblPeliculas;

    private final CineServicio servicio = Sesion.getInstancia().getServicio();
    private final ServicioAdministracion adm = servicio.getAdministracion();
    private final Administrador actor = Sesion.getInstancia().getAdmin();

    @FXML
    private void initialize() {
        Ui.columnas(tblPeliculas,
                Ui.<Pelicula>col("Título", Pelicula::getTitulo),
                Ui.<Pelicula>col("Duración", p -> Formato.duracion(p.getDuracionMinutos())),
                Ui.<Pelicula>col("Póster", p -> p.getPosterUrl() == null ? "—" : p.getPosterUrl()));
        tblPeliculas.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                txtTitulo.setText(p.getTitulo());
                txtDuracion.setText(String.valueOf(p.getDuracionMinutos()));
                txtPoster.setText(p.getPosterUrl() == null ? "" : p.getPosterUrl());
            }
        });
        refrescar();
    }

    private void refrescar() {
        Ui.items(tblPeliculas, servicio.getPeliculas());
    }

    @FXML
    private void agregar() {
        if (Ui.intentar(() -> adm.crearPelicula(actor, txtTitulo.getText(), Integer.parseInt(txtDuracion.getText().trim()), txtPoster.getText()))) {
            txtTitulo.clear();
            txtDuracion.clear();
            txtPoster.clear();
            refrescar();
        }
    }

    @FXML
    private void guardar() {
        Pelicula p = tblPeliculas.getSelectionModel().getSelectedItem();
        if (p != null && Ui.intentar(() -> adm.editarPelicula(actor, p, txtTitulo.getText(), Integer.parseInt(txtDuracion.getText().trim()), txtPoster.getText()))) {
            refrescar();
        }
    }

    @FXML
    private void eliminar() {
        Pelicula p = tblPeliculas.getSelectionModel().getSelectedItem();
        if (p != null && Ui.confirmar("¿Eliminar \"" + p.getTitulo() + "\"?") && Ui.intentar(() -> adm.eliminarPelicula(actor, p))) {
            refrescar();
        }
    }
}