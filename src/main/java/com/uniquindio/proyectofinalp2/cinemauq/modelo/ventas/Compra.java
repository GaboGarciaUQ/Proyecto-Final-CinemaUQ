package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;


/*Una compra del cliente: entradas más productos y combos de confitería.
Es el PRODUCTO del patrón Builder: se crea con CompraBuilder.
El total se calcula a partir de su contenido y toda compra nace PENDIENTE.
*/
public class Compra {

    private final String id;
    private final LocalDateTime fecha;
    private final Cliente cliente;
    private final List<Entrada> entradas;
    private final List<Producto> productos;
    private final List<Combo> combos;
    private final double total;
    private EstadoCompra estado;

    // Público porque CompraBuilder vive en otro paquete.
    public Compra(String id, LocalDateTime fecha, Cliente cliente,
                  List<Entrada> entradas, List<Producto> productos, List<Combo> combos) {
        this.id = id;
        this.fecha = fecha;
        this.cliente = cliente;
        this.entradas = entradas;
        this.productos = productos;
        this.combos = combos;
        this.total = calcularTotal();
        this.estado = EstadoCompra.PENDIENTE;
    }

    private double calcularTotal() {
        double suma = 0;
        for (Entrada e : entradas) {
            suma += e.getPrecio();
        }
        for (Producto p : productos) {
            suma += p.getPrecio();
        }
        for (Combo c : combos) {
            suma += c.getPrecio();
        }
        return suma;
    }

    /** Pasa de PENDIENTE a PAGADA. Se llama después de cobrar a la tarjeta. */
    public void marcarPagada() {
        if (estado != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException("Solo una compra pendiente puede pagarse (estado: " + estado + ")");
        }
        estado = EstadoCompra.PAGADA;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<Entrada> getEntradas() {
        return Collections.unmodifiableList(entradas);
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public List<Combo> getCombos() {
        return Collections.unmodifiableList(combos);
    }

    public double getTotal() {
        return total;
    }

    public EstadoCompra getEstado() {
        return estado;
    }
}