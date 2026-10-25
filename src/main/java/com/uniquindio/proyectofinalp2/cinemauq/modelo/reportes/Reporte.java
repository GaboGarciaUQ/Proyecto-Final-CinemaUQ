package com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes;

import java.io.IOException;
import java.nio.file.Path;

public interface Reporte {

    String getExtension();

    void exportar(Path destino) throws IOException;
}