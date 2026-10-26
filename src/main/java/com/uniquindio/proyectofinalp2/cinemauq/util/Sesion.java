package com.uniquindio.proyectofinalp2.cinemauq.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;

public final class Sesion {

    private final CineServicio servicio = new CineServicio();
    private final List<Asiento> asientos = new ArrayList<>();
    private final List<Comprable> confiteria = new ArrayList<>();
    private Cliente cliente;
    private Funcion funcion;

    private Sesion() {
    }

    private static class Holder {
        private static final Sesion INSTANCIA = new Sesion();
    }

    public static Sesion getInstancia() {
        return Holder.INSTANCIA;
    }

    public CineServicio getServicio() {
        return servicio;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void iniciarSesion(Cliente cliente) {
        this.cliente = cliente;
        limpiarCompra();
    }

    public void cerrarSesion() {
        cliente = null;
        limpiarCompra();
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public void iniciarCompra(Funcion funcion) {
        limpiarCompra();
        this.funcion = funcion;
    }

    public List<Asiento> getAsientos() {
        return Collections.unmodifiableList(asientos);
    }

    public void setAsientos(List<Asiento> seleccionados) {
        asientos.clear();
        asientos.addAll(seleccionados);
    }

    public List<Comprable> getConfiteria() {
        return Collections.unmodifiableList(confiteria);
    }

    public void setConfiteria(List<Comprable> items) {
        confiteria.clear();
        confiteria.addAll(items);
    }

    public void limpiarCompra() {
        funcion = null;
        asientos.clear();
        confiteria.clear();
    }
}