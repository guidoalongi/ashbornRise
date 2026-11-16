package ashbornrise.modelo.fabricas;

import ashbornrise.modelo.enemigos.Enemigo;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe una batalla de la secuencia: qué enemigos aparecen y qué botín se gana.
 * Las batallas pasan a ser datos (una lista de definiciones) en lugar de un switch por número.
 */
public class DefinicionBatalla {

    private List<String> nombresEnemigos = new ArrayList<>();
    private List<CreadorEnemigo> creadoresEnemigos = new ArrayList<>();
    private List<String> botin = new ArrayList<>();

    /** Agrega un enemigo. Devuelve la misma definición para poder encadenar llamadas. */
    public DefinicionBatalla enemigo(String nombre, CreadorEnemigo creador) {
        nombresEnemigos.add(nombre);
        creadoresEnemigos.add(creador);
        return this;
    }

    /** Agrega un ítem (por su nombre en el catálogo) al botín de esta batalla. */
    public DefinicionBatalla botin(String nombreItem) {
        botin.add(nombreItem);
        return this;
    }

    /** Crea enemigos nuevos cada vez (así se puede reintentar la batalla). */
    public List<Enemigo> crearEnemigos(int numeroBatalla) {
        List<Enemigo> enemigos = new ArrayList<>();
        for (int i = 0; i < creadoresEnemigos.size(); i++) {
            enemigos.add(creadoresEnemigos.get(i).crear(nombresEnemigos.get(i), numeroBatalla));
        }
        return enemigos;
    }

    public List<String> getBotin() {
        return new ArrayList<>(botin);
    }
}
