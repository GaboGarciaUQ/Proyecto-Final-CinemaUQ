package com.uniquindio.proyectofinalp2.cinemauq.patrones.builder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;

public class CompraBuilder {

    private String id;
    private LocalDateTime fecha;
    private double total;
    private EstadoCompra estado;
    private Cliente cliente;
    private List<Entradas> entradas;
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

    public CompraBuilder conListEntradas(List<Entradas> ents){
        this.entradas = ents;
        return this;
    }

    public CompraBuilder conListProductos(List<Productos> prods){
        this.productos = prods;
        return this;
    }

    public CompraBuilder conListCombos(List<Combos> combs){
        this.combos = combs;
        return this;
    }

    public Compra build(){
        return new Compra(this);
    }
}
