package com.uniquindio.proyectofinalp2.cinemauq.patrones.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;


/*PATRÓN CREACIONAL: BUILDER
Construye una Compra paso a paso. Las partes obligatorias (cliente y al
menos una entrada) se validan en build(); las opcionales (productos,
combos) tienen valores por defecto.
Lo que NO se configura desde fuera (lo decide Compra):
 - total: se calcula a partir de entradas, productos y combos.
 - estado: toda compra nace en PENDIENTE y cambia con sus propios métodos.
*/
public class CompraBuilder {

    private String id;
    private LocalDateTime fecha;
    private Cliente cliente;
    private final List<Entrada> entradas = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();
    private final List<Combo> combos = new ArrayList<>();

    public CompraBuilder conId(String id) {
        this.id = id;
        return this;
    }

    public CompraBuilder conFecha(LocalDateTime fecha) {
        this.fecha = fecha;
        return this;
    }

    public CompraBuilder conCliente(Cliente cliente) {
        this.cliente = cliente;
        return this;
    }

    public CompraBuilder agregarEntrada(Entrada entrada) {
        this.entradas.add(Objects.requireNonNull(entrada, "La entrada no puede ser nula"));
        return this;
    }

    public CompraBuilder agregarProducto(Producto producto) {
        this.productos.add(Objects.requireNonNull(producto, "El producto no puede ser nulo"));
        return this;
    }

    public CompraBuilder agregarCombo(Combo combo) {
        this.combos.add(Objects.requireNonNull(combo, "El combo no puede ser nulo"));
        return this;
    }

    /**
     * Valida las partes obligatorias y crea la compra.
     *
     * @throws IllegalStateException si falta el cliente o no hay entradas
     */
    public Compra build() {
        if (cliente == null) {
            throw new IllegalStateException("La compra necesita un cliente");
        }
        if (entradas.isEmpty()) {
            throw new IllegalStateException("La compra necesita al menos una entrada");
        }

        String idFinal = (id != null) ? id : UUID.randomUUID().toString();
        LocalDateTime fechaFinal = (fecha != null) ? fecha : LocalDateTime.now();

        // List.copyOf: la compra recibe copias inmutables, así reutilizar
        // el builder no altera compras ya construidas.
        return new Compra(idFinal, fechaFinal, cliente,
                List.copyOf(entradas), List.copyOf(productos), List.copyOf(combos));
    }
}