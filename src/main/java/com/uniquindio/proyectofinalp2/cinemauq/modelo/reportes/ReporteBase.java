package com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class ReporteBase implements Reporte {

    protected static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    protected final String titulo;
    protected final LocalDateTime generado;
    protected final String periodo;
    protected final List<String> filas;
    protected final double total;

    private static final Locale LOCAL = Locale.forLanguageTag("es-CO");

    // 45000.0 -> "45.000"; 12.5 -> "12,50". Sin ".0" sobrante y con separador de miles.
    protected String textoTotal() {
        if (total == Math.rint(total)) {
            return String.format(LOCAL, "%,.0f", total);
        }
        return String.format(LOCAL, "%,.2f", total);
    }

    // Para CSV: número sin separador de miles y con punto decimal, listo para una hoja de cálculo.
    protected String textoTotalPlano() {
        if (total == Math.rint(total)) {
            return String.format(Locale.ROOT, "%.0f", total);
        }
        return String.format(Locale.ROOT, "%.2f", total);
    }

    protected ReporteBase(String titulo, LocalDateTime generado, String periodo,
                          List<String> filas, double total) {
        this.titulo = titulo;
        this.generado = generado;
        this.periodo = periodo;
        this.filas = new ArrayList<>(filas);
        this.total = total;
    }
}