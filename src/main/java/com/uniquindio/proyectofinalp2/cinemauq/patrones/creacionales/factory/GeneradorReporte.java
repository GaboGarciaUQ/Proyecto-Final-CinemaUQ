package com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.Reporte;

/*
 PATRÓN CREACIONAL: FACTORY METHOD (Creator abstracto).
 generar define el flujo; crearReporte es el método fábrica.
*/
public abstract class GeneradorReporte {

    public final Path generar(String titulo, String periodo, List<String> filas, double total, Path carpeta) throws IOException {
        Reporte reporte = crearReporte(titulo, LocalDateTime.now(), periodo, filas, total);
        Files.createDirectories(carpeta);
        String nombre = titulo.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        Path destino = carpeta.resolve(nombre + "." + reporte.getExtension());
        reporte.exportar(destino);
        return destino;
    }

    protected abstract Reporte crearReporte(String titulo, LocalDateTime generado, String periodo, List<String> filas, double total);
}