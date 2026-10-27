package ashbornrise.modelo;

import java.util.Comparator;

/**
 * Criterio para ordenar combatientes de mayor a menor velocidad.
 *
 * Es un Comparator y no un Comparable porque la velocidad no es el "orden natural"
 * de un combatiente: es el criterio que usa la cola de turnos. Si mañana hace falta
 * otro orden, se crea otro Comparator sin tocar Combatiente ni GestorTurnos.
 */
public class ComparadorPorVelocidad implements Comparator<Combatiente> {

    @Override
    public int compare(Combatiente a, Combatiente b) {
        return b.obtenerVelocidad() - a.obtenerVelocidad();
    }
}
