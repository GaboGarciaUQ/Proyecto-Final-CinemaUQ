package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import java.time.LocalDateTime;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.Reporte;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.adapter.ReporteHtmlAdapter;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.adapter.externo.ExportadorHtmlExterno;

public class GeneradorHtml extends GeneradorReporte {

    @Override
    protected Reporte crearReporte(String titulo, LocalDateTime generado, String periodo, List<String> filas, double total) {
        return new ReporteHtmlAdapter(titulo, generado, periodo, filas, total, new ExportadorHtmlExterno());
    }
}