package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import java.time.LocalDateTime;

public class Compra {

    private String id;
    private LocalDateTime fecha;
    private double total;
    private EstadoCompra estado;
    private Cliente cliente;
    private List<Entradas> entradas;
    private List<Producto> producto;
    private List<Combo> combos;
    private Pago pago;

    

}
