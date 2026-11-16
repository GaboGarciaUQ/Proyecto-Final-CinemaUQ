package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.ServicioAdministracion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminConfiteriaController {

    @FXML private TextField txtProducto;
    @FXML private TextField txtPrecio;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TextField txtCombo;
    @FXML private TextField txtDescCombo;
    @FXML private ListView<Producto> lstComponentes;
    @FXML private TableView<Combo> tblCombos;
    @FXML private TextField txtPromo;
    @FXML private TextField txtPorcentaje;
    @FXML private TableView<Promocion> tblPromos;

    private final CineServicio servicio = Sesion.getInstancia().getServicio();
    private final ServicioAdministracion adm = servicio.getAdministracion();
    private final Administrador actor = Sesion.getInstancia().getAdmin();

    @FXML
    private void initialize() {
        Ui.columnas(tblProductos,
                Ui.<Producto>col("Producto", Producto::getNombre),
                Ui.<Producto>col("Precio", p -> Formato.dinero(p.getPrecio())));
        Ui.columnas(tblCombos,
                Ui.<Combo>col("Combo", Combo::getNombre),
                Ui.<Combo>col("Incluye", c -> c.getComponentes().stream().map(Comprable::getNombre)
                        .collect(Collectors.joining(", "))),
                Ui.<Combo>col("Desc.", c -> pct(c.getPorcentajeDescuento())),
                Ui.<Combo>col("Precio", c -> Formato.dinero(c.getPrecio())));
        Ui.columnas(tblPromos,
                Ui.<Promocion>col("Promoción", Promocion::getNombre),
                Ui.<Promocion>col("Descuento", p -> pct(p.getPorcentajeDescuento())));
        lstComponentes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstComponentes.setCellFactory(l -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Producto p, boolean vacio) {
                super.updateItem(p, vacio);
                setText(vacio || p == null ? null : p.getNombre());
            }
        });
        tblProductos.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                txtProducto.setText(p.getNombre());
                txtPrecio.setText(String.valueOf(Math.round(p.getPrecio())));
            }
        });
        tblPromos.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                txtPromo.setText(p.getNombre());
                txtPorcentaje.setText(String.valueOf(Math.round(p.getPorcentajeDescuento() * 100)));
            }
        });
        refrescar();
    }

    private String pct(double fraccion) {
        return Math.round(fraccion * 100) + " %";
    }

    private void refrescar() {
        Ui.items(tblProductos, servicio.getProductos());
        Ui.items(tblCombos, servicio.getCombos());
        Ui.items(tblPromos, servicio.getPromociones());
        lstComponentes.setItems(FXCollections.observableArrayList(servicio.getProductos()));
    }

    // ---- Productos
    @FXML
    private void agregarProducto() {
        if (Ui.intentar(() -> adm.crearProducto(actor, txtProducto.getText(), Ui.numero(txtPrecio.getText())))) {
            txtProducto.clear();
            txtPrecio.clear();
            refrescar();
        }
    }

    @FXML
    private void guardarProducto() {
        Producto p = tblProductos.getSelectionModel().getSelectedItem();
        if (p != null && Ui.intentar(() -> adm.editarProducto(actor, p, txtProducto.getText(), Ui.numero(txtPrecio.getText())))) {
            refrescar();
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto p = tblProductos.getSelectionModel().getSelectedItem();
        if (p != null && Ui.confirmar("¿Eliminar " + p.getNombre() + "?") && Ui.intentar(() -> adm.eliminarProducto(actor, p))) {
            refrescar();
        }
    }

    // ---- Combos (el descuento se escribe en % y el modelo lo guarda como fracción)
    @FXML
    private void crearCombo() {
        List<Comprable> componentes = new ArrayList<>(lstComponentes.getSelectionModel().getSelectedItems());
        if (Ui.intentar(() -> adm.crearCombo(actor, txtCombo.getText(), componentes, Ui.numero(txtDescCombo.getText()) / 100))) {
            txtCombo.clear();
            txtDescCombo.clear();
            refrescar();
        }
    }

    @FXML
    private void eliminarCombo() {
        Combo c = tblCombos.getSelectionModel().getSelectedItem();
        if (c != null && Ui.confirmar("¿Eliminar el combo " + c.getNombre() + "?") && Ui.intentar(() -> adm.eliminarCombo(actor, c))) {
            refrescar();
        }
    }

    // ---- Promociones
    @FXML
    private void agregarPromo() {
        if (Ui.intentar(() -> adm.crearPromocion(actor, txtPromo.getText(), Ui.numero(txtPorcentaje.getText()) / 100))) {
            txtPromo.clear();
            txtPorcentaje.clear();
            refrescar();
        }
    }

    @FXML
    private void guardarPromo() {
        Promocion p = tblPromos.getSelectionModel().getSelectedItem();
        if (p != null && Ui.intentar(() -> adm.editarPromocion(actor, p, txtPromo.getText(), Ui.numero(txtPorcentaje.getText()) / 100))) {
            refrescar();
        }
    }

    @FXML
    private void eliminarPromo() {
        Promocion p = tblPromos.getSelectionModel().getSelectedItem();
        if (p != null && Ui.confirmar("¿Eliminar la promoción " + p.getNombre() + "?") && Ui.intentar(() -> adm.eliminarPromocion(actor, p))) {
            refrescar();
        }
    }
}