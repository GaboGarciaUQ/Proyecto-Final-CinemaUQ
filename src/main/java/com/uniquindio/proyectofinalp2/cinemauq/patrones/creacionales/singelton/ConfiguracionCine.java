package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singelton;

import java.time.Duration;


/*  PATRÓN CREACIONAL: =============SINGLETON=============
 
  Única fuente de verdad para las reglas configurables de CinemaUQ:
  acumulación y equivalencia de puntos, vigencia de los puntos y
  políticas de reembolso.
 
  Implementación: "Initialization-on-demand holder idiom".
   - Constructor privado: nadie más puede crear instancias.
   - La instancia se crea de forma perezosa (lazy) cuando se llama a getInstancia().
   - Es seguro entre hilos sin necesidad de synchronized.
 
  Nota sobre DIP: las clases que usen estas reglas (Compra, CuentaPuntos...)
  deberían recibir la configuración por constructor o parámetro, en lugar de
  llamar a getInstancia() internamente.
*/
public final class ConfiguracionCine {

    // Valores por defecto (REGLAS DEL GRUPO: documentarlas en el informe)

    /** Regla de acumulación: por cada este valor gastado se gana 1 bloque de puntos. */
    private static final double VALOR_POR_BLOQUE_DEFECTO = 1000.0;

    /** Puntos que se ganan por cada bloque de valor gastado. */
    private static final int PUNTOS_POR_BLOQUE_DEFECTO = 1;

    /** Vigencia de los puntos (el enunciado exige 1 año). */
    private static final int VIGENCIA_PUNTOS_ANIOS = 1;

    /** Equivalencias de redención. */
    private static final int PUNTOS_PARA_ENTRADA_DEFECTO = 100;
    private static final int PUNTOS_PARA_PRODUCTO_DEFECTO = 30;
    private static final int PUNTOS_PARA_COMBO_DEFECTO = 60;

    /** Política de reembolso (enunciado). */
    private static final Duration LIMITE_REEMBOLSO_TOTAL = Duration.ofHours(24);
    private static final Duration LIMITE_CANCELACION = Duration.ofHours(2);
    private static final double PORCENTAJE_REEMBOLSO_TOTAL = 1.0;     // > 24 h
    private static final double PORCENTAJE_REEMBOLSO_PARCIAL = 0.8;   // entre 2 y 24 h
    private static final double PORCENTAJE_REEMBOLSO_FUNCION_CANCELADA = 1.0;

    // ---------------------------------------------------------------
    // Estado configurable
    // ---------------------------------------------------------------

    private double valorPorBloque = VALOR_POR_BLOQUE_DEFECTO;
    private int puntosPorBloque = PUNTOS_POR_BLOQUE_DEFECTO;
    private int puntosParaEntrada = PUNTOS_PARA_ENTRADA_DEFECTO;
    private int puntosParaProducto = PUNTOS_PARA_PRODUCTO_DEFECTO;
    private int puntosParaCombo = PUNTOS_PARA_COMBO_DEFECTO;

    // Singleton

    private ConfiguracionCine() {
        // Constructor privado: impide new ConfiguracionCine() desde fuera.
    }

    /** Clase interna que contiene la única instancia (se carga al primer uso). */
    private static class Holder {
        private static final ConfiguracionCine INSTANCIA = new ConfiguracionCine();
    }

    public static ConfiguracionCine getInstancia() {
        return Holder.INSTANCIA;
    }

    // Reglas de puntos

    /*
      Calcula los puntos que genera una compra según el valor gastado.
      Ejemplo con los valores por defecto: $25.500 -> 25 puntos.
     */
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

    // Reglas de cancelación y reembolso

    /** Indica si todavía se puede cancelar una compra (mínimo 2 horas antes). */
    public boolean permiteCancelacion(Duration tiempoRestante) {
        return tiempoRestante.compareTo(LIMITE_CANCELACION) >= 0;
    }

   
    /*Porcentaje a reembolsar según el tiempo restante para la función.
     - Más de 24 h        -> 1.0 (100 %)
     - Entre 2 y 24 h     -> 0.8 (80 %)
     - Menos de 2 h       -> 0.0 (no se permite cancelar)
    
    Quien llame debe usar permiteCancelacion() y lanzar
    CancelacionNoPermitidaException cuando corresponda.
    */
    public double calcularPorcentajeReembolso(Duration tiempoRestante) {
        if (tiempoRestante.compareTo(LIMITE_REEMBOLSO_TOTAL) > 0) {
            return PORCENTAJE_REEMBOLSO_TOTAL;
        }
        if (tiempoRestante.compareTo(LIMITE_CANCELACION) >= 0) {
            return PORCENTAJE_REEMBOLSO_PARCIAL;
        }
        return 0.0;
    }

    /** Si el cine cancela la función, siempre se devuelve el 100 %. */
    public double getPorcentajeReembolsoFuncionCancelada() {
        return PORCENTAJE_REEMBOLSO_FUNCION_CANCELADA;
    }

    // Modificación controlada (uso del administrador)

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