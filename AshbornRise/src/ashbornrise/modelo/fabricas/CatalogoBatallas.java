package ashbornrise.modelo.fabricas;

import java.util.ArrayList;
import java.util.List;

/** La secuencia de batallas del juego, en orden. La cantidad de batallas sale de acá. */
public class CatalogoBatallas {

    private List<DefinicionBatalla> batallas = new ArrayList<>();

    public void agregar(DefinicionBatalla definicion) {
        batallas.add(definicion);
    }

    /** Definición de la batalla número n (empieza en 1). */
    public DefinicionBatalla getBatalla(int numero) {
        if (numero < 1 || numero > batallas.size()) {
            throw new IllegalArgumentException("No existe la batalla " + numero);
        }
        return batallas.get(numero - 1);
    }

    public int getCantidad() {
        return batallas.size();
    }
}
