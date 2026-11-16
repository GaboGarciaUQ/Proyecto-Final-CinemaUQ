package com.uniquindio.proyectofinalp2.cinemauq.servicio;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.CalcularEstadisticas;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.CreadorExportador;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;

/*
 Convierte las estadísticas en reportes exportables. No calcula nada: pide los datos a
 CalcularEstadisticas, los arma como filas de texto y deja que el GeneradorReporte
 (Factory Method) decida el formato: TXT, PDF, CSV o HTML.
*/
public class ServicioReportes {

    public enum TipoReporte {
        VENTAS_POR_PERIODO,
        ENTRADAS_POR_PELICULA,
        OCUPACION_SALAS,
        PRODUCTOS_Y_COMBOS,
        RECARGAS,
        CANCELACIONES_Y_REEMBOLSOS,
        PUNTOS
    }

    private static final Locale LOCAL = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CineServicio servicio;

    public ServicioReportes(CineServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio es obligatorio");
    }

    /* Genera un reporte con los datos de este momento. desde y hasta solo se usan en los reportes por periodo. */
    public Path generar(TipoReporte tipo, CreadorExportador generador,
                        LocalDate desde, LocalDate hasta, Path carpeta) throws IOException {
        Objects.requireNonNull(tipo, "El tipo de reporte es obligatorio");
        Objects.requireNonNull(generador, "El generador es obligatorio");
        Objects.requireNonNull(carpeta, "La carpeta es obligatoria");
        CalcularEstadisticas est = servicio.getEstadisticas();
        switch (tipo) {
            case VENTAS_POR_PERIODO:
                return ventas(est, generador, desde, hasta, carpeta);
            case ENTRADAS_POR_PELICULA:
                return entradas(est, generador, desde, hasta, carpeta);
            case OCUPACION_SALAS:
                return ocupacion(est, generador, carpeta);
            case PRODUCTOS_Y_COMBOS:
                return productos(est, generador, carpeta);
            case RECARGAS:
                return recargas(est, generador, desde, hasta, carpeta);
            case CANCELACIONES_Y_REEMBOLSOS:
                return cancelaciones(est, generador, carpeta);
            case PUNTOS:
                return puntos(est, generador, carpeta);
            default:
                throw new IllegalArgumentException("Reporte sin implementar: " + tipo);
        }
    }

    /* Genera los 7 reportes en el mismo formato. */
    public List<Path> generarTodos(CreadorExportador generador, LocalDate desde, LocalDate hasta,
                                   Path carpeta) throws IOException {
        List<Path> archivos = new ArrayList<>();
        for (TipoReporte tipo : TipoReporte.values()) {
            archivos.add(generar(tipo, generador, desde, hasta, carpeta));
        }
        return archivos;
    }

