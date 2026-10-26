package com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportePdf extends ReporteBase {

    private static final int LINEAS_POR_PAGINA = 45;

    public ReportePdf(String titulo, LocalDateTime generado, String periodo,
                      List<String> filas, double total) {
        super(titulo, generado, periodo, filas, total);
    }

    @Override
    public String getExtension() {
        return "pdf";
    }

    @Override
    public void exportar(Path destino) throws IOException {
        List<String> lineas = armarLineas();
        int paginas = (lineas.size() + LINEAS_POR_PAGINA - 1) / LINEAS_POR_PAGINA;

        // Objetos: 1 catálogo, 2 páginas, 3 fuente, luego (página, contenido) por cada página.
        List<String> objetos = new ArrayList<>();
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < paginas; i++) {
            kids.append(4 + 2 * i).append(" 0 R ");
        }
        objetos.add("<< /Type /Pages /Kids [ " + kids + "] /Count " + paginas + " >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");

        for (int i = 0; i < paginas; i++) {
            int desde = i * LINEAS_POR_PAGINA;
            int hasta = Math.min(desde + LINEAS_POR_PAGINA, lineas.size());
            StringBuilder texto = new StringBuilder("BT /F1 11 Tf 50 790 Td 16 TL\n");
            for (int j = desde; j < hasta; j++) {
                texto.append("(").append(escapar(lineas.get(j))).append(") Tj T*\n");
            }
            texto.append("ET");
            int largo = texto.toString().getBytes(StandardCharsets.ISO_8859_1).length;

            objetos.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + (5 + 2 * i) + " 0 R >>");
            objetos.add("<< /Length " + largo + " >>\nstream\n" + texto + "\nendstream");
        }

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        escribir(salida, "%PDF-1.4\n");
        List<Integer> posiciones = new ArrayList<>();
        for (int k = 0; k < objetos.size(); k++) {
            posiciones.add(salida.size());
            escribir(salida, (k + 1) + " 0 obj\n" + objetos.get(k) + "\nendobj\n");
        }

        int inicioXref = salida.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(objetos.size() + 1).append("\n");
        xref.append("0000000000 65535 f \n");
        for (int pos : posiciones) {
            xref.append(String.format("%010d 00000 n \n", pos));
        }
        xref.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\n");
        xref.append("startxref\n").append(inicioXref).append("\n%%EOF");
        escribir(salida, xref.toString());

        Files.write(destino, salida.toByteArray());
    }

    private List<String> armarLineas() {
        List<String> lineas = new ArrayList<>();
        lineas.add(titulo);
        lineas.add("Generado: " + generado.format(FORMATO));
        lineas.add("Periodo: " + periodo);
        lineas.add("Registros: " + filas.size());
        lineas.add("------------------------------");
        for (String fila : filas) {
            lineas.add(fila);
        }
        lineas.add("------------------------------");
        lineas.add("Total: " + total);
        return lineas;
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private void escribir(ByteArrayOutputStream salida, String texto) throws IOException {
        salida.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }
}