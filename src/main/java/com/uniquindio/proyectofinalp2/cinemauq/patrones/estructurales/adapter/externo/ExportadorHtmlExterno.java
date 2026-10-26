package com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.adapter.externo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// Simula una librería de terceros: su interfaz no se puede modificar.
public class ExportadorHtmlExterno {

    private final StringBuilder cuerpo = new StringBuilder();
    private String titulo = "";

    public void definirTitulo(String titulo) {
        this.titulo = escapar(titulo);
    }

    public void agregarParrafo(String texto) {
        cuerpo.append("<p>").append(escapar(texto)).append("</p>\n");
    }

    public void iniciarTabla(String... encabezados) {
        cuerpo.append("<table border=\"1\" cellpadding=\"6\">\n<tr>");
        for (String e : encabezados) {
            cuerpo.append("<th>").append(escapar(e)).append("</th>");
        }
        cuerpo.append("</tr>\n");
    }

    public void agregarFilaTabla(String... celdas) {
        cuerpo.append("<tr>");
        for (String c : celdas) {
            cuerpo.append("<td>").append(escapar(c)).append("</td>");
        }
        cuerpo.append("</tr>\n");
    }

    public void cerrarTabla() {
        cuerpo.append("</table>\n");
    }

    public void guardarEnArchivo(String ruta) throws IOException {
        String html = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>"
                + titulo + "</title>\n</head>\n<body>\n<h1>" + titulo + "</h1>\n"
                + cuerpo + "</body>\n</html>\n";
        Files.writeString(Paths.get(ruta), html, StandardCharsets.UTF_8);
    }

    private String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}