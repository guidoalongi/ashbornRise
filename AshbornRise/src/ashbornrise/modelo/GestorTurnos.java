package ashbornrise.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fabricación Pura: no es un concepto del juego, existe para que Batalla
 * no tenga además la responsabilidad de ordenar los turnos.
 */
public class GestorTurnos {

    /**
     * Ordena de mayor a menor velocidad. Primero se mezcla la lista para que,
     * a igual velocidad, el orden sea al azar.
     */
    public List<Combatiente> calcularOrden(List<Combatiente> combatientes) {
        List<Combatiente> orden = new ArrayList<>(combatientes);
        Collections.shuffle(orden);
        // Ordenamiento por inserción (estable: respeta el orden al azar en los empates).
        for (int i = 1; i < orden.size(); i++) {
            Combatiente actual = orden.get(i);
            int j = i - 1;
            while (j >= 0 && orden.get(j).obtenerVelocidad() < actual.obtenerVelocidad()) {
                orden.set(j + 1, orden.get(j));
                j--;
            }
            orden.set(j + 1, actual);
        }
        return orden;
    }
}