    private Path ventas(CalcularEstadisticas est, CreadorExportador generador,
                        LocalDate desde, LocalDate hasta, Path carpeta) throws IOException {
        CalcularEstadisticas.VentasPeriodo v = est.ventasPorPeriodo(desde, hasta);
        List<String> filas = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> e : v.getComprasPorDia().entrySet()) {
            filas.add(DIA.format(e.getKey()) + " - " + e.getValue() + " compras - "
                    + Formato.dinero(v.getIngresosPorDia().get(e.getKey())));
        }
        filas.add("Compras totales - " + v.getTotalCompras());
        filas.add("Promedio por compra - " + Formato.dinero(v.getPromedioPorCompra()));
        filas.add("Total de ingresos - " + Formato.dinero(v.getTotalIngresos()));
        return generador.generar("Ventas por periodo", periodo(desde, hasta), filas,
                v.getTotalIngresos(), carpeta);
    }

    /*
     Reporte del ejemplo del enunciado: una fila por película con sus entradas y lo cobrado,
     más "Total entradas" y "Total ventas". Respeta el rango de fechas.
    */
    private Path entradas(CalcularEstadisticas est, CreadorExportador generador,
                          LocalDate desde, LocalDate hasta, Path carpeta) throws IOException {
        CalcularEstadisticas.VentasPorPelicula r = est.ventasPorPelicula(desde, hasta);
        List<String> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> e : r.getEntradasPorPelicula().entrySet()) {
            filas.add(e.getKey() + " - " + e.getValue() + " entradas - "
                    + Formato.dinero(r.getIngresos(e.getKey())));
        }
        filas.add("Total entradas - " + r.getTotalEntradas());
        filas.add("Total ventas - " + Formato.dinero(r.getTotalVentas()));
        return generador.generar("Entradas por pelicula", periodo(desde, hasta), filas,
                r.getTotalVentas(), carpeta);
    }

    private Path ocupacion(CalcularEstadisticas est, CreadorExportador generador,
                           Path carpeta) throws IOException {
        CalcularEstadisticas.OcupacionSalas oc = est.ocupacionSalas();
        List<String> filas = new ArrayList<>();
        int ofrecidos = 0;
        int ocupados = 0;
        filas.add("POR TIPO DE SALA");
        for (CalcularEstadisticas.Ocupacion o : oc.getPorTipo().values()) {
            filas.add(linea(o));
            ofrecidos += o.getAsientosOfrecidos();
            ocupados += o.getAsientosOcupados();
        }
        filas.add("POR SALA");
        for (CalcularEstadisticas.Ocupacion o : oc.getPorSala().values()) {
            filas.add(linea(o));
        }
        double global = ofrecidos == 0 ? 0 : ocupados * 100.0 / ofrecidos;
        filas.add("Ocupacion global - " + String.format(LOCAL, "%.1f %%", global));
        return generador.generar("Ocupacion de salas", corte(), filas, global, carpeta);
    }

    private Path productos(CalcularEstadisticas est, CreadorExportador generador,
                           Path carpeta) throws IOException {
        CalcularEstadisticas.ProductosVendidos p = est.productosVendidos();
        List<String> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> e : p.getProductos().entrySet()) {
            filas.add("Producto " + e.getKey() + " - " + e.getValue() + " unidades - "
                    + Formato.dinero(p.getValor(e.getKey())));
        }
        for (Map.Entry<String, Integer> e : p.getCombos().entrySet()) {
            filas.add("Combo " + e.getKey() + " - " + e.getValue() + " unidades - "
                    + Formato.dinero(p.getValor(e.getKey())));
        }
        filas.add("Total de productos - " + p.getTotalProductos());
        filas.add("Total de combos - " + p.getTotalCombos());
        filas.add("Valor total vendido - " + Formato.dinero(p.getValorTotal()));
        return generador.generar("Productos y combos vendidos", corte(), filas, p.getValorTotal(), carpeta);
    }

    private Path recargas(CalcularEstadisticas est, CreadorExportador generador,
                          LocalDate desde, LocalDate hasta, Path carpeta) throws IOException {
        CalcularEstadisticas.Recargas r = est.recargas(desde, hasta);
        List<String> filas = new ArrayList<>();
        for (Map.Entry<String, Double> e : r.getTotalPorCliente().entrySet()) {
            filas.add(e.getKey() + " - " + Formato.dinero(e.getValue()));
        }
        filas.add("Recargas realizadas - " + r.getCantidad());
        filas.add("Promedio por recarga - " + Formato.dinero(r.getPromedio()));
        filas.add("Total recargado - " + Formato.dinero(r.getTotal()));
        return generador.generar("Recargas de tarjetas", periodo(desde, hasta), filas, r.getTotal(), carpeta);
    }

    private Path cancelaciones(CalcularEstadisticas est, CreadorExportador generador,
                               Path carpeta) throws IOException {
        CalcularEstadisticas.Cancelaciones c = est.cancelacionesYReembolsos();
        List<String> filas = new ArrayList<>();
        for (Compra compra : c.getCompras()) {
            String id = compra.getId().substring(0, Math.min(8, compra.getId().length()));
            filas.add(id + " - " + compra.getCliente().getNombre()
                    + " - " + compra.getFuncion().getPelicula().getTitulo()
                    + " - " + compra.getEstado()
                    + " - reembolso " + Formato.dinero(compra.getMontoReembolsado()));
        }
        filas.add("Compras canceladas o reembolsadas - " + c.getCantidadCompras());
        filas.add("Funciones canceladas por el cine - " + c.getFuncionesCanceladas());
        filas.add("Total reembolsado - " + Formato.dinero(c.getTotalReembolsado()));
        return generador.generar("Cancelaciones y reembolsos", corte(), filas, c.getTotalReembolsado(), carpeta);
    }

    private Path puntos(CalcularEstadisticas est, CreadorExportador generador,
                        Path carpeta) throws IOException {
        CalcularEstadisticas.ResumenPuntos p = est.puntos();
        List<String> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> e : p.getAcumuladosPorCliente().entrySet()) {
            String nombre = e.getKey();
            filas.add(nombre + " - acumulados " + e.getValue()
                    + " - redimidos " + p.getRedimidosPorCliente().get(nombre)
                    + " - vigentes " + p.getVigentesPorCliente().get(nombre));
        }
        filas.add("Total redimidos - " + p.getTotalRedimidos());
        filas.add("Total vigentes - " + p.getTotalVigentes());
        filas.add("Total de puntos acumulados - " + p.getTotalAcumulados());
        return generador.generar("Puntos acumulados y redimidos", corte(), filas, p.getTotalAcumulados(), carpeta);
    }

    private static String linea(CalcularEstadisticas.Ocupacion o) {
        return o.getNombre() + " - " + o.getFunciones() + " funciones - "
                + o.getAsientosOcupados() + " de " + o.getAsientosOfrecidos() + " asientos - "
                + String.format(LOCAL, "%.1f %%", o.getPorcentaje())
                + " - promedio " + String.format(LOCAL, "%.1f", o.getPromedioOcupadosPorFuncion())
                + " por funcion";
    }

    private static String periodo(LocalDate desde, LocalDate hasta) {
        return DIA.format(desde) + " - " + DIA.format(hasta);
    }

    private static String corte() {
        return "Al " + Formato.fechaHora(LocalDateTime.now());
    }
}