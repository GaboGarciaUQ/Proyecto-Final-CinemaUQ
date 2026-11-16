package com.uniquindio.proyectofinalp2.cinemauq.controlador;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.CalcularEstadisticas;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.CreadorPDF;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.CreadorExportador;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.factory.CreadorTXT;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.CineServicio;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.ServicioReportes;
import com.uniquindio.proyectofinalp2.cinemauq.servicio.ServicioReportes.TipoReporte;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;
import com.uniquindio.proyectofinalp2.cinemauq.util.Sesion;
import com.uniquindio.proyectofinalp2.cinemauq.util.Ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

public class AdminReportesController {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox<TipoReporte> cmbTipo;
    @FXML private ComboBox<String> cmbFormato;
    @FXML private Label lblResultado;
    @FXML private Label lblVentas;
    @FXML private Label lblEntradas;
    @FXML private Label lblLider;
    @FXML private Label lblReembolsos;
    @FXML private BarChart<String, Number> grafEntradas;
    @FXML private PieChart grafOcupacion;
    @FXML private LineChart<String, Number> grafVentas;
    @FXML private BarChart<String, Number> grafProductos;

    private final CineServicio servicio = Sesion.getInstancia().getServicio();

    @FXML
    private void initialize() {
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(29));
        Ui.texto(cmbTipo, t -> {
            String s = t.name().replace('_', ' ').toLowerCase();
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        });
        cmbTipo.setItems(FXCollections.observableArrayList(TipoReporte.values()));
        cmbTipo.setValue(TipoReporte.VENTAS_POR_PERIODO);
        cmbFormato.setItems(FXCollections.observableArrayList("TXT", "PDF"));
        cmbFormato.setValue("TXT");
        for (XYChart<String, Number> g : java.util.List.of(grafEntradas, grafVentas, grafProductos)) {
            g.setAnimated(false);
        }
        grafOcupacion.setAnimated(false);
        actualizar();
    }

    @FXML
    private void actualizar() {
        Ui.intentar(() -> {
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();
            CalcularEstadisticas est = servicio.getEstadisticas();

            CalcularEstadisticas.VentasPeriodo ventas = est.ventasPorPeriodo(desde, hasta);
            CalcularEstadisticas.EntradasPorPelicula entradas = est.entradasPorPelicula();
            lblVentas.setText(Formato.dinero(ventas.getTotalIngresos()));
            lblEntradas.setText(String.valueOf(entradas.getTotalEntradas()));
            lblLider.setText(entradas.getPeliculaMasVendida().orElse("—"));
            lblReembolsos.setText(Formato.dinero(est.cancelacionesYReembolsos().getTotalReembolsado()));

            llenarBarras(grafEntradas, entradas.getEntradasPorPelicula());
            llenarBarras(grafProductos, mezclar(est.productosVendidos()));

            grafOcupacion.setData(FXCollections.observableArrayList());
            est.ocupacionSalas().getPorcentajesPorTipo().forEach((tipo, pct) ->
                    grafOcupacion.getData().add(new PieChart.Data(tipo + " " + Math.round(pct) + "%", pct)));

            XYChart.Series<String, Number> linea = new XYChart.Series<>();
            ventas.getIngresosPorDia().forEach((dia, valor) -> linea.getData().add(new XYChart.Data<>(dia.format(DIA), valor)));
            grafVentas.getData().setAll(java.util.List.of(linea));
        });
    }

    private Map<String, Integer> mezclar(CalcularEstadisticas.ProductosVendidos p) {
        Map<String, Integer> todo = new java.util.LinkedHashMap<>(p.getProductos());
        p.getCombos().forEach((k, v) -> todo.merge(k, v, Integer::sum));
        return todo;
    }

    private void llenarBarras(BarChart<String, Number> grafico, Map<String, Integer> datos) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        datos.forEach((k, v) -> serie.getData().add(new XYChart.Data<>(k, v)));
        grafico.getData().setAll(java.util.List.of(serie));
    }

    @FXML
    private void generar() {
        CreadorExportador generador = "PDF".equals(cmbFormato.getValue()) ? new CreadorPDF() : new CreadorTXT();
        Path[] destino = new Path[1];
        boolean ok = Ui.intentar(() -> {
            try {
                destino[0] = new ServicioReportes(servicio).generar(cmbTipo.getValue(), generador,
                        dpDesde.getValue(), dpHasta.getValue(), Paths.get("reportes"));
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo guardar el reporte: " + e.getMessage(), e);
            }
        });
        if (ok) {
            lblResultado.setText("Reporte guardado en " + destino[0].toAbsolutePath());
        }
    }
}