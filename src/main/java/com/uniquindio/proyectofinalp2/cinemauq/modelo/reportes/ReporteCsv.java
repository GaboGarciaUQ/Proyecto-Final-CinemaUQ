package com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ReporteCsv extends ReporteBase {

    public ReporteCsv(String titulo, LocalDateTime generado, String periodo,
                      List<String> filas, double total) {
        super(titulo, generado, periodo, filas, total);
    }

    @Override
    public String getExtension() {
        return "csv";
    }

    @Override
    public void exportar(Path destino) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Titulo,").append(campo(titulo)).append("\n");
        sb.append("Generado,").append(campo(generado.format(FORMATO))).append("\n");
        sb.append("Periodo,").append(campo(periodo)).append("\n");
        sb.append("Registros,").append(filas.size()).append("\n");
        sb.append("Detalle\n");
        for (String fila : filas) {
            sb.append(campo(fila)).append("\n");
        }
        sb.append("Total,").append(total).append("\n");
        Files.writeString(destino, sb.toString(), StandardCharsets.UTF_8);
    }

    private String campo(String valor) {
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}