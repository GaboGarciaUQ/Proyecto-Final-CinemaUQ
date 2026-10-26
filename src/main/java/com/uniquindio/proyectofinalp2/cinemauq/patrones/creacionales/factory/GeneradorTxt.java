package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import java.time.LocalDateTime;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.Reporte;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.ReporteTxt;

public class GeneradorTxt extends GeneradorReporte {

    @Override
    protected Reporte crearReporte(String titulo, LocalDateTime generado, String periodo, List<String> filas, double total) {
        return new ReporteTxt(titulo, generado, periodo, filas, total);
    }
}