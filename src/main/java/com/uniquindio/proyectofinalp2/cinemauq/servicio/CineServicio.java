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

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AccesoDenegadoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.CorreoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.TarjetaNoEncontradaException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.UsuarioDuplicadoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Movimiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.Tarjeta;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.pagos.TarjetaVirtual;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.reportes.CalcularEstadisticas;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Administrador;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Usuario;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Entrada;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaAccesible;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EntradaGeneral;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer.BandejaNotificaciones;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer.EventoCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer.ObservadorCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.observer.PublicadorCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.ConsumoPuntos;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.EstrategiaPuntos;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.PuntosAcumulados;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.PuntosBase;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos.ServicioPuntos;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.PoliticaReembolso;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.ReembolsoFuncionCancelada;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.reembolso.ReembolsoPorAnticipacion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory2D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory3D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory4DX;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.facade.CompraFacade;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.estructurales.proxy.TarjetaProxy;
import com.uniquindio.proyectofinalp2.cinemauq.util.Formato;

public class CineServicio {
    private final ConfiguracionCine config = ConfiguracionCine.getInstancia();
    private final CompraFacade fachada = new CompraFacade(config);
    private final ServicioPuntos servicioPuntos = new ServicioPuntos(config, new PuntosBase(config));
    // Política cuando cancela el cliente (100 % / 80 % / no permitido). Strategy de reembolso.
    private final PoliticaReembolso politicaCliente = new ReembolsoPorAnticipacion(config);
    // Política cuando cancela el cine: siempre se devuelve el 100 %.
    private final PoliticaReembolso politicaCine = new ReembolsoFuncionCancelada(config);
    // Observer: el servicio publica eventos sin saber quién los escucha.
    private final PublicadorCine publicador = new PublicadorCine();
    private final BandejaNotificaciones bandeja = new BandejaNotificaciones();

    private final Administrador administrador = new Administrador("A1", "Carlos Ruiz", "admin@cinemauq.com");
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<String, TarjetaVirtual> tarjetas = new HashMap<>();
    private final List<Pelicula> peliculas = new ArrayList<>();
    private final List<Sala> salas = new ArrayList<>();
    private final List<Funcion> funciones = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();
    private final List<Combo> combos = new ArrayList<>();
    private final List<Promocion> promociones = new ArrayList<>();
    private final List<Compra> compras = new ArrayList<>();
    private final ServicioAdministracion administracion;
    private final ServicioSolicitudesRecarga solicitudesRecarga;
    private int consecutivoCliente = 0;

    public CineServicio() {
        publicador.registrar(bandeja);
        cargarDatosIniciales();
        // Se crea después de cargar los datos para que sus consecutivos partan de lo ya existente.
        administracion = new ServicioAdministracion(peliculas, salas, funciones, productos,
                combos, promociones, compras, usuarios, config);
        solicitudesRecarga = new ServicioSolicitudesRecarga(this);
    }

    public Optional<Usuario> autenticar(String correo) {
        if (correo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usuarios.get(normalizar(correo)));
    }

