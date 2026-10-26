package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

import java.util.Objects;
import java.util.UUID;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;



public abstract class Entrada {

    private final String numero;
    private final Funcion funcion;
    private final Asiento asiento;
    private final double precio;
    private EstadoEntrada estado;

    
    /*Constructor protegido: solo lo usan las subclases.
    Quien crea entradas desde fuera debe hacerlo con una EntradaFactory.
    */
    protected Entrada(Funcion funcion, Asiento asiento) {
        this.funcion = Objects.requireNonNull(funcion, "La función es obligatoria");
        this.asiento = Objects.requireNonNull(asiento, "El asiento es obligatorio");
        this.numero = UUID.randomUUID().toString();
        // Se llama después de asignar funcion y asiento, que son lo único
        // que calcularPrecio() puede usar en las subclases.
        this.precio = calcularPrecio();
        this.estado = EstadoEntrada.VIGENTE;
    }

    
    /*Precio de esta entrada según su tipo.
    Las subclases deben usar solo getFuncion() y getAsiento(), no sus propios
    atributos (todavía no están inicializados cuando el constructor lo llama).
    */
    protected abstract double calcularPrecio();

    /** Anula la entrada (por ejemplo, al cancelar la compra). */
    public void anular() {
        if (estado != EstadoEntrada.VIGENTE) {
            throw new EstadoInvalidoException("Solo una entrada vigente puede anularse (estado: " + estado + ")");
        }
        estado = EstadoEntrada.ANULADA;
    }

    public String getNumero() {
        return numero;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public Asiento getAsiento() {
        return asiento;
    }

    public double getPrecio() {
        return precio;
    }

    public EstadoEntrada getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return funcion + " - asiento " + asiento + " - $" + precio;
    }
}