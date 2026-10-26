package com.uniquindio.proyectofinalp2.cinemauq.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.TarjetaVirtual;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Usuario;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaAccesible;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaGeneral;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory2D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory3D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory4DX;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.facade.CompraFacade;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.proxy.TarjetaProxy;

public class CineServicio {
    private final ConfiguracionCine config = ConfiguracionCine.getInstancia();
    private final CompraFacade fachada = new CompraFacade(config);

    private final Administrador administrador = new Administrador("A1", "Carlos Ruiz", "admin@cinemauq.com");
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<String, TarjetaVirtual> tarjetas = new HashMap<>();
    private final List<Pelicula> peliculas = new ArrayList<>();
    private final List<Funcion> funciones = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();
    private final List<Combo> combos = new ArrayList<>();
    private final List<Promocion> promociones = new ArrayList<>();
    private final List<Compra> compras = new ArrayList<>();
    private int consecutivoCliente = 0;

    public CineServicio() {
        cargarDatosIniciales();
    }

        public Optional<Usuario> autenticar(String correo) {
        if (correo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usuarios.get(normalizar(correo)));
    }

        public Cliente registrarCliente(String nombre, String correo) {
        if (correo == null || !correo.contains("@")) {
            throw new IllegalArgumentException("Escribe un correo válido");
        }
        if (usuarios.containsKey(normalizar(correo))) {
            throw new IllegalStateException("Ya existe una cuenta con ese correo");
        }
        consecutivoCliente++;
        Cliente cliente = new Cliente("C" + consecutivoCliente, nombre.trim(), normalizar(correo));
        usuarios.put(normalizar(correo), cliente);
        tarjetas.put(cliente.getId(), new TarjetaVirtual("T" + consecutivoCliente, cliente));
        return cliente;
    }

        public Tarjeta getTarjeta(Cliente cliente) {
        TarjetaVirtual real = tarjetas.get(cliente.getId());
        if (real == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual");
        }
        return new TarjetaProxy(real, cliente);
    }

    public Administrador getAdministrador() {
        return administrador;
    }

        public List<Pelicula> getPeliculasEnCartelera() {
        return peliculas.stream()
                .filter(p -> !getFuncionesDe(p).isEmpty())
                .collect(Collectors.toList());
    }

    public List<Funcion> getFuncionesDe(Pelicula pelicula) {
        LocalDateTime ahora = LocalDateTime.now();
        return funciones.stream()
                .filter(f -> f.getPelicula().getId().equals(pelicula.getId()))
                .filter(f -> !f.estaCancelada())
                .filter(f -> f.getFechaHora().isAfter(ahora))
                .sorted(Comparator.comparing(Funcion::getFechaHora))
                .collect(Collectors.toList());
    }

