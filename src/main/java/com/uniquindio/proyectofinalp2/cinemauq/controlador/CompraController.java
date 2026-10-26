package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AsientoNoDisponibleException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CinemaException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.SaldoInsuficienteException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CompraController {

    @FXML private BarraSuperiorController barraController;
    @FXML private HBox vistaPago;
    @FXML private VBox vistaConfirmacion;
    @FXML private Label lblPelicula;
    @FXML private Label lblFuncion;
    @FXML private Label tituloConfiteria;
    @FXML private VBox lineasEntradas;
    @FXML private VBox lineasConfiteria;
    @FXML private VBox listaPromociones;
    @FXML private HBox filaDescuento;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDescuento;
    @FXML private Label lblEtiquetaIva;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;
    @FXML private Label lblPuntos;
    @FXML private Label lblSaldo;
    @FXML private Label lblSaldoFinal;
    @FXML private Label lblError;
    @FXML private Button btnPagar;
    @FXML private Button btnVolver;
    @FXML private Label lblCodigo;
    @FXML private Label lblConfPelicula;
    @FXML private Label lblConfAsientos;
    @FXML private Label lblConfTotal;
    @FXML private Label lblConfPuntos;
    @FXML private Label lblConfSaldo;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();
    private final Cliente cliente = sesion.getCliente();
    private final Funcion funcion = sesion.getFuncion();
    private final List<Asiento> asientos = sesion.getAsientos();
    private final List<Comprable> confiteria = sesion.getConfiteria();
    private final List<Promocion> aplicadas = new ArrayList<>();
    private double subtotal;

    @FXML
    private void initialize() {
        lblPelicula.setText(funcion.getPelicula().getTitulo());
        lblFuncion.setText(Formato.fechaHora(funcion.getFechaHora()) + " · " + funcion.getSala().getNombre()
                + " · " + funcion.getSala().getTarifa().getNombre());

        for (Asiento a : asientos) {
            double precio = servicio.precioEntrada(funcion, a);
            subtotal += precio;
            String tipo = a.esAccesible() ? "Entrada " + etiqueta(a) + " (accesible)" : "Entrada " + etiqueta(a);
            lineasEntradas.getChildren().add(linea(tipo, precio));
        }
        cargarConfiteria();
        cargarPromociones();
        recalcular();
    }

    private void cargarConfiteria() {
        Map<Comprable, Integer> cantidades = new LinkedHashMap<>();
        for (Comprable c : confiteria) {
            cantidades.merge(c, 1, Integer::sum);
        }
        boolean hay = !cantidades.isEmpty();
        tituloConfiteria.setVisible(hay);
        tituloConfiteria.setManaged(hay);
        lineasConfiteria.setVisible(hay);
        lineasConfiteria.setManaged(hay);
        for (Map.Entry<Comprable, Integer> e : cantidades.entrySet()) {
            double valor = e.getKey().getPrecio() * e.getValue();
            subtotal += valor;
            lineasConfiteria.getChildren().add(linea(e.getValue() + " × " + e.getKey().getNombre(), valor));
        }
    }

    private void cargarPromociones() {
        List<Promocion> disponibles = servicio.getPromociones();
        if (disponibles.isEmpty()) {
            Label vacio = new Label("No hay promociones disponibles");
            vacio.getStyleClass().add("texto-suave");
            listaPromociones.getChildren().add(vacio);
            return;
        }
        for (Promocion p : disponibles) {
            CheckBox opcion = new CheckBox(p.getNombre() + " (−" + Math.round(p.getPorcentajeDescuento() * 100) + "%)");
            opcion.getStyleClass().add("check-cine");
            opcion.setOnAction(e -> {
                if (opcion.isSelected()) {
                    aplicadas.add(p);
                } else {
                    aplicadas.remove(p);
                }
                recalcular();
            });
            listaPromociones.getChildren().add(opcion);
        }
    }

    private HBox linea(String texto, double valor) {
        Label nombre = new Label(texto);
        nombre.setWrapText(true);
        nombre.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nombre, Priority.ALWAYS);
        Label precio = new Label(Formato.dinero(valor));
        precio.getStyleClass().add("texto-suave");
        return new HBox(8, nombre, precio);
    }

    private void recalcular() {
        lblError.setText("");
        Compra simulada = servicio.simularCompra(cliente, funcion, asientos, confiteria, aplicadas);
        double total = simulada.getTotal();
        double iva = servicio.getConfig().getIva();
        double baseConDescuento = total / (1 + iva);
        double descuento = subtotal - baseConDescuento;

        lblSubtotal.setText(Formato.dinero(subtotal));
        lblDescuento.setText(descuento > 0.5 ? "−" + Formato.dinero(descuento) : Formato.dinero(0));
        lblEtiquetaIva.setText("IVA (" + Math.round(iva * 100) + "%)");
        lblIva.setText(Formato.dinero(total - baseConDescuento));
        lblTotal.setText(Formato.dinero(total));
        lblPuntos.setText("Ganarás " + servicio.puntosPorCompra(total) + " pts con esta compra");

        Tarjeta tarjeta = servicio.getTarjeta(cliente);
        double saldo = tarjeta.getSaldo();
        lblSaldo.setText(Formato.dinero(saldo));
        lblSaldoFinal.setText(Formato.dinero(saldo - total));

        if (!tarjeta.isActiva()) {
            bloquearPago("Tu tarjeta está inactiva. Contacta al administrador");
        } else if (saldo < total) {
            bloquearPago("Saldo insuficiente. Pide una recarga al administrador");
        } else {
            btnPagar.setDisable(false);
        }
    }

    private void bloquearPago(String mensaje) {
        btnPagar.setDisable(true);
        lblError.setText(mensaje);
    }

    @FXML
    private void pagar() {
        try {
            Compra compra = servicio.comprar(cliente, funcion, asientos, confiteria, aplicadas);
            mostrarConfirmacion(compra);
        } catch (SaldoInsuficienteException e) {
            bloquearPago("Te faltan " + Formato.dinero(e.getFaltante()) + ". Pide una recarga al administrador");
        } catch (AsientoNoDisponibleException e) {
            bloquearPago("Uno de los asientos ya fue vendido. Vuelve y elige otros");
        } catch (CinemaException e) {
            bloquearPago(e.getMessage());
        }
    }

    private void mostrarConfirmacion(Compra compra) {
        double saldo = servicio.getTarjeta(cliente).getSaldo();
        lblCodigo.setText("Código de compra: " + compra.getId());
        lblConfPelicula.setText(funcion.getPelicula().getTitulo() + " · " + Formato.fechaHora(funcion.getFechaHora()));
        lblConfAsientos.setText("Asientos: " + asientos.stream().map(this::etiqueta).collect(Collectors.joining(", ")));
        lblConfTotal.setText("Pagaste " + Formato.dinero(compra.getTotal()));
        lblConfPuntos.setText("Ganaste " + servicio.puntosPorCompra(compra.getTotal()) + " pts");
        lblConfSaldo.setText("Saldo restante: " + Formato.dinero(saldo));
        barraController.actualizarSaldo();
        mostrar(vistaPago, false);
        mostrar(vistaConfirmacion, true);
    }

    private void mostrar(Pane panel, boolean visible) {
        panel.setVisible(visible);
        panel.setManaged(visible);
    }

    private String etiqueta(Asiento a) {
        return "" + a.getFila() + a.getNumero();
    }

    @FXML
    private void volver() {
        Navegador.ir(Vista.CONFITERIA);
    }

    @FXML
    private void terminar() {
        sesion.limpiarCompra();
        Navegador.ir(Vista.CARTELERA);
    }
}