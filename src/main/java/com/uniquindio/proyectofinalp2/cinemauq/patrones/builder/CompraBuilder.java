package com.uniquindio.proyectofinalp2.cinemauq.patrones.builder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;

public class CompraBuilder {

    private String id;
    private LocalDateTime fecha;
    private double total;
    private EstadoCompra estado;
    private Cliente cliente;
    private List<Entrada> entradas;
    private List<Producto> productos;
    private List<Combo> combos;
    private Pago pago;

    public CompraBuilder conNumMatricula(String i){
        this.id = i;
        return this;
    }

    public CompraBuilder conFecha(LocalDateTime f){
        this.fecha = f;
        return this;
    }

    public CompraBuilder conTotal(double t){
        this.total = t;
        return this;
    }

    public CompraBuilder conEstadoCompra(EstadoCompra e){
        this.estado = e;
        return this;
    }

    public CompraBuilder conCliente(Cliente c){
        this.cliente = c;
        return this;
    }

    public CompraBuilder conListEntradas(List<Entrada> ents){
        this.entradas = ents;
        return this;
    }

    public CompraBuilder conListProductos(List<Producto> prods){
        this.productos = prods;
        return this;
    }

    public CompraBuilder conListCombos(List<Combo> combs){
        this.combos = combs;
        return this;
    }

    public Compra build(){
        return new Compra(this);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getTotal() {
        return total;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<Entrada> getEntradas() {
        return entradas;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Combo> getCombos() {
        return combos;
    }

    public Pago getPago() {
        return pago;
    }
}
