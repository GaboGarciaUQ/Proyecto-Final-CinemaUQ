package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

/*
 PATRÓN CREACIONAL: BUILDER
 Compra es el producto y solo se crea con Compra.Builder.
 Cálculo del total: subtotal -> promoción -> puntos -> IVA.
*/
public class Compra {

    private final String id;
    private final LocalDateTime fecha;
    private final Cliente cliente;
    private final Funcion funcion;
    private final List<Entrada> entradas;
    private final Combo combo;
    private final Promocion promocion;
    private final int puntosRedimidos;
    private final double total;
    private EstadoCompra estado;

    private Compra(Builder b) {
        this.id = b.id;
        this.fecha = b.fecha;
        this.cliente = b.cliente;
        this.funcion = b.funcion;
        this.entradas = new ArrayList<>(b.entradas);
        this.combo = b.combo;
        this.promocion = b.promocion;
        this.puntosRedimidos = b.puntosRedimidos;

        double base = calcularBase(entradas, combo, promocion);
        base = base - (puntosRedimidos * b.config.getValorPunto());
        this.total = base + (base * b.config.getIva());

        this.estado = EstadoCompra.PENDIENTE;
    }

    // Subtotal de entradas y combo, con la promoción aplicada.
    private static double calcularBase(List<Entrada> entradas, Combo combo, Promocion promocion) {
        double subtotal = 0;
        for (Entrada e : entradas) {
            subtotal += e.getPrecio();
        }
        if (combo != null) {
            subtotal += combo.getPrecio();
        }
        if (promocion != null) {
            subtotal = promocion.aplicar(subtotal);
        }
        return subtotal;
    }

    public void marcarPagada() {
        if (estado != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo una compra pendiente puede pagarse (estado: " + estado + ")");
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

    public Funcion getFuncion() {
        return funcion;
    }

    public List<Entrada> getEntradas() {
        return Collections.unmodifiableList(entradas);
    }

    public Combo getCombo() {
        return combo;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public int getPuntosRedimidos() {
        return puntosRedimidos;
    }

    public double getTotal() {
        return total;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public static class Builder {

        private final ConfiguracionCine config;
        private String id = UUID.randomUUID().toString();
        private LocalDateTime fecha = LocalDateTime.now();

        // Obligatorios
        private Cliente cliente;
        private Funcion funcion;
        private final List<Entrada> entradas = new ArrayList<>();

        // Opcionales
        private Combo combo;
        private Promocion promocion;
        private int puntosRedimidos = 0;

        public Builder(ConfiguracionCine config) {
            this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
        }

        public Builder cliente(Cliente cliente) {
            this.cliente = cliente;
            return this;
        }

        public Builder funcion(Funcion funcion) {
            this.funcion = funcion;
            return this;
        }

        public Builder entrada(Entrada entrada) {
            if (entrada == null) {
                throw new IllegalArgumentException("La entrada no puede ser nula");
            }
            this.entradas.add(entrada);
            return this;
        }

        public Builder combo(Combo combo) {
            this.combo = combo;
            return this;
        }

        public Builder promocion(Promocion promocion) {
            this.promocion = promocion;
            return this;
        }

        public Builder puntosRedimidos(int puntos) {
            this.puntosRedimidos = puntos;
            return this;
        }

        public Compra build() {
            if (cliente == null) {
                throw new IllegalStateException("La compra necesita un cliente");
            }
            if (funcion == null) {
                throw new IllegalStateException("La compra necesita una función");
            }
            if (entradas.isEmpty()) {
                throw new IllegalStateException("Debe elegir al menos un asiento (entrada)");
            }
            if (puntosRedimidos < 0) {
                throw new IllegalStateException("Los puntos redimidos no pueden ser negativos");
            }
            for (Entrada e : entradas) {
                if (e.getFuncion() != funcion) {
                    throw new IllegalStateException(
                            "Todas las entradas deben pertenecer a la función de la compra");
                }
            }
            double base = calcularBase(entradas, combo, promocion);
            if (puntosRedimidos * config.getValorPunto() > base) {
                throw new IllegalStateException(
                        "Los puntos redimidos superan el valor de la compra");
            }
            return new Compra(this);
        }
    }
}