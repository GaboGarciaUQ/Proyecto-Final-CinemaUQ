package com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Movimiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.TipoMovimiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoEntrada;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.PuntosAcumulados;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.ServicioPuntos;

/*
 Calcula las estadísticas de operación. No importa nada de JavaFX ni de la interfaz:
 recibe los datos y devuelve Map y objetos de resultado listos para gráficos y reportes.
 Una venta es una compra en estado PAGADA: las canceladas y reembolsadas no cuentan.
 Trabaja sobre una copia de las listas (foto del momento en que se crea).
*/
public class CalcularEstadisticas {

    // Nombre del combo contenedor con el que CineServicio agrupa la confitería de una compra.
    private static final String CONTENEDOR_CONFITERIA = "Confitería";

    private final List<Compra> compras;
    private final List<Funcion> funciones;
    private final Map<Cliente, List<Movimiento>> movimientos;
    private final List<Cliente> clientes;
    private final ServicioPuntos servicioPuntos;

    public CalcularEstadisticas(List<Compra> compras, List<Funcion> funciones) {
        this(compras, funciones, new LinkedHashMap<Cliente, List<Movimiento>>(),
                new ArrayList<Cliente>(), null);
    }

    public CalcularEstadisticas(List<Compra> compras, List<Funcion> funciones,
                                Map<Cliente, List<Movimiento>> movimientos,
                                List<Cliente> clientes, ServicioPuntos servicioPuntos) {
        this.compras = new ArrayList<>(Objects.requireNonNull(compras, "Las compras son obligatorias"));
        this.funciones = new ArrayList<>(Objects.requireNonNull(funciones, "Las funciones son obligatorias"));
        this.movimientos = new LinkedHashMap<>(Objects.requireNonNull(movimientos, "Los movimientos son obligatorios"));
        this.clientes = new ArrayList<>(Objects.requireNonNull(clientes, "Los clientes son obligatorios"));
        this.servicioPuntos = servicioPuntos;
    }

