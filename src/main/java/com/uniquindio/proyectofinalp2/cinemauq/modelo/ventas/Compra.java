package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CompraInvalidaException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.state.EstadoDeCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.state.EstadoPendiente;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.PoliticaReembolso;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator.Cobro;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator.CobroBase;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator.DescuentoPromocion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator.DescuentoPuntos;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.decorator.RecargoIva;

/*
 Creacional: Builder (Compra solo se crea con Compra.Builder).
 Estructural: Decorator (el total se calcula con una cadena de Cobro).
 Comportamiento: State (Compra es el Contexto; su ciclo de vida lo manejan
 los objetos EstadoDeCompra). Los estados que reembolsan usan Strategy
 (PoliticaReembolso) para decidir si se permite y cuánto se devuelve.
*/
public class Compra {

    private final String id;
    private final LocalDateTime fecha;
    private final Cliente cliente;
    private final Funcion funcion;
    private final List<Entrada> entradas;
    private final Combo combo;
    private final List<Promocion> promociones;
    private final int puntosRedimidos;
    private final double total;
    private final String descripcionCobro;
    private EstadoDeCompra estado;
    private double montoReembolsado;

    private Compra(Builder b) {
        this.id = b.id;
        this.fecha = b.fecha;
        this.cliente = b.cliente;
        this.funcion = b.funcion;
        this.entradas = new ArrayList<>(b.entradas);
        this.combo = b.combo;
        this.promociones = new ArrayList<>(b.promociones);
        this.puntosRedimidos = b.puntosRedimidos;

        Cobro cobro = armarCobro(entradas, combo, promociones, puntosRedimidos, b.config);
        this.total = cobro.getValor();
        this.descripcionCobro = cobro.getDescripcion();

        this.estado = EstadoPendiente.INSTANCIA;
    }

    // Cadena: subtotal -> promociones -> puntos -> IVA.
    private static Cobro armarCobro(List<Entrada> entradas, Combo combo, List<Promocion> promociones,
                                    int puntos, ConfiguracionCine config) {
        double subtotal = 0;
        for (Entrada e : entradas) {
            subtotal += e.getPrecio();
        }
        if (combo != null) {
            subtotal += combo.getPrecio();
        }

        Cobro cobro = new CobroBase(subtotal);
        for (Promocion p : promociones) {
            cobro = new DescuentoPromocion(cobro, p);
        }
        if (puntos > 0) {
            cobro = new DescuentoPuntos(cobro, puntos, config.getValorPunto());
        }
        return new RecargoIva(cobro, config.getIva());
    }

    // ---- Operaciones del ciclo de vida: se delegan en el estado actual (State) ----

    public void marcarPagada() {
        estado.pagar(this);
    }

    /*
     Cancela la compra según la política recibida (Strategy) y devuelve el
     monto a reembolsar. Solo calcula: quien mueve el dinero a la tarjeta
     es el servicio, no la compra.
    */
    public double cancelar(PoliticaReembolso politica, LocalDateTime ahora) {
        return estado.cancelar(this, politica, ahora);
    }

    // ---- Métodos que invocan los estados (están en otro paquete, por eso son públicos) ----

    public void cambiarEstado(EstadoDeCompra nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "El estado es obligatorio");
    }

    // Anula las entradas y devuelve los asientos a la función.
    public void liberarEntradas() {
        for (Entrada entrada : entradas) {
            entrada.anular();
            funcion.liberar(entrada.getAsiento());
        }
    }

    /*
     Anota el monto devuelto. La tolerancia de 0.01 existe porque el total
     (con IVA) puede tener más de dos decimales y el reembolso se redondea
     a dos: un reembolso del 100 % podría superar el total por medio centavo.
    */
    public void registrarReembolso(double monto) {
        if (monto < 0 || monto > total + 0.01) {
            throw new CompraInvalidaException(
                    "El reembolso no puede ser negativo ni superar el total pagado");
        }
        this.montoReembolsado = monto;
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

    public List<Promocion> getPromociones() {
        return Collections.unmodifiableList(promociones);
    }

    public int getPuntosRedimidos() {
        return puntosRedimidos;
    }

    public double getTotal() {
        return total;
    }

    public String getDescripcionCobro() {
        return descripcionCobro;
    }

    // Se sigue exponiendo el enum para que reportes, puntos e interfaz no cambien.
    public EstadoCompra getEstado() {
        return estado.getEstado();
    }

    public double getMontoReembolsado() {
        return montoReembolsado;
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
        private final List<Promocion> promociones = new ArrayList<>();
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
            if (promocion == null) {
                throw new IllegalArgumentException("La promoción no puede ser nula");
            }
            this.promociones.add(promocion);
            return this;
        }

        public Builder puntosRedimidos(int puntos) {
            this.puntosRedimidos = puntos;
            return this;
        }

        public Compra build() {
            if (cliente == null) {
                throw new CompraInvalidaException("La compra necesita un cliente");
            }
            if (funcion == null) {
                throw new CompraInvalidaException("La compra necesita una función");
            }
            if (entradas.isEmpty()) {
                throw new CompraInvalidaException("Debe elegir al menos un asiento (entrada)");
            }
            if (puntosRedimidos < 0) {
                throw new CompraInvalidaException("Los puntos redimidos no pueden ser negativos");
            }
            for (Entrada e : entradas) {
                if (e.getFuncion() != funcion) {
                    throw new CompraInvalidaException(
                            "Todas las entradas deben pertenecer a la función de la compra");
                }
            }
            return new Compra(this);
        }
    }
}