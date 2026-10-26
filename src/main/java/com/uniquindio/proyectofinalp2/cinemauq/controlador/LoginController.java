package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.util.Optional;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CinemaException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Usuario;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.util.Navegador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Vista;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblMensaje;
    @FXML private Label lblCambio;
    @FXML private VBox boxNombre;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private Button btnAccion;
    @FXML private Button btnCambiar;

    private final Sesion sesion = Sesion.getInstancia();
    private final CineServicio servicio = sesion.getServicio();
    private boolean modoRegistro;

    @FXML
    private void accion() {
        lblMensaje.setText("");
        if (modoRegistro) {
            registrar();
        } else {
            ingresar();
        }
    }

    @FXML
    private void cambiarModo() {
        modoRegistro = !modoRegistro;
        lblMensaje.setText("");
        boxNombre.setVisible(modoRegistro);
        boxNombre.setManaged(modoRegistro);
        lblTitulo.setText(modoRegistro ? "Crear cuenta" : "Ingresar");
        lblSubtitulo.setText(modoRegistro
                ? "Tu tarjeta virtual se crea con la cuenta."
                : "Usa el correo con el que te registraste.");
        btnAccion.setText(modoRegistro ? "Crear cuenta" : "Ingresar");
        lblCambio.setText(modoRegistro ? "¿Ya tienes cuenta?" : "¿Aún no tienes cuenta?");
        btnCambiar.setText(modoRegistro ? "Ingresar" : "Crear cuenta");
    }

    private void ingresar() {
        String correo = txtCorreo.getText().trim();
        if (correo.isEmpty()) {
            mostrarError("Escribe tu correo");
            return;
        }
        Optional<Usuario> usuario = servicio.autenticar(correo);
        if (usuario.isEmpty()) {
            mostrarError("No encontramos una cuenta con ese correo");
            return;
        }
        if (usuario.get().esAdministrador()) {
            mostrarError("El panel de administrador llega en la siguiente etapa");
            return;
        }
        entrar((Cliente) usuario.get());
    }

    private void registrar() {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("Escribe tu nombre");
            return;
        }
        Cliente cliente;
        try {
            cliente = servicio.registrarCliente(nombre, correo);
        } catch (CinemaException e) {
            mostrarError(e.getMessage());
            return;
        }
        entrar(cliente);
    }

    private void entrar(Cliente cliente) {
        sesion.iniciarSesion(cliente);
        Navegador.ir(Vista.CARTELERA);
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
    }
}