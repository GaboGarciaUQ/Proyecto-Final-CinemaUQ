package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.adapter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.ReporteBase;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.adapter.externo.ExportadorHtmlExterno;

// Adapter: traduce la interfaz Reporte a las llamadas del exportador externo.
public class ReporteHtmlAdapter extends ReporteBase {

    private final ExportadorHtmlExterno exportador;

    public ReporteHtmlAdapter(String titulo, LocalDateTime generado, String periodo,
                              List<String> filas, double total, ExportadorHtmlExterno exportador) {
        super(titulo, generado, periodo, filas, total);
        this.exportador = exportador;
    }

    @Override
    public String getExtension() {
        return "html";
    }

    @Override
    public void exportar(Path destino) throws IOException {
        exportador.definirTitulo(titulo);
        exportador.agregarParrafo("Generado: " + generado.format(FORMATO));
        exportador.agregarParrafo("Periodo: " + periodo);
        exportador.agregarParrafo("Registros: " + filas.size());

        exportador.iniciarTabla("Detalle");
        for (String fila : filas) {
            exportador.agregarFilaTabla(fila);
        }
        exportador.cerrarTabla();

        exportador.agregarParrafo("Total: " + total);
        exportador.guardarEnArchivo(destino.toString());
    }
}