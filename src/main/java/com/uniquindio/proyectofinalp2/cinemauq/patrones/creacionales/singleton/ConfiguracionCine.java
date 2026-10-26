package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton;

import java.time.Duration;

/*
 PATRÓN CREACIONAL: SINGLETON (Initialization-on-demand holder).
 Reglas configurables de CinemaUQ: puntos, IVA y reembolsos.
*/
public final class ConfiguracionCine {

    /*Valores por defecto (reglas del grupo)*/
    private static final double VALOR_POR_BLOQUE_DEFECTO = 1000.0;
    private static final int PUNTOS_POR_BLOQUE_DEFECTO = 1;
    private static final int VIGENCIA_PUNTOS_ANIOS = 1;
    
    private static final int PUNTOS_PARA_ENTRADA_DEFECTO = 100;
    private static final int PUNTOS_PARA_PRODUCTO_DEFECTO = 30;
    private static final int PUNTOS_PARA_COMBO_DEFECTO = 60;

    private static final double IVA_DEFECTO = 0.19;
    private static final double VALOR_PUNTO_DEFECTO = 10.0; // pesos por punto redimido

    /*Política de reembolso*/
    private static final Duration LIMITE_REEMBOLSO_TOTAL = Duration.ofHours(24);
    private static final Duration LIMITE_CANCELACION = Duration.ofHours(2);
    private static final double PORCENTAJE_REEMBOLSO_TOTAL = 1.0;
    private static final double PORCENTAJE_REEMBOLSO_PARCIAL = 0.8;
    private static final double PORCENTAJE_REEMBOLSO_FUNCION_CANCELADA = 1.0;

    // Estado configurable
    private double valorPorBloque = VALOR_POR_BLOQUE_DEFECTO;
    private int puntosPorBloque = PUNTOS_POR_BLOQUE_DEFECTO;
    private int puntosParaEntrada = PUNTOS_PARA_ENTRADA_DEFECTO;
    private int puntosParaProducto = PUNTOS_PARA_PRODUCTO_DEFECTO;
    private int puntosParaCombo = PUNTOS_PARA_COMBO_DEFECTO;

    private ConfiguracionCine() { }

    private static class Holder {
        private static final ConfiguracionCine INSTANCIA = new ConfiguracionCine();
    }

    public static ConfiguracionCine getInstancia() {
        return Holder.INSTANCIA;
    }

    /*Puntos*/

    /* Ejemplo con valores por defecto: $25.500 -> 25 puntos. */
    public int calcularPuntos(double valorGastado) {
        if (valorGastado < 0) {
            throw new IllegalArgumentException("El valor gastado no puede ser negativo");
        }
        return (int) (valorGastado / valorPorBloque) * puntosPorBloque;
    }

    public int getVigenciaPuntosAnios() {
        return VIGENCIA_PUNTOS_ANIOS;
    }

    public int getPuntosParaEntrada() {
        return puntosParaEntrada;
    }

    public int getPuntosParaProducto() {
        return puntosParaProducto;
    }

    public int getPuntosParaCombo() {
        return puntosParaCombo;
    }

    public double getValorPunto() {
        return VALOR_PUNTO_DEFECTO;
    }

    /*Impuestos*/

    public double getIva() {
        return IVA_DEFECTO;
    }

    /*Cancelación y reembolso*/

    /* Se puede cancelar si faltan al menos 2 horas. */
    public boolean permiteCancelacion(Duration tiempoRestante) {
        return tiempoRestante.compareTo(LIMITE_CANCELACION) >= 0;
    }

    /* > 24 h: 100 %; entre 2 y 24 h: 80 %; menos de 2 h: 0 %. */
    public double calcularPorcentajeReembolso(Duration tiempoRestante) {
        if (tiempoRestante.compareTo(LIMITE_REEMBOLSO_TOTAL) > 0) {
            return PORCENTAJE_REEMBOLSO_TOTAL;
        }
        if (tiempoRestante.compareTo(LIMITE_CANCELACION) >= 0) {
            return PORCENTAJE_REEMBOLSO_PARCIAL;
        }
        return 0.0;
    }

    public double getPorcentajeReembolsoFuncionCancelada() {
        return PORCENTAJE_REEMBOLSO_FUNCION_CANCELADA;
    }

    /*Modificación (administrador)*/

    public void setReglaAcumulacion(double valorPorBloque, int puntosPorBloque) {
        if (valorPorBloque <= 0 || puntosPorBloque <= 0) {
            throw new IllegalArgumentException("Los valores deben ser mayores a cero");
        }
        this.valorPorBloque = valorPorBloque;
        this.puntosPorBloque = puntosPorBloque;
    }

    public void setEquivalenciasPuntos(int entrada, int producto, int combo) {
        if (entrada <= 0 || producto <= 0 || combo <= 0) {
            throw new IllegalArgumentException("Las equivalencias deben ser mayores a cero");
        }
        this.puntosParaEntrada = entrada;
        this.puntosParaProducto = producto;
        this.puntosParaCombo = combo;
    }
}