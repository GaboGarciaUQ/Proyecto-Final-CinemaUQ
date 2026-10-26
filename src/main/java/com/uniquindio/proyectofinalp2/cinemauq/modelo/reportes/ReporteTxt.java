package com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ReporteTxt extends ReporteBase {

    public ReporteTxt(String titulo, LocalDateTime generado, String periodo,
                      List<String> filas, double total) {
        super(titulo, generado, periodo, filas, total);
    }

    @Override
    public String getExtension() {
        return "txt";
    }

    @Override
    public void exportar(Path destino) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(titulo).append("\n");
        sb.append("Generado: ").append(generado.format(FORMATO)).append("\n");
        sb.append("Periodo: ").append(periodo).append("\n");
        sb.append("Registros: ").append(filas.size()).append("\n");
        sb.append("------------------------------\n");
        for (String fila : filas) {
            sb.append(fila).append("\n");
        }
        sb.append("------------------------------\n");
        sb.append("Total: ").append(total).append("\n");
        Files.writeString(destino, sb.toString(), StandardCharsets.UTF_8);
    }
}