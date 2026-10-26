package com.uniquindio.proyectofinalp2.cinemauq.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formato {

    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");
    private static final NumberFormat NUMERO = NumberFormat.getIntegerInstance(ES_CO);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("h:mm a", ES_CO);
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEE d MMM", ES_CO);

    private Formato() {
    }

    public static String dinero(double valor) {
        return "$" + NUMERO.format(Math.round(valor));
    }

    public static String hora(LocalDateTime fechaHora) {
        return limpiar(HORA.format(fechaHora));
    }

    public static String dia(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        if (fecha.equals(hoy)) {
            return "Hoy";
        }
        if (fecha.equals(hoy.plusDays(1))) {
            return "Mañana";
        }
        String texto = limpiar(DIA.format(fecha)).replace(".", "");
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return dia(fechaHora.toLocalDate()) + ", " + hora(fechaHora);
    }

    public static String duracion(int minutos) {
        int horas = minutos / 60;
        int resto = minutos % 60;
        if (horas == 0) {
            return resto + " min";
        }
        return horas + " h " + resto + " min";
    }

    private static String limpiar(String texto) {
        return texto.replace('\u202f', ' ').replace('\u00a0', ' ');
    }
}