        public double precioEntrada(Funcion funcion, Asiento asiento) {
        return crearEntradaSinReservar(funcion, asiento).getPrecio();
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public List<Combo> getCombos() {
        return Collections.unmodifiableList(combos);
    }

    public List<Promocion> getPromociones() {
        return Collections.unmodifiableList(promociones);
    }

        public Compra simularCompra(Cliente cliente, Funcion funcion, List<Asiento> asientos,
                                List<Comprable> confiteria, List<Promocion> promocionesAplicadas) {
        Compra.Builder builder = new Compra.Builder(config)
                .cliente(cliente)
                .funcion(funcion)
                .combo(armarConfiteria(confiteria));
        for (Asiento a : asientos) {
            builder.entrada(crearEntradaSinReservar(funcion, a));
        }
        for (Promocion p : promocionesAplicadas) {
            builder.promocion(p);
        }
        return builder.build();
    }

        public Compra comprar(Cliente cliente, Funcion funcion, List<Asiento> asientos,
                          List<Comprable> confiteria, List<Promocion> promocionesAplicadas) {
        Compra compra = fachada.comprar(getTarjeta(cliente), funcion, asientos,
                armarConfiteria(confiteria), promocionesAplicadas, 0);
        compras.add(compra);
        return compra;
    }

    public List<Compra> getComprasDe(Cliente cliente) {
        return compras.stream()
                .filter(c -> c.getCliente().getId().equals(cliente.getId()))
                .collect(Collectors.toList());
    }

    public int puntosPorCompra(double total) {
        return config.calcularPuntos(total);
    }

    public ConfiguracionCine getConfig() {
        return config;
    }

        // Compra admite un solo Combo: se agrupa todo en un combo contenedor sin descuento.
    private Combo armarConfiteria(List<Comprable> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new Combo("CONF-" + System.nanoTime(), "Confitería", items, 0.0);
    }

    private Entrada crearEntradaSinReservar(Funcion funcion, Asiento asiento) {
        return asiento.esAccesible()
                ? new EntradaAccesible(funcion, asiento)
                : new EntradaGeneral(funcion, asiento);
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase();
    }

    private void cargarDatosIniciales() {
        usuarios.put(administrador.getCorreo(), administrador);

        Cliente ana = registrarCliente("Ana Gómez", "ana@correo.com");
        Cliente luis = registrarCliente("Luis Pérez", "luis@correo.com");
        new TarjetaProxy(tarjetas.get(ana.getId()), administrador).recargar(150000);
        new TarjetaProxy(tarjetas.get(luis.getId()), administrador).recargar(20000);

        Sala sala1 = new SalaFactory2D().crearSala("S1", "Sala 1");
        Sala sala2 = new SalaFactory3D().crearSala("S2", "Sala 2");
        Sala sala3 = new SalaFactory4DX().crearSala("S3", "Sala 3");

        Pelicula interestelar = new Pelicula("P1", "Interestelar", 169);
        Pelicula avatar = new Pelicula("P2", "Avatar: El camino del agua", 192);
        Pelicula dune = new Pelicula("P3", "Dune: Parte dos", 166);
        Pelicula horizons = new Pelicula("P4", "Beyond Horizons", 128);
        Pelicula echoes = new Pelicula("P5", "Echoes", 104);
        Pelicula future = new Pelicula("P6", "Future We Build", 117);
        peliculas.add(interestelar);
        peliculas.add(avatar);
        peliculas.add(dune);
        peliculas.add(horizons);
        peliculas.add(echoes);
        peliculas.add(future);

        agregarFuncion("F1", interestelar, sala1, 1, 19, 0, 15000);
        agregarFuncion("F2", interestelar, sala2, 1, 21, 30, 15000);
        agregarFuncion("F3", interestelar, sala1, 2, 16, 0, 15000);
        agregarFuncion("F4", avatar, sala2, 1, 18, 0, 16000);
        agregarFuncion("F5", avatar, sala3, 2, 20, 0, 16000);
        agregarFuncion("F6", dune, sala1, 1, 14, 30, 14000);
        agregarFuncion("F7", dune, sala3, 3, 19, 30, 16000);
        agregarFuncion("F8", horizons, sala1, 2, 18, 15, 14000);
        agregarFuncion("F9", horizons, sala2, 3, 20, 45, 15000);
        agregarFuncion("F10", echoes, sala1, 1, 17, 0, 13000);
        agregarFuncion("F11", future, sala2, 2, 19, 0, 15000);
        agregarFuncion("F12", future, sala3, 4, 21, 0, 16000);

        ocuparAsientosDeEjemplo();

        Producto palomitasM = new Producto("PR1", "Palomitas medianas", 12000);
        Producto palomitasG = new Producto("PR2", "Palomitas grandes", 16000);
        Producto gaseosa = new Producto("PR3", "Gaseosa", 8000);
        Producto perro = new Producto("PR4", "Perro caliente", 11000);
        Producto nachos = new Producto("PR5", "Nachos con queso", 10000);
        Producto chocolatina = new Producto("PR6", "Chocolatina", 5000);
        Producto agua = new Producto("PR7", "Agua", 4000);
        productos.add(palomitasM);
        productos.add(palomitasG);
        productos.add(gaseosa);
        productos.add(perro);
        productos.add(nachos);
        productos.add(chocolatina);
        productos.add(agua);

        Combo personal = new Combo("C1", "Combo Personal", List.of(palomitasM, gaseosa), 0.10);
        Combo pareja = new Combo("C2", "Combo Pareja", List.of(palomitasG, gaseosa, gaseosa), 0.15);
        Combo familiar = new Combo("C3", "Combo Familiar", List.of(pareja, personal, nachos), 0.20);
        combos.add(personal);
        combos.add(pareja);
        combos.add(familiar);

        promociones.add(new Promocion("Estudiante UQ", 0.10));
        promociones.add(new Promocion("Martes de cine", 0.15));
    }

    private void agregarFuncion(String id, Pelicula pelicula, Sala sala,
                                int diasDesdeHoy, int hora, int minuto, double precioBase) {
        LocalDateTime fechaHora = LocalDate.now().plusDays(diasDesdeHoy).atTime(hora, minuto);
        funciones.add(new Funcion(id, pelicula, sala, fechaHora, precioBase));
    }

        private void ocuparAsientosDeEjemplo() {
        Random azar = new Random(7);
        for (Funcion f : funciones) {
            for (Asiento a : f.getSala().getAsientos()) {
                if (azar.nextDouble() < 0.20) {
                    f.ocupar(a);
                }
            }
        }
    }
}