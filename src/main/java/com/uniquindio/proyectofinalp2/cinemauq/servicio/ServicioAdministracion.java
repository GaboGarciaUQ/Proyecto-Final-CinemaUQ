package com.uniquindio.proyectofinalp2.cinemauq.servicio;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.AccesoDenegadoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Asiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Funcion;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Pelicula;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.Sala;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.cartelera.TipoAsiento;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Combo;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Comprable;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.confiteria.Producto;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Usuario;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Promocion;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory2D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory3D;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.abstractfactory.SalaFactory4DX;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

/*
 Gestión del catálogo del cine (películas, salas, funciones, productos, combos y promociones)
 y consulta de clientes. Todas las operaciones exigen un administrador.
 Trabaja sobre las mismas listas que CineServicio, que es quien la crea.
*/
public class ServicioAdministracion {

    public enum TipoSala { SALA_2D, SALA_3D, SALA_4DX }

    // Tiempo de aseo entre dos funciones de la misma sala.
    private static final int MINUTOS_ASEO = 15;

    private final List<Pelicula> peliculas;
    private final List<Sala> salas;
    private final List<Funcion> funciones;
    private final List<Producto> productos;
    private final List<Combo> combos;
    private final List<Promocion> promociones;
    private final List<Compra> compras;
    private final Map<String, Usuario> usuarios;
    private final ConfiguracionCine config;

    private int consecutivoPelicula;
    private int consecutivoSala;
    private int consecutivoFuncion;
    private int consecutivoProducto;
    private int consecutivoCombo;

    ServicioAdministracion(List<Pelicula> peliculas, List<Sala> salas, List<Funcion> funciones,
                           List<Producto> productos, List<Combo> combos, List<Promocion> promociones,
                           List<Compra> compras, Map<String, Usuario> usuarios,
                           ConfiguracionCine config) {
        this.peliculas = peliculas;
        this.salas = salas;
        this.funciones = funciones;
        this.productos = productos;
        this.combos = combos;
        this.promociones = promociones;
        this.compras = compras;
        this.usuarios = usuarios;
        this.config = config;
        consecutivoPelicula = peliculas.size();
        consecutivoSala = salas.size();
        consecutivoFuncion = funciones.size();
        consecutivoProducto = productos.size();
        consecutivoCombo = combos.size();
    }

    // ---------------------------------------------------------------- Películas

    public Pelicula crearPelicula(Usuario actor, String titulo, int duracionMinutos) {
        exigirAdministrador(actor, "crear una película");
        if (existeTitulo(titulo, null)) {
            throw new EstadoInvalidoException("Ya existe una película con ese título");
        }
        Pelicula pelicula = new Pelicula("P" + (++consecutivoPelicula), titulo, duracionMinutos);
        peliculas.add(pelicula);
        return pelicula;
    }

    public void editarPelicula(Usuario actor, Pelicula pelicula, String titulo, int duracionMinutos) {
        exigirAdministrador(actor, "editar una película");
        exigirExiste(peliculas.contains(pelicula), "La película no pertenece al cine");
        if (existeTitulo(titulo, pelicula)) {
            throw new EstadoInvalidoException("Ya existe otra película con ese título");
        }
        pelicula.setTitulo(titulo);
        pelicula.setDuracionMinutos(duracionMinutos);
    }

    public void eliminarPelicula(Usuario actor, Pelicula pelicula) {
        exigirAdministrador(actor, "eliminar una película");
        exigirExiste(peliculas.contains(pelicula), "La película no pertenece al cine");
        boolean conFunciones = funciones.stream().anyMatch(f -> f.getPelicula() == pelicula);
        if (conFunciones) {
            throw new EstadoInvalidoException("La película tiene funciones; cancélalas o elimínalas antes");
        }
        peliculas.remove(pelicula);
    }

    // -------------------------------------------------------------------- Salas

    // Abstract Factory: la fábrica decide el tipo de sala y su tarifa.
    public Sala crearSala(Usuario actor, TipoSala tipo, String nombre) {
        exigirAdministrador(actor, "crear una sala");
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La sala necesita un nombre");
        }
        SalaFactory fabrica = fabricaPara(tipo);
        Sala sala = fabrica.crearSala("S" + (++consecutivoSala), nombre.trim());
        salas.add(sala);
        return sala;
    }

    public void eliminarSala(Usuario actor, Sala sala) {
        exigirAdministrador(actor, "eliminar una sala");
        exigirExiste(salas.contains(sala), "La sala no pertenece al cine");
        if (funciones.stream().anyMatch(f -> f.getSala() == sala)) {
            throw new EstadoInvalidoException("La sala tiene funciones programadas");
        }
        salas.remove(sala);
    }

    /*
     Cambia un asiento de general a accesible o al revés. No se permite si ese asiento
     ya está ocupado en una función futura de la sala, porque cambiaría el precio de una venta hecha.
    */
    public void definirTipoAsiento(Usuario actor, Sala sala, char fila, int numero, TipoAsiento tipo) {
        exigirAdministrador(actor, "cambiar el tipo de un asiento");
        exigirExiste(sala != null && salas.contains(sala), "La sala no pertenece al cine");
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de asiento es obligatorio");
        }
        Asiento asiento = sala.getAsiento(fila, numero)
                .orElseThrow(() -> new IllegalArgumentException("El asiento no existe en la sala"));
        LocalDateTime ahora = LocalDateTime.now();
        for (Funcion f : funciones) {
            if (f.getSala() == sala && !f.estaCancelada() && f.getFechaHora().isAfter(ahora)
                    && !f.estaDisponible(asiento)) {
                throw new EstadoInvalidoException("El asiento está ocupado en una función próxima");
            }
        }
        asiento.cambiarTipo(tipo);
    }

    // ----------------------------------------------------------------- Funciones

    public Funcion programarFuncion(Usuario actor, Pelicula pelicula, Sala sala,
                                    LocalDateTime fechaHora, double precioBase) {
        exigirAdministrador(actor, "programar una función");
        exigirExiste(pelicula != null && peliculas.contains(pelicula), "La película no pertenece al cine");
        exigirExiste(sala != null && salas.contains(sala), "La sala no pertenece al cine");
        if (precioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor a cero");
        }
        validarHorario(sala, pelicula, fechaHora, null);
        Funcion funcion = new Funcion("F" + (++consecutivoFuncion), pelicula, sala, fechaHora, precioBase);
        funciones.add(funcion);
        return funcion;
    }

    /*
     Prototype: crea una función nueva a partir de otra (misma película, sala y precio)
     en otra fecha y hora. Nace sin asientos ocupados y sin cancelar.
    */
    public Funcion duplicarFuncion(Usuario actor, Funcion plantilla, LocalDateTime nuevaFechaHora) {
        exigirAdministrador(actor, "duplicar una función");
        exigirExiste(plantilla != null && funciones.contains(plantilla), "La función no pertenece al cine");
        validarHorario(plantilla.getSala(), plantilla.getPelicula(), nuevaFechaHora, null);
        Funcion copia = plantilla.clonar("F" + (++consecutivoFuncion), nuevaFechaHora);
        funciones.add(copia);
        return copia;
    }

    /*
     Cambia fecha, hora y precio base. Solo si nadie ha pagado ya esa función:
     si hay compras pagadas, lo correcto es cancelarla para que se reembolse.
    */
    public void reprogramarFuncion(Usuario actor, Funcion funcion, LocalDateTime nuevaFechaHora,
                                   double nuevoPrecioBase) {
        exigirAdministrador(actor, "reprogramar una función");
        exigirExiste(funcion != null && funciones.contains(funcion), "La función no pertenece al cine");
        if (funcion.estaCancelada()) {
            throw new EstadoInvalidoException("La función está cancelada");
        }
        boolean conPagadas = compras.stream()
                .anyMatch(c -> c.getFuncion() == funcion && c.getEstado() == EstadoCompra.PAGADA);
        if (conPagadas) {
            throw new EstadoInvalidoException("La función tiene compras pagadas; cancélala para reembolsar");
        }
        if (nuevoPrecioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor a cero");
        }
        validarHorario(funcion.getSala(), funcion.getPelicula(), nuevaFechaHora, funcion);
        funcion.reprogramar(nuevaFechaHora);
        funcion.cambiarPrecioBase(nuevoPrecioBase);
    }

    /*
     Solo se elimina una función sin ninguna compra asociada. Si ya tiene compras,
     el camino correcto es cancelarla (CineServicio.cancelarFuncion), que reembolsa.
    */
    public void eliminarFuncion(Usuario actor, Funcion funcion) {
        exigirAdministrador(actor, "eliminar una función");
        exigirExiste(funciones.contains(funcion), "La función no pertenece al cine");
        if (compras.stream().anyMatch(c -> c.getFuncion() == funcion)) {
            throw new EstadoInvalidoException("La función tiene compras; cancélala en lugar de eliminarla");
        }
        funciones.remove(funcion);
    }

    // ----------------------------------------------------------------- Productos

    public Producto crearProducto(Usuario actor, String nombre, double precio) {
        exigirAdministrador(actor, "crear un producto");
        if (existeNombreProducto(nombre, null)) {
            throw new EstadoInvalidoException("Ya existe un producto con ese nombre");
        }
        Producto producto = new Producto("PR" + (++consecutivoProducto), nombre, precio);
        productos.add(producto);
        return producto;
    }

    // Los combos que lo contienen toman el precio nuevo solos (Composite).
    public void editarProducto(Usuario actor, Producto producto, String nombre, double precio) {
        exigirAdministrador(actor, "editar un producto");
        exigirExiste(productos.contains(producto), "El producto no pertenece al cine");
        if (existeNombreProducto(nombre, producto)) {
            throw new EstadoInvalidoException("Ya existe otro producto con ese nombre");
        }
        producto.setNombre(nombre);
        producto.setPrecio(precio);
    }

    public void eliminarProducto(Usuario actor, Producto producto) {
        exigirAdministrador(actor, "eliminar un producto");
        exigirExiste(productos.contains(producto), "El producto no pertenece al cine");
        for (Combo combo : combos) {
            if (contiene(combo, producto)) {
                throw new EstadoInvalidoException("El producto forma parte del combo \"" + combo.getNombre() + "\"");
            }
        }
        productos.remove(producto);
    }

    // -------------------------------------------------------------------- Combos

    public Combo crearCombo(Usuario actor, String nombre, List<Comprable> componentes, double descuento) {
        exigirAdministrador(actor, "crear un combo");
        validarComponentes(componentes);
        Combo combo = new Combo("C" + (++consecutivoCombo), nombre, componentes, descuento);
        combos.add(combo);
        return combo;
    }

    /*
     Combo es inmutable, así que editar es reemplazarlo conservando su id.
     No se permite si otro combo lo contiene, porque ese combo seguiría apuntando al viejo.
    */
    public Combo editarCombo(Usuario actor, Combo combo, String nombre,
                             List<Comprable> componentes, double descuento) {
        exigirAdministrador(actor, "editar un combo");
        exigirExiste(combos.contains(combo), "El combo no pertenece al cine");
        exigirLibreDeContenedores(combo, "editar");
        validarComponentes(componentes);
        Combo nuevo = new Combo(combo.getId(), nombre, componentes, descuento);
        combos.set(combos.indexOf(combo), nuevo);
        return nuevo;
    }

    public void eliminarCombo(Usuario actor, Combo combo) {
        exigirAdministrador(actor, "eliminar un combo");
        exigirExiste(combos.contains(combo), "El combo no pertenece al cine");
        exigirLibreDeContenedores(combo, "eliminar");
        combos.remove(combo);
    }

    // --------------------------------------------------------------- Promociones

    public Promocion crearPromocion(Usuario actor, String nombre, double porcentaje) {
        exigirAdministrador(actor, "crear una promoción");
        if (existePromocion(nombre, null)) {
            throw new EstadoInvalidoException("Ya existe una promoción con ese nombre");
        }
        Promocion promocion = new Promocion(nombre, porcentaje);
        promociones.add(promocion);
        return promocion;
    }

    public void editarPromocion(Usuario actor, Promocion promocion, String nombre, double porcentaje) {
        exigirAdministrador(actor, "editar una promoción");
        exigirExiste(promociones.contains(promocion), "La promoción no pertenece al cine");
        if (existePromocion(nombre, promocion)) {
            throw new EstadoInvalidoException("Ya existe otra promoción con ese nombre");
        }
        promocion.setNombre(nombre);
        promocion.setPorcentajeDescuento(porcentaje);
    }

    public void eliminarPromocion(Usuario actor, Promocion promocion) {
        exigirAdministrador(actor, "eliminar una promoción");
        exigirExiste(promociones.contains(promocion), "La promoción no pertenece al cine");
        promociones.remove(promocion);
    }

    // ------------------------------------------------------------ Reglas de puntos

    // Cada $valorPorBloque gastados dan puntosPorBloque puntos. Aplica a compras futuras.
    public void configurarAcumulacion(Usuario actor, double valorPorBloque, int puntosPorBloque) {
        exigirAdministrador(actor, "cambiar la regla de acumulación de puntos");
        config.setReglaAcumulacion(valorPorBloque, puntosPorBloque);
    }

    // Puntos que cuesta canjear una entrada, un producto o un combo.
    public void configurarEquivalencias(Usuario actor, int entrada, int producto, int combo) {
        exigirAdministrador(actor, "cambiar las equivalencias de puntos");
        config.setEquivalenciasPuntos(entrada, producto, combo);
    }

    // ------------------------------------------------------------------ Clientes

    public List<Cliente> getClientes(Usuario actor) {
        exigirAdministrador(actor, "consultar los clientes");
        return usuarios.values().stream()
                .filter(u -> u instanceof Cliente)
                .map(u -> (Cliente) u)
                .collect(Collectors.toList());
    }

    public List<Cliente> buscarClientes(Usuario actor, String texto) {
        List<Cliente> todos = getClientes(actor);
        if (texto == null || texto.isBlank()) {
            return todos;
        }
        String buscado = texto.trim().toLowerCase();
        return todos.stream()
                .filter(c -> c.getNombre().toLowerCase().contains(buscado)
                        || c.getCorreo().toLowerCase().contains(buscado))
                .collect(Collectors.toList());
    }

    public List<Compra> getComprasDeCliente(Usuario actor, Cliente cliente) {
        exigirAdministrador(actor, "consultar las compras de un cliente");
        return compras.stream()
                .filter(c -> c.getCliente().getId().equals(cliente.getId()))
                .collect(Collectors.toList());
    }

    public List<Compra> getTodasLasCompras(Usuario actor) {
        exigirAdministrador(actor, "consultar las compras");
        return Collections.unmodifiableList(compras);
    }

    // ------------------------------------------------------------------ Internos

    private SalaFactory fabricaPara(TipoSala tipo) {
        switch (tipo) {
            case SALA_3D:
                return new SalaFactory3D();
            case SALA_4DX:
                return new SalaFactory4DX();
            case SALA_2D:
            default:
                return new SalaFactory2D();
        }
    }

    private void exigirAdministrador(Usuario actor, String accion) {
        if (actor == null || !actor.esAdministrador()) {
            String nombre = actor == null ? "Anónimo" : actor.getNombre();
            throw new AccesoDenegadoException(nombre, accion);
        }
    }

    private void exigirExiste(boolean existe, String mensaje) {
        if (!existe) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private boolean existeTitulo(String titulo, Pelicula excepto) {
        return titulo != null && peliculas.stream()
                .filter(p -> p != excepto)
                .anyMatch(p -> p.getTitulo().equalsIgnoreCase(titulo.trim()));
    }

    private boolean existeNombreProducto(String nombre, Producto excepto) {
        return nombre != null && productos.stream()
                .filter(p -> p != excepto)
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre.trim()));
    }

    private boolean existePromocion(String nombre, Promocion excepto) {
        return nombre != null && promociones.stream()
                .filter(p -> p != excepto)
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre.trim()));
    }

    private void validarHorario(Sala sala, Pelicula pelicula, LocalDateTime fechaHora, Funcion excepto) {
        if (fechaHora == null || !fechaHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La función debe programarse a futuro");
        }
        if (haySolapamiento(sala, fechaHora, pelicula.getDuracionMinutos(), excepto)) {
            throw new EstadoInvalidoException("La sala ya tiene una función en ese horario");
        }
    }

    private boolean haySolapamiento(Sala sala, LocalDateTime inicio, int duracionMinutos, Funcion excepto) {
        LocalDateTime fin = inicio.plusMinutes(duracionMinutos + MINUTOS_ASEO);
        for (Funcion f : funciones) {
            if (f == excepto || f.getSala() != sala || f.estaCancelada()) {
                continue;
            }
            LocalDateTime inicioExistente = f.getFechaHora();
            LocalDateTime finExistente = inicioExistente
                    .plusMinutes(f.getPelicula().getDuracionMinutos() + MINUTOS_ASEO);
            if (inicio.isBefore(finExistente) && inicioExistente.isBefore(fin)) {
                return true;
            }
        }
        return false;
    }

    // Los componentes de un combo deben ser productos o combos del catálogo.
    private void validarComponentes(List<Comprable> componentes) {
        if (componentes == null || componentes.isEmpty()) {
            throw new IllegalArgumentException("Un combo necesita al menos un componente");
        }
        for (Comprable c : componentes) {
            boolean enCatalogo = (c instanceof Producto && productos.contains(c))
                    || (c instanceof Combo && combos.contains(c));
            if (!enCatalogo) {
                throw new IllegalArgumentException("\"" + c.getNombre() + "\" no está en el catálogo del cine");
            }
        }
    }

    private void exigirLibreDeContenedores(Combo combo, String accion) {
        for (Combo otro : combos) {
            if (otro != combo && contiene(otro, combo)) {
                throw new EstadoInvalidoException("No se puede " + accion + " el combo: forma parte de \""
                        + otro.getNombre() + "\"");
            }
        }
    }

    // ¿El combo contiene al elemento, directamente o dentro de otro combo?
    private boolean contiene(Combo combo, Comprable buscado) {
        for (Comprable c : combo.getComponentes()) {
            if (c.equals(buscado)) {
                return true;
            }
            if (c instanceof Combo && contiene((Combo) c, buscado)) {
                return true;
            }
        }
        return false;
    }
}