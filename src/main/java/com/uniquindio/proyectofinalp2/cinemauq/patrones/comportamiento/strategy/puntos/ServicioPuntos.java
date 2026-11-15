package com.uniquindio.proyectofinalp2.cinemauq.patrones.comportamiento.strategy.puntos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.uniquindio.proyectofinalp2.cinemauq.excepcion.EstadoInvalidoException;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.usuarios.Cliente;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.Compra;
import com.uniquindio.proyectofinalp2.cinemauq.modelo.ventas.EstadoCompra;
import com.uniquindio.proyectofinalp2.cinemauq.patrones.creacionales.singleton.ConfiguracionCine;

/*
 Context del Strategy.
 Confirma una compra ya pagada y acumula los puntos delegando el cálculo en la estrategia activa.
 No contiene ningún if/else sobre el tipo de política: agregar una política nueva
 es crear una clase que implemente EstrategiaPuntos (OCP), sin tocar esta clase.
*/
public class ServicioPuntos {

    private final ConfiguracionCine config;
    private final Map<String, List<PuntosAcumulados>> cuentas = new HashMap<>();
    private final Set<String> comprasConfirmadas = new HashSet<>();
    private EstrategiaPuntos estrategia;

    public ServicioPuntos(ConfiguracionCine config, EstrategiaPuntos estrategiaInicial) {
        this.config = Objects.requireNonNull(config, "La configuración es obligatoria");
        setEstrategia(estrategiaInicial);
    }

    /* Cambio de política en tiempo de ejecución. */
    public void setEstrategia(EstrategiaPuntos estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "La estrategia es obligatoria");
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
        LocalDate hoy = LocalDate.now();
        PuntosAcumulados registro = new PuntosAcumulados(compra.getId(), puntos, hoy,
                hoy.plusYears(config.getVigenciaPuntosAnios()), estrategia.getNombre());
        cuentas.computeIfAbsent(compra.getCliente().getId(), k -> new ArrayList<>()).add(registro);
        return registro;
    }

    /*
     Quita los puntos que otorgó una compra que fue reembolsada: si el dinero
     volvió, los puntos ganados con ese dinero también. Devuelve cuántos puntos
     se quitaron (0 si la compra no había otorgado puntos). Llamarlo dos veces
     es inofensivo: la segunda vez ya no hay nada que quitar.
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
        int revocados = cuenta.stream()
                .filter(p -> p.getIdCompra().equals(compra.getId()))
                .mapToInt(PuntosAcumulados::getPuntos)
                .sum();
        cuenta.removeIf(p -> p.getIdCompra().equals(compra.getId()));
        return revocados;
    }

    public List<PuntosAcumulados> getHistorial(Cliente cliente) {
        return Collections.unmodifiableList(
                cuentas.getOrDefault(cliente.getId(), Collections.<PuntosAcumulados>emptyList()));
    }

    public int getPuntosVigentes(Cliente cliente) {
        LocalDate hoy = LocalDate.now();
        int total = 0;
        for (PuntosAcumulados p : getHistorial(cliente)) {
            if (p.estaVigente(hoy)) {
                total += p.getPuntos();
            }
        }
        return total;
    }
}