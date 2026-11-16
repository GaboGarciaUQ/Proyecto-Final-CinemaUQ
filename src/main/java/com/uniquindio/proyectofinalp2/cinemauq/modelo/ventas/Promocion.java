package com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas;

public class Promocion {

    private String nombre;
    private double porcentajeDescuento; // 0.10 = 10 %

    public Promocion(String nombre, double porcentajeDescuento) {
        validar(nombre, porcentajeDescuento);
        this.nombre = nombre.trim();
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public double aplicar(double valor) {
        return valor - (valor * porcentajeDescuento);
    }

    public String getNombre() {
        return nombre;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setNombre(String nombre) {
        validar(nombre, porcentajeDescuento);
        this.nombre = nombre.trim();
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        validar(nombre, porcentajeDescuento);
        this.porcentajeDescuento = porcentajeDescuento;
    }

    private static void validar(String nombre, double porcentajeDescuento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La promoción necesita un nombre");
        }
        if (porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 1");
        }
    }
}