    public Cliente registrarCliente(String nombre, String correo) {
        if (correo == null || !correo.contains("@")) {
            throw new CorreoInvalidoException();
        }
        if (usuarios.containsKey(normalizar(correo))) {
            throw new UsuarioDuplicadoException(correo);
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
            throw new TarjetaNoEncontradaException();
        }
        return new TarjetaProxy(real, cliente);
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    // Gestión del catálogo y consulta de clientes (solo administrador).
    public ServicioAdministracion getAdministracion() {
        return administracion;
    }

    // El cliente solicita recargas; el administrador las aprueba o rechaza.
    public ServicioSolicitudesRecarga getSolicitudesRecarga() {
        return solicitudesRecarga;
    }

    // Todas las películas, tengan o no funciones programadas.
    public List<Pelicula> getPeliculas() {
        return Collections.unmodifiableList(peliculas);
    }

    public List<Sala> getSalas() {
        return Collections.unmodifiableList(salas);
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

    // Vista previa sin canje.
    public Compra simularCompra(Cliente cliente, Funcion funcion, List<Asiento> asientos,
                                List<Comprable> confiteria, List<Promocion> promocionesAplicadas) {
        return simularCompra(cliente, funcion, asientos, confiteria, promocionesAplicadas,
                new ArrayList<>(), new ArrayList<>());
    }

    // Vista previa con canje: no reserva asientos ni cobra nada.
    public Compra simularCompra(Cliente cliente, Funcion funcion, List<Asiento> asientos,
                                List<Comprable> confiteria, List<Promocion> promocionesAplicadas,
                                List<Asiento> asientosCanjeados, List<Comprable> confiteriaCanjeada) {
        Compra.Builder builder = new Compra.Builder(config)
                .cliente(cliente)
                .funcion(funcion)
                .combo(armarConfiteria(confiteria));
        List<Entrada> entradas = new ArrayList<>();
        for (Asiento a : asientos) {
            Entrada e = crearEntradaSinReservar(funcion, a);
            entradas.add(e);
            builder.entrada(e);
        }
        for (Promocion p : promocionesAplicadas) {
            builder.promocion(p);
        }
        for (Asiento a : asientosCanjeados) {
            for (Entrada e : entradas) {
                if (e.getAsiento() == a) {
                    builder.canjearEntrada(e);
                }
            }
        }
        for (Comprable item : confiteriaCanjeada) {
            builder.canjearConfiteria(item);
        }
        return builder.build();
    }

    // Compra sin canje.
    public Compra comprar(Cliente cliente, Funcion funcion, List<Asiento> asientos,
                          List<Comprable> confiteria, List<Promocion> promocionesAplicadas) {
        return comprar(cliente, funcion, asientos, confiteria, promocionesAplicadas,
                new ArrayList<>(), new ArrayList<>());
    }

    /*
     Compra con canje de puntos. Los puntos se verifican ANTES de cobrar; después de pagar
     se gastan (primero los lotes que vencen antes) y se acumulan los puntos de lo que
     realmente se pagó.
    */
    public Compra comprar(Cliente cliente, Funcion funcion, List<Asiento> asientos,
                          List<Comprable> confiteria, List<Promocion> promocionesAplicadas,
                          List<Asiento> asientosCanjeados, List<Comprable> confiteriaCanjeada) {
        Compra compra = fachada.comprar(getTarjeta(cliente), funcion, asientos,
                armarConfiteria(confiteria), promocionesAplicadas,
                asientosCanjeados, confiteriaCanjeada,
                c -> {
                    if (c.getPuntosRedimidos() > 0) {
                        servicioPuntos.verificarDisponibles(cliente, c.getPuntosRedimidos());
                    }
                });
        compras.add(compra);

        int puntosGastados = compra.getPuntosRedimidos();
        if (puntosGastados > 0) {
            servicioPuntos.redimir(cliente, compra.getId(), puntosGastados);
        }
        servicioPuntos.confirmarCompra(compra);

        String aviso = "Compra " + compra.getId() + " pagada por " + Formato.dinero(compra.getTotal())
                + " para " + descripcionFuncion(compra.getFuncion()) + ".";
        if (puntosGastados > 0) {
            aviso += " Canjeaste " + puntosGastados + " puntos.";
        }
        publicador.publicar(new EventoCine(EventoCine.Tipo.COMPRA_PAGADA, cliente, aviso));
        return compra;
    }

    public List<Compra> getComprasDe(Cliente cliente) {
        return compras.stream()
                .filter(c -> c.getCliente().getId().equals(cliente.getId()))
                .collect(Collectors.toList());
    }

    // Todas las compras del cine (para reportes y estadísticas).
    public List<Compra> getCompras() {
        return Collections.unmodifiableList(compras);
    }

    // Todas las funciones, incluidas las canceladas y las pasadas (para reportes y estadísticas).
    public List<Funcion> getFunciones() {
        return Collections.unmodifiableList(funciones);
    }

    // Estadísticas con los datos de este momento.
    public CalcularEstadisticas getEstadisticas() {
        Map<Cliente, List<Movimiento>> movimientos = new LinkedHashMap<>();
        List<Cliente> clientes = new ArrayList<>();
        for (Usuario usuario : usuarios.values()) {
            if (usuario instanceof Cliente) {
                Cliente cliente = (Cliente) usuario;
                clientes.add(cliente);
                TarjetaVirtual tarjeta = tarjetas.get(cliente.getId());
                if (tarjeta != null) {
                    movimientos.put(cliente, new ArrayList<>(tarjeta.getMovimientos()));
                }
            }
        }
        // Con movimientos, clientes y puntos: sin ellos los reportes de recargas y puntos salen vacíos.
        return new CalcularEstadisticas(compras, funciones, movimientos, clientes, servicioPuntos);
    }

    /*
     Cancela una compra del cliente, devuelve el dinero a su tarjeta, le devuelve los puntos
     que gastó en esa compra y le quita los que esa compra le había dado (solo los no gastados).
     El estado (State) decide si se puede y cuánto devolver (Strategy).
    */
    public double cancelarCompra(Cliente cliente, Compra compra) {
        if (!compra.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoDenegadoException(cliente.getNombre(), "cancelar una compra ajena");
        }
        double monto = compra.cancelar(politicaCliente, LocalDateTime.now());
        if (monto > 0) {
            tarjetaDelSistema(cliente).reembolsar(monto, compra.getId());
        }
        if (compra.getEstado() == EstadoCompra.REEMBOLSADA) {
            int devueltos = servicioPuntos.devolverPuntos(compra);
            servicioPuntos.revocarPuntos(compra);
            String aviso = "Compra " + compra.getId() + " cancelada: se devolvieron "
                    + Formato.dinero(monto) + " a tu tarjeta.";
            if (devueltos > 0) {
                aviso += " También recuperaste " + devueltos + " puntos.";
            }
            publicador.publicar(new EventoCine(EventoCine.Tipo.COMPRA_REEMBOLSADA, cliente, aviso));
        }
        return monto;
    }

    /*
     El cine cancela una función: solo el administrador. Cada compra pagada de esa función
     se reembolsa al 100 %, se libera su asiento, se devuelven los puntos gastados y se
     quitan los ganados. Devuelve cuántas compras se reembolsaron.
    */
    public int cancelarFuncion(Usuario actor, Funcion funcion) {
        if (!actor.esAdministrador()) {
            throw new AccesoDenegadoException(actor.getNombre(), "cancelar una función");
        }
        if (!funciones.contains(funcion)) {
            throw new IllegalArgumentException("La función no pertenece a la cartelera del cine");
        }
        if (funcion.estaCancelada()) {
            throw new EstadoInvalidoException("La función ya está cancelada");
        }
        funcion.cancelar();

        LocalDateTime ahora = LocalDateTime.now();
        int reembolsadas = 0;
        for (Compra compra : compras) {
            if (compra.getFuncion() != funcion || compra.getEstado() != EstadoCompra.PAGADA) {
                continue;
            }
            double monto = compra.cancelar(politicaCine, ahora);
            if (monto > 0) {
                tarjetaDelSistema(compra.getCliente()).reembolsar(monto, compra.getId());
            }
            int devueltos = servicioPuntos.devolverPuntos(compra);
            servicioPuntos.revocarPuntos(compra);
            String aviso = "El cine canceló " + descripcionFuncion(funcion) + ". Tu compra "
                    + compra.getId() + " fue reembolsada: " + Formato.dinero(monto)
                    + " de vuelta en tu tarjeta.";
            if (devueltos > 0) {
                aviso += " También recuperaste " + devueltos + " puntos.";
            }
            publicador.publicar(new EventoCine(EventoCine.Tipo.FUNCION_CANCELADA,
                    compra.getCliente(), aviso));
            reembolsadas++;
        }
        return reembolsadas;
    }

    /*
     Recarga la tarjeta de un cliente. Quien exige que sea administrador es el Proxy de la tarjeta.
    */
    public void recargarTarjeta(Usuario actor, Cliente cliente, double monto) {
        TarjetaVirtual real = tarjetas.get(cliente.getId());
        if (real == null) {
            throw new TarjetaNoEncontradaException();
        }
        new TarjetaProxy(real, actor).recargar(monto);
        publicador.publicar(new EventoCine(EventoCine.Tipo.TARJETA_RECARGADA, cliente,
                "Tu tarjeta fue recargada con " + Formato.dinero(monto) + "."));
    }

    // Tarjeta de un cliente vista por un actor: el Proxy decide si puede (titular o administrador).
    public Tarjeta getTarjetaDe(Usuario actor, Cliente cliente) {
        TarjetaVirtual real = tarjetas.get(cliente.getId());
        if (real == null) {
            throw new TarjetaNoEncontradaException();
        }
        return new TarjetaProxy(real, actor);
    }

    // Solo el administrador puede bloquear una tarjeta: lo exige el Proxy.
    public void desactivarTarjeta(Usuario actor, Cliente cliente) {
        getTarjetaDe(actor, cliente).desactivar();
    }

    public void activarTarjeta(Usuario actor, Cliente cliente) {
        getTarjetaDe(actor, cliente).activar();
    }

    public List<EventoCine> getNotificaciones(Cliente cliente) {
        return bandeja.getNotificaciones(cliente);
    }

    public void registrarObservador(ObservadorCine observador) {
        publicador.registrar(observador);
    }

    public int puntosPorCompra(double total) {
        return servicioPuntos.calcularPuntos(total);
    }

    public void setEstrategiaPuntos(EstrategiaPuntos estrategia) {
        servicioPuntos.setEstrategia(estrategia);
    }

    public int getPuntosVigentes(Cliente cliente) {
        return servicioPuntos.getPuntosVigentes(cliente);
    }

    public int getPuntosRedimidos(Cliente cliente) {
        return servicioPuntos.getPuntosRedimidos(cliente);
    }

    public List<PuntosAcumulados> getHistorialPuntos(Cliente cliente) {
        return servicioPuntos.getHistorial(cliente);
    }

    public List<ConsumoPuntos> getHistorialConsumos(Cliente cliente) {
        return servicioPuntos.getHistorialConsumos(cliente);
    }

    public int getRestantes(Cliente cliente, PuntosAcumulados lote) {
        return servicioPuntos.getRestantes(cliente, lote);
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

    private String descripcionFuncion(Funcion funcion) {
        return funcion.getPelicula().getTitulo() + " (" + Formato.fechaHora(funcion.getFechaHora()) + ")";
    }

    private Entrada crearEntradaSinReservar(Funcion funcion, Asiento asiento) {
        return asiento.esAccesible()
                ? new EntradaAccesible(funcion, asiento)
                : new EntradaGeneral(funcion, asiento);
    }

    // Tarjeta del cliente actuando como administrador (el sistema): solo el sistema acredita reembolsos.
    private Tarjeta tarjetaDelSistema(Cliente cliente) {
        TarjetaVirtual real = tarjetas.get(cliente.getId());
        if (real == null) {
            throw new TarjetaNoEncontradaException();
        }
        return new TarjetaProxy(real, administrador);
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
        salas.add(sala1);
        salas.add(sala2);
        salas.add(sala3);

        Pelicula interestelar = new Pelicula("P1", "Interestelar", 169);
        Pelicula avatar = new Pelicula("P2", "Avatar: El camino del agua", 192);
        Pelicula dune = new Pelicula("P3", "Dune: Parte dos", 166);
        Pelicula clayface = new Pelicula("P4", "Clayface", 100);
        Pelicula doomsday = new Pelicula("P5", "Avengers: Doomsday", 150);
        peliculas.add(interestelar);
        peliculas.add(avatar);
        peliculas.add(dune);
        peliculas.add(clayface);
        peliculas.add(doomsday);
        interestelar.setPosterUrl("https://image.tmdb.org/t/p/original/qDSDtonrb6LfVOJXBbpZJNLjr2L.jpg");
        avatar.setPosterUrl("https://image.tmdb.org/t/p/original/vc9OVrE1xsl1jyTvhfaGb4nAgvH.jpg");
        dune.setPosterUrl("https://image.tmdb.org/t/p/original/ehO9fGqjgNCxF1HYTUUy70tIBk0.jpg");
        clayface.setPosterUrl("https://image.tmdb.org/t/p/original/iOEgit3wQbqigNYl2M3vd9TA6kb.jpg");
        doomsday.setPosterUrl("https://image.tmdb.org/t/p/original/czwC0Eq98CXZOicPzzq4iXaX494.jpg");

        agregarFuncion("F1", interestelar, sala1, 1, 19, 0, 15000);
        agregarFuncion("F2", interestelar, sala2, 1, 21, 30, 15000);
        agregarFuncion("F3", interestelar, sala1, 2, 16, 0, 15000);
        agregarFuncion("F4", avatar, sala2, 1, 18, 0, 16000);
        agregarFuncion("F5", avatar, sala3, 2, 20, 0, 16000);
        agregarFuncion("F6", dune, sala1, 1, 14, 30, 14000);
        agregarFuncion("F7", dune, sala3, 3, 19, 30, 16000);
        agregarFuncion("F8", clayface, sala1, 2, 18, 15, 14000);
        agregarFuncion("F9", clayface, sala2, 3, 20, 45, 15000);
        agregarFuncion("F10", doomsday, sala2, 1, 17, 0, 15000);
        agregarFuncion("F11", doomsday, sala3, 2, 19, 0, 16000);
        agregarFuncion("F12", doomsday, sala1, 4, 21, 0, 14000);

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