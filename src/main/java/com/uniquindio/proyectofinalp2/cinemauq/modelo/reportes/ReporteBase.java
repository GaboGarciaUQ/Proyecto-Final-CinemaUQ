package com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public abstract class ReporteBase implements Reporte {

    protected static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    protected final String titulo;
    protected final LocalDateTime generado;
    protected final String periodo;
    protected final List<String> filas;
    protected final double total;

    protected ReporteBase(String titulo, LocalDateTime generado, String periodo,
                          List<String> filas, double total) {
        this.titulo = titulo;
        this.generado = generado;
        this.periodo = periodo;
        this.filas = new ArrayList<>(filas);
        this.total = total;
    }
}