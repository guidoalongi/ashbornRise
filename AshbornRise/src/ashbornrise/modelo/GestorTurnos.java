package ashbornrise.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fabricación Pura: no es un concepto del juego, existe para que Batalla
 * no tenga además la responsabilidad de ordenar los turnos.
 */
public class GestorTurnos {

    private ComparadorPorVelocidad criterio;

    public GestorTurnos() {
        this.criterio = new ComparadorPorVelocidad();
    }

    /**
     * Ordena de mayor a menor velocidad. Primero se mezcla la lista para que,
     * a igual velocidad, el orden sea al azar: sort() de Java no cambia el orden
     * de los elementos que el Comparator considera iguales.
     */
    public List<Combatiente> calcularOrden(List<Combatiente> combatientes) {
        List<Combatiente> orden = new ArrayList<>(combatientes);
        Collections.shuffle(orden);
        orden.sort(criterio);
        return orden;
    }
}