    /* Consulta 1: ventas por día en un periodo (ambos extremos incluidos). */
    public VentasPeriodo ventasPorPeriodo(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Map<LocalDate, Integer> cantidad = new LinkedHashMap<>();
        Map<LocalDate, Double> ingresos = new LinkedHashMap<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            cantidad.put(dia, 0);
            ingresos.put(dia, 0.0);
        }
        for (Compra compra : compras) {
            if (compra.getEstado() != EstadoCompra.PAGADA) {
                continue;
            }
            LocalDate dia = compra.getFecha().toLocalDate();
            if (!cantidad.containsKey(dia)) {
                continue;
            }
            cantidad.merge(dia, 1, Integer::sum);
            ingresos.merge(dia, compra.getTotal(), Double::sum);
        }
        return new VentasPeriodo(cantidad, ingresos);
    }

    /* Consulta 2: entradas vendidas por película y por función. */
    public EntradasPorPelicula entradasPorPelicula() {
        Map<String, Integer> entradas = new LinkedHashMap<>();
        Map<String, Integer> cantidadFunciones = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> porFuncion = new LinkedHashMap<>();

        for (Funcion funcion : funciones) {
            String titulo = funcion.getPelicula().getTitulo();
            entradas.putIfAbsent(titulo, 0);
            cantidadFunciones.putIfAbsent(titulo, 0);
            porFuncion.computeIfAbsent(titulo, k -> new LinkedHashMap<>());
            if (!funcion.estaCancelada()) {
                cantidadFunciones.merge(titulo, 1, Integer::sum);
                porFuncion.get(titulo).put(funcion.getId(), 0);
            }
        }

        for (Compra compra : compras) {
            if (compra.getEstado() != EstadoCompra.PAGADA) {
                continue;
            }
            String titulo = compra.getFuncion().getPelicula().getTitulo();
            int vendidas = 0;
            for (Entrada entrada : compra.getEntradas()) {
                if (entrada.getEstado() != EstadoEntrada.ANULADA) {
                    vendidas++;
                }
            }
            entradas.merge(titulo, vendidas, Integer::sum);
            porFuncion.computeIfAbsent(titulo, k -> new LinkedHashMap<>())
                    .merge(compra.getFuncion().getId(), vendidas, Integer::sum);
        }
        return new EntradasPorPelicula(ordenarDescendente(entradas), cantidadFunciones, porFuncion);
    }

    /* Consulta 3: ocupación de asientos por tipo de sala (2D, 3D, 4DX) y por sala. */
    public OcupacionSalas ocupacionSalas() {
        Map<String, int[]> tipos = new LinkedHashMap<>();
        Map<String, int[]> salas = new LinkedHashMap<>();
        for (Funcion funcion : funciones) {
            if (funcion.estaCancelada()) {
                continue;
            }
            int[] tipo = tipos.computeIfAbsent(funcion.getSala().getTarifa().getNombre(), k -> new int[3]);
            tipo[0]++;
            tipo[1] += funcion.getCapacidad();
            tipo[2] += funcion.getCantidadOcupados();

            int[] sala = salas.computeIfAbsent(funcion.getSala().getNombre(), k -> new int[3]);
            sala[0]++;
            sala[1] += funcion.getCapacidad();
            sala[2] += funcion.getCantidadOcupados();
        }
        return new OcupacionSalas(convertir(tipos), convertir(salas));
    }

    /*
     Consulta 4: productos y combos vendidos en compras pagadas.
     Un combo se cuenta como combo (no se desarma en sus productos). El valor es el
     precio de lista: lo canjeado con puntos también cuenta como vendido.
    */
    public ProductosVendidos productosVendidos() {
        Map<String, Integer> productos = new LinkedHashMap<>();
        Map<String, Integer> combos = new LinkedHashMap<>();
        Map<String, Double> valores = new LinkedHashMap<>();
        for (Compra compra : compras) {
            if (compra.getEstado() != EstadoCompra.PAGADA || compra.getCombo() == null) {
                continue;
            }
            Combo combo = compra.getCombo();
            if (CONTENEDOR_CONFITERIA.equals(combo.getNombre())) {
                for (Comprable item : combo.getComponentes()) {
                    contar(item, productos, combos, valores);
                }
            } else {
                contar(combo, productos, combos, valores);
            }
        }
        return new ProductosVendidos(ordenarDescendente(productos), ordenarDescendente(combos), valores);
    }

    /* Consulta 5: recargas de tarjetas en un periodo (ambos extremos incluidos). */
    public Recargas recargas(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        int cantidad = 0;
        double total = 0;
        Map<String, Double> porCliente = new LinkedHashMap<>();
        for (Map.Entry<Cliente, List<Movimiento>> e : movimientos.entrySet()) {
            for (Movimiento m : e.getValue()) {
                LocalDate dia = m.getFecha().toLocalDate();
                if (m.getTipo() != TipoMovimiento.RECARGA || dia.isBefore(desde) || dia.isAfter(hasta)) {
                    continue;
                }
                cantidad++;
                total += m.getMonto();
                porCliente.merge(e.getKey().getNombre(), m.getMonto(), Double::sum);
            }
        }
        return new Recargas(cantidad, total, porCliente);
    }

    /* Consulta 6: compras canceladas o reembolsadas y funciones canceladas por el cine. */
    public Cancelaciones cancelacionesYReembolsos() {
        List<Compra> canceladas = new ArrayList<>();
        Map<String, Integer> porPelicula = new LinkedHashMap<>();
        double reembolsado = 0;
        for (Compra compra : compras) {
            if (compra.getEstado() != EstadoCompra.REEMBOLSADA && compra.getEstado() != EstadoCompra.CANCELADA) {
                continue;
            }
            canceladas.add(compra);
            reembolsado += compra.getMontoReembolsado();
            porPelicula.merge(compra.getFuncion().getPelicula().getTitulo(), 1, Integer::sum);
        }
        int funcionesCanceladas = 0;
        for (Funcion funcion : funciones) {
            if (funcion.estaCancelada()) {
                funcionesCanceladas++;
            }
        }
        return new Cancelaciones(canceladas, funcionesCanceladas, reembolsado, ordenarDescendente(porPelicula));
    }

    /*
     Consulta 7: puntos por cliente. Los acumulados son netos: si una compra se reembolsó,
     sus puntos ya no figuran. Vacío si no se entregó un ServicioPuntos.
    */
    public ResumenPuntos puntos() {
        Map<String, Integer> acumulados = new LinkedHashMap<>();
        Map<String, Integer> redimidos = new LinkedHashMap<>();
        Map<String, Integer> vigentes = new LinkedHashMap<>();
        if (servicioPuntos != null) {
            for (Cliente cliente : clientes) {
                int ganados = 0;
                for (PuntosAcumulados lote : servicioPuntos.getHistorial(cliente)) {
                    ganados += lote.getPuntos();
                }
                acumulados.put(cliente.getNombre(), ganados);
                redimidos.put(cliente.getNombre(), servicioPuntos.getPuntosRedimidos(cliente));
                vigentes.put(cliente.getNombre(), servicioPuntos.getPuntosVigentes(cliente));
            }
        }
        return new ResumenPuntos(acumulados, redimidos, vigentes);
    }

    /*
     Consulta 2b: entradas vendidas y dinero por película en un periodo (ambos extremos incluidos).
     Es el reporte del ejemplo del enunciado: Película, Entradas, Total. Lo cobrado de cada compra
     se atribuye a la película de su función (incluye confitería e IVA), así que la suma de las
     películas coincide con el total de ventas del periodo.
    */
    public VentasPorPelicula ventasPorPelicula(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Map<String, Integer> entradas = new LinkedHashMap<>();
        Map<String, Double> ingresos = new LinkedHashMap<>();
        for (Funcion funcion : funciones) {
            entradas.putIfAbsent(funcion.getPelicula().getTitulo(), 0);
            ingresos.putIfAbsent(funcion.getPelicula().getTitulo(), 0.0);
        }
        for (Compra compra : compras) {
            if (compra.getEstado() != EstadoCompra.PAGADA) {
                continue;
            }
            LocalDate dia = compra.getFecha().toLocalDate();
            if (dia.isBefore(desde) || dia.isAfter(hasta)) {
                continue;
            }
            String titulo = compra.getFuncion().getPelicula().getTitulo();
            int vendidas = 0;
            for (Entrada entrada : compra.getEntradas()) {
                if (entrada.getEstado() != EstadoEntrada.ANULADA) {
                    vendidas++;
                }
            }
            entradas.merge(titulo, vendidas, Integer::sum);
            ingresos.merge(titulo, compra.getTotal(), Double::sum);
        }
        return new VentasPorPelicula(ordenarDescendente(entradas), ingresos);
    }

    // ---------------------------------------------------------------- apoyo

    private static void validarPeriodo(LocalDate desde, LocalDate hasta) {
        Objects.requireNonNull(desde, "La fecha inicial es obligatoria");
        Objects.requireNonNull(hasta, "La fecha final es obligatoria");
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
    }

    private static void contar(Comprable item, Map<String, Integer> productos,
                               Map<String, Integer> combos, Map<String, Double> valores) {
        Map<String, Integer> destino = item instanceof Combo ? combos : productos;
        destino.merge(item.getNombre(), 1, Integer::sum);
        valores.merge(item.getNombre(), item.getPrecio(), Double::sum);
    }

    private static Map<String, Ocupacion> convertir(Map<String, int[]> datos) {
        Map<String, Ocupacion> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : datos.entrySet()) {
            resultado.put(e.getKey(),
                    new Ocupacion(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]));
        }
        return resultado;
    }

    private static Map<String, Integer> ordenarDescendente(Map<String, Integer> datos) {
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(datos.entrySet());
        lista.sort((x, y) -> {
            int porValor = Integer.compare(y.getValue(), x.getValue());
            return porValor != 0 ? porValor : x.getKey().compareTo(y.getKey());
        });
        Map<String, Integer> ordenado = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : lista) {
            ordenado.put(e.getKey(), e.getValue());
        }
        return ordenado;
    }

    private static int sumar(Map<String, Integer> datos) {
        int total = 0;
        for (int n : datos.values()) {
            total += n;
        }
        return total;
    }

    // ---------------------------------------------------------------- resultados

    public static final class VentasPeriodo {
        private final Map<LocalDate, Integer> comprasPorDia;
        private final Map<LocalDate, Double> ingresosPorDia;

        private VentasPeriodo(Map<LocalDate, Integer> comprasPorDia, Map<LocalDate, Double> ingresosPorDia) {
            this.comprasPorDia = comprasPorDia;
            this.ingresosPorDia = ingresosPorDia;
        }

        public Map<LocalDate, Integer> getComprasPorDia() {
            return Collections.unmodifiableMap(comprasPorDia);
        }

        public Map<LocalDate, Double> getIngresosPorDia() {
            return Collections.unmodifiableMap(ingresosPorDia);
        }

        public int getTotalCompras() {
            int total = 0;
            for (int n : comprasPorDia.values()) {
                total += n;
            }
            return total;
        }

        public double getTotalIngresos() {
            double total = 0;
            for (double v : ingresosPorDia.values()) {
                total += v;
            }
            return total;
        }

        public double getPromedioPorCompra() {
            int cantidad = getTotalCompras();
            return cantidad == 0 ? 0 : getTotalIngresos() / cantidad;
        }
    }

    public static final class EntradasPorPelicula {
        private final Map<String, Integer> entradas;
        private final Map<String, Integer> funciones;
        private final Map<String, Map<String, Integer>> entradasPorFuncion;

        private EntradasPorPelicula(Map<String, Integer> entradas, Map<String, Integer> funciones,
                                    Map<String, Map<String, Integer>> entradasPorFuncion) {
            this.entradas = entradas;
            this.funciones = funciones;
            this.entradasPorFuncion = entradasPorFuncion;
        }

        /* Título -> entradas vendidas, de mayor a menor. */
        public Map<String, Integer> getEntradasPorPelicula() {
            return Collections.unmodifiableMap(entradas);
        }

        /* Título -> funciones programadas (sin contar las canceladas). */
        public Map<String, Integer> getFuncionesPorPelicula() {
            return Collections.unmodifiableMap(funciones);
        }

        /* Id de función -> entradas vendidas, para una película. */
        public Map<String, Integer> getEntradasPorFuncion(String titulo) {
            Map<String, Integer> datos = entradasPorFuncion.get(titulo);
            return datos == null ? Collections.<String, Integer>emptyMap() : Collections.unmodifiableMap(datos);
        }

        public int getTotalEntradas() {
            return sumar(entradas);
        }

        /* Vacío si todavía no se vendió ninguna entrada. */
        public Optional<String> getPeliculaMasVendida() {
            for (Map.Entry<String, Integer> e : entradas.entrySet()) {
                return e.getValue() > 0 ? Optional.of(e.getKey()) : Optional.<String>empty();
            }
            return Optional.empty();
        }
    }

    /* Ocupación de un grupo de funciones (un tipo de sala o una sala). */
    public static final class Ocupacion {
        private final String nombre;
        private final int funciones;
        private final int asientosOfrecidos;
        private final int asientosOcupados;

        private Ocupacion(String nombre, int funciones, int asientosOfrecidos, int asientosOcupados) {
            this.nombre = nombre;
            this.funciones = funciones;
            this.asientosOfrecidos = asientosOfrecidos;
            this.asientosOcupados = asientosOcupados;
        }

        public String getNombre() {
            return nombre;
        }

        public int getFunciones() {
            return funciones;
        }

        /* Suma de la capacidad de todas sus funciones. */
        public int getAsientosOfrecidos() {
            return asientosOfrecidos;
        }

        public int getAsientosOcupados() {
            return asientosOcupados;
        }

        public double getPorcentaje() {
            return asientosOfrecidos == 0 ? 0 : asientosOcupados * 100.0 / asientosOfrecidos;
        }

        public double getPromedioOcupadosPorFuncion() {
            return funciones == 0 ? 0 : (double) asientosOcupados / funciones;
        }
    }

    public static final class OcupacionSalas {
        private final Map<String, Ocupacion> porTipo;
        private final Map<String, Ocupacion> porSala;

        private OcupacionSalas(Map<String, Ocupacion> porTipo, Map<String, Ocupacion> porSala) {
            this.porTipo = porTipo;
            this.porSala = porSala;
        }

        public Map<String, Ocupacion> getPorTipo() {
            return Collections.unmodifiableMap(porTipo);
        }

        public Map<String, Ocupacion> getPorSala() {
            return Collections.unmodifiableMap(porSala);
        }

        /* Tipo de sala -> porcentaje: es el Map que recibe el gráfico. */
        public Map<String, Double> getPorcentajesPorTipo() {
            return porcentajes(porTipo);
        }

        public Map<String, Double> getPorcentajesPorSala() {
            return porcentajes(porSala);
        }

        private static Map<String, Double> porcentajes(Map<String, Ocupacion> datos) {
            Map<String, Double> resultado = new LinkedHashMap<>();
            for (Map.Entry<String, Ocupacion> e : datos.entrySet()) {
                resultado.put(e.getKey(), e.getValue().getPorcentaje());
            }
            return resultado;
        }
    }

    public static final class ProductosVendidos {
        private final Map<String, Integer> productos;
        private final Map<String, Integer> combos;
        private final Map<String, Double> valores;

        private ProductosVendidos(Map<String, Integer> productos, Map<String, Integer> combos,
                                  Map<String, Double> valores) {
            this.productos = productos;
            this.combos = combos;
            this.valores = valores;
        }

        /* Nombre -> unidades vendidas, de mayor a menor. */
        public Map<String, Integer> getProductos() {
            return Collections.unmodifiableMap(productos);
        }

        public Map<String, Integer> getCombos() {
            return Collections.unmodifiableMap(combos);
        }

        /* Valor a precio de lista de lo vendido de ese producto o combo. */
        public double getValor(String nombre) {
            return valores.getOrDefault(nombre, 0.0);
        }

        public int getTotalProductos() {
            return sumar(productos);
        }

        public int getTotalCombos() {
            return sumar(combos);
        }

        public double getValorTotal() {
            double total = 0;
            for (double v : valores.values()) {
                total += v;
            }
            return total;
        }
    }

    public static final class Recargas {
        private final int cantidad;
        private final double total;
        private final Map<String, Double> porCliente;

        private Recargas(int cantidad, double total, Map<String, Double> porCliente) {
            this.cantidad = cantidad;
            this.total = total;
            this.porCliente = porCliente;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getTotal() {
            return total;
        }

        public double getPromedio() {
            return cantidad == 0 ? 0 : total / cantidad;
        }

        /* Nombre del cliente -> total recargado. */
        public Map<String, Double> getTotalPorCliente() {
            return Collections.unmodifiableMap(porCliente);
        }
    }

    public static final class Cancelaciones {
        private final List<Compra> compras;
        private final int funcionesCanceladas;
        private final double totalReembolsado;
        private final Map<String, Integer> porPelicula;

        private Cancelaciones(List<Compra> compras, int funcionesCanceladas, double totalReembolsado,
                              Map<String, Integer> porPelicula) {
            this.compras = compras;
            this.funcionesCanceladas = funcionesCanceladas;
            this.totalReembolsado = totalReembolsado;
            this.porPelicula = porPelicula;
        }

        public List<Compra> getCompras() {
            return Collections.unmodifiableList(compras);
        }

        public int getCantidadCompras() {
            return compras.size();
        }

        public int getFuncionesCanceladas() {
            return funcionesCanceladas;
        }

        public double getTotalReembolsado() {
            return totalReembolsado;
        }

        /* Título -> compras canceladas o reembolsadas, de mayor a menor. */
        public Map<String, Integer> getPorPelicula() {
            return Collections.unmodifiableMap(porPelicula);
        }
    }

    public static final class ResumenPuntos {
        private final Map<String, Integer> acumulados;
        private final Map<String, Integer> redimidos;
        private final Map<String, Integer> vigentes;

        private ResumenPuntos(Map<String, Integer> acumulados, Map<String, Integer> redimidos,
                              Map<String, Integer> vigentes) {
            this.acumulados = acumulados;
            this.redimidos = redimidos;
            this.vigentes = vigentes;
        }

        /* Nombre del cliente -> puntos. */
        public Map<String, Integer> getAcumuladosPorCliente() {
            return Collections.unmodifiableMap(acumulados);
        }

        public Map<String, Integer> getRedimidosPorCliente() {
            return Collections.unmodifiableMap(redimidos);
        }

        public Map<String, Integer> getVigentesPorCliente() {
            return Collections.unmodifiableMap(vigentes);
        }

        public int getTotalAcumulados() {
            return sumar(acumulados);
        }

        public int getTotalRedimidos() {
            return sumar(redimidos);
        }

        public int getTotalVigentes() {
            return sumar(vigentes);
        }
    }

    public static final class VentasPorPelicula {
        private final Map<String, Integer> entradas;
        private final Map<String, Double> ingresos;

        VentasPorPelicula(Map<String, Integer> entradas, Map<String, Double> ingresos) {
            this.entradas = Collections.unmodifiableMap(new LinkedHashMap<>(entradas));
            this.ingresos = Collections.unmodifiableMap(new LinkedHashMap<>(ingresos));
        }

        // Ordenado de mayor a menor numero de entradas.
        public Map<String, Integer> getEntradasPorPelicula() {
            return entradas;
        }

        public double getIngresos(String titulo) {
            return ingresos.getOrDefault(titulo, 0.0);
        }

        public int getTotalEntradas() {
            int total = 0;
            for (int n : entradas.values()) {
                total += n;
            }
            return total;
        }

        public double getTotalVentas() {
            double total = 0;
            for (double v : ingresos.values()) {
                total += v;
            }
            return total;
        }
    }
}