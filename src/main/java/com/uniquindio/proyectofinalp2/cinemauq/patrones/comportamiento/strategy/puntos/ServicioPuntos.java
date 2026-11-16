package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.excepcion.PuntosInsuficientesException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

/*
 Context del Strategy.
 Confirma una compra ya pagada y acumula los puntos delegando el cálculo en la estrategia activa.
 No contiene ningún if/else sobre el tipo de política: agregar una política nueva
 es crear una clase que implemente EstrategiaPuntos (OCP), sin tocar esta clase.

 También administra el canje: los lotes (PuntosAcumulados) nunca se modifican;
 cada canje se anota aparte como ConsumoPuntos. Lo que le queda a un lote es
 sus puntos menos la suma de sus consumos.
*/
public class ServicioPuntos {

    private final ConfiguracionCine config;
    private final Map<String, List<PuntosAcumulados>> cuentas = new HashMap<>();
    private final Map<String, List<ConsumoPuntos>> consumos = new HashMap<>();
    private final Set<String> comprasConfirmadas = new HashSet<>();
    private EstrategiaPuntos estrategia;
    // Reloj del servicio: en producción es el del sistema; las pruebas lo adelantan para verificar vencimientos.
    private Clock reloj = Clock.systemDefaultZone();

    public ServicioPuntos(ConfiguracionCine config, EstrategiaPuntos estrategiaInicial) {
        this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
        setEstrategia(estrategiaInicial);
    }

    /* Cambio de política en tiempo de ejecución. */
    public void setEstrategia(EstrategiaPuntos estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "La estrategia es obligatoria");
    }

    public void setReloj(Clock reloj) {
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    public EstrategiaPuntos getEstrategia() {
        return estrategia;
    }

    /* Calcula sin registrar (vista previa: "Ganarás X pts con esta compra"). */
    public int calcularPuntos(double valorPagado) {
        if (valorPagado < 0) {
            throw new IllegalArgumentException("El valor pagado no puede ser negativo");
        }
        return estrategia.calcular(valorPagado);
    }

    /*
     Confirmación de la compra: solo una compra PAGADA acumula puntos y solo una vez.
     Devuelve el registro creado (o null si la política no otorgó puntos).
    */
    public PuntosAcumulados confirmarCompra(Compra compra) {
        Objects.requireNonNull(compra, "La compra es obligatoria");
        if (compra.getEstado() != EstadoCompra.PAGADA) {
            throw new EstadoInvalidoException(
                    "Solo una compra pagada acumula puntos (estado: " + compra.getEstado() + ")");
        }
        if (!comprasConfirmadas.add(compra.getId())) {
            throw new EstadoInvalidoException("Los puntos de esta compra ya fueron acumulados");
        }

        int puntos = calcularPuntos(compra.getTotal());
        if (puntos == 0) {
            return null;
        }
        LocalDate hoy = LocalDate.now(reloj);
        PuntosAcumulados registro = new PuntosAcumulados(compra.getId(), puntos, hoy,
                hoy.plusYears(config.getVigenciaPuntosAnios()), estrategia.getNombre());
        cuentas.computeIfAbsent(compra.getCliente().getId(), k -> new ArrayList<>()).add(registro);
        return registro;
    }

    /*
     Comprueba que el cliente tenga al menos esos puntos vigentes. Se llama ANTES de cobrar,
     para no cobrar una compra cuyos puntos luego no se puedan descontar.
    */
    public void verificarDisponibles(Cliente cliente, int puntos) {
        int disponibles = getPuntosVigentes(cliente);
        if (puntos > disponibles) {
            throw new PuntosInsuficientesException(disponibles, puntos);
        }
    }

    /*
     Gasta puntos en el canje de una compra. Toma primero los lotes que vencen antes,
     ignora los vencidos y no toca nada si no alcanzan. Devuelve los consumos creados.
    */
    public List<ConsumoPuntos> redimir(Cliente cliente, String idCompraCanje, int puntos) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");
        Objects.requireNonNull(idCompraCanje, "La compra del canje es obligatoria");
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos a redimir deben ser mayores a cero");
        }
        List<ConsumoPuntos> delCliente = consumos.computeIfAbsent(cliente.getId(), k -> new ArrayList<>());
        if (delCliente.stream().anyMatch(c -> c.getIdCompraCanje().equals(idCompraCanje))) {
            throw new EstadoInvalidoException("Los puntos de esta compra ya fueron redimidos");
        }
        verificarDisponibles(cliente, puntos);

        LocalDate hoy = LocalDate.now(reloj);
        List<PuntosAcumulados> lotes = new ArrayList<>(getHistorial(cliente));
        lotes.sort(Comparator.comparing(PuntosAcumulados::getFechaVencimiento)
                .thenComparing(PuntosAcumulados::getFechaObtencion));

        int pendiente = puntos;
        List<ConsumoPuntos> nuevos = new ArrayList<>();
        for (PuntosAcumulados lote : lotes) {
            if (pendiente == 0) {
                break;
            }
            if (!lote.estaVigente(hoy)) {
                continue;
            }
            int tomar = Math.min(getRestantes(cliente, lote), pendiente);
            if (tomar > 0) {
                nuevos.add(new ConsumoPuntos(lote.getIdCompra(), idCompraCanje, tomar, hoy));
                pendiente -= tomar;
            }
        }
        delCliente.addAll(nuevos);
        return nuevos;
    }

    /*
     Devuelve los puntos gastados en una compra de canje que fue reembolsada: se borran sus
     consumos y los puntos vuelven a sus lotes de origen (con su vencimiento original; si el
     lote ya venció, vuelven vencidos). Devuelve cuántos puntos se restituyeron.
     Llamarlo dos veces es inofensivo.
    */
    public int devolverPuntos(Compra compra) {
        Objects.requireNonNull(compra, "La compra es obligatoria");
        if (compra.getEstado() != EstadoCompra.REEMBOLSADA) {
            throw new EstadoInvalidoException(
                    "Solo se devuelven los puntos de una compra reembolsada (estado: " + compra.getEstado() + ")");
        }
        List<ConsumoPuntos> delCliente = consumos.get(compra.getCliente().getId());
        if (delCliente == null) {
            return 0;
        }
        int devueltos = 0;
        List<ConsumoPuntos> aBorrar = new ArrayList<>();
        for (ConsumoPuntos c : delCliente) {
            if (c.getIdCompraCanje().equals(compra.getId())) {
                aBorrar.add(c);
                if (existeLote(compra.getCliente(), c.getIdLote())) {
                    devueltos += c.getPuntos();
                }
            }
        }
        delCliente.removeAll(aBorrar);
        return devueltos;
    }

    /*
     Quita los puntos que otorgó una compra que fue reembolsada: si el dinero
     volvió, los puntos ganados con ese dinero también. Se quita lo que AÚN no se gastó;
     lo que ya se canjeó queda canjeado (regla del grupo). Devuelve cuántos puntos se
     quitaron (0 si no quedaba nada). Llamarlo dos veces es inofensivo.
    */
    public int revocarPuntos(Compra compra) {
        Objects.requireNonNull(compra, "La compra es obligatoria");
        if (compra.getEstado() != EstadoCompra.REEMBOLSADA) {
            throw new EstadoInvalidoException(
                    "Solo se revocan los puntos de una compra reembolsada (estado: " + compra.getEstado() + ")");
        }
        List<PuntosAcumulados> cuenta = cuentas.get(compra.getCliente().getId());
        if (cuenta == null) {
            return 0;
        }
        int revocados = 0;
        for (PuntosAcumulados lote : cuenta) {
            if (lote.getIdCompra().equals(compra.getId())) {
                revocados += getRestantes(compra.getCliente(), lote);
            }
        }
        cuenta.removeIf(p -> p.getIdCompra().equals(compra.getId()));
        return revocados;
    }

    public List<PuntosAcumulados> getHistorial(Cliente cliente) {
        return Collections.unmodifiableList(
                cuentas.getOrDefault(cliente.getId(), Collections.<PuntosAcumulados>emptyList()));
    }

    public List<ConsumoPuntos> getHistorialConsumos(Cliente cliente) {
        return Collections.unmodifiableList(
                consumos.getOrDefault(cliente.getId(), Collections.<ConsumoPuntos>emptyList()));
    }

    /* Lo que le queda a un lote: sus puntos menos lo ya canjeado de él. */
    public int getRestantes(Cliente cliente, PuntosAcumulados lote) {
        int gastados = 0;
        for (ConsumoPuntos c : getHistorialConsumos(cliente)) {
            if (c.getIdLote().equals(lote.getIdCompra())) {
                gastados += c.getPuntos();
            }
        }
        return lote.getPuntos() - gastados;
    }

    /* Puntos que el cliente puede gastar hoy: lotes vigentes, descontando lo ya canjeado. */
    public int getPuntosVigentes(Cliente cliente) {
        LocalDate hoy = LocalDate.now(reloj);
        int total = 0;
        for (PuntosAcumulados p : getHistorial(cliente)) {
            if (p.estaVigente(hoy)) {
                total += getRestantes(cliente, p);
            }
        }
        return total;
    }

    /* Total histórico de puntos canjeados por el cliente (para reportes). */
    public int getPuntosRedimidos(Cliente cliente) {
        int total = 0;
        for (ConsumoPuntos c : getHistorialConsumos(cliente)) {
            total += c.getPuntos();
        }
        return total;
    }

    private boolean existeLote(Cliente cliente, String idLote) {
        return getHistorial(cliente).stream().anyMatch(p -> p.getIdCompra().equals(idLote));
    }
}