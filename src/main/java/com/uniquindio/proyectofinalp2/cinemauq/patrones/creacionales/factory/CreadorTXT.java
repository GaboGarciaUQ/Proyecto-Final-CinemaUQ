package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import java.time.LocalDateTime;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.ExportadorReporte;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.ExportadorTXT;

public class CreadorTXT extends CreadorExportador {

    @Override
    protected ExportadorReporte crearReporte(String titulo, LocalDateTime generado, String periodo, List<String> filas, double total) {
        return new ExportadorTXT(titulo, generado, periodo, filas, total);
    }
}