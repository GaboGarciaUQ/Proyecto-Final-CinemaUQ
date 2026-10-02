package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import java.time.LocalDateTime;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.builder.CompraBuilder;

public class Compra {

    private final String id;
    private final LocalDateTime fecha;
    private final double total;
    private final EstadoCompra estado;
    private final Cliente cliente;
    private final List<Entrada> entradas;
    private final List<Producto> productos;
    private final List<Combo> combos;
    private final Pago pago;

    Compra(CompraBuilder b){

        this.id = b.getId();
        this.fecha = b.getFecha();
        this.total = b.getTotal();
        this.estado = b.getEstado();
        this.cliente = b.getCliente();
        this.entradas = b.getEntradas();
        this.productos = b.getProductos();
        this.combos = b.getCombos();
        this.pago = b.getPago();
    
    }


}
