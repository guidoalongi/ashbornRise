package ashbornrise.modelo.fabricas;

import ashbornrise.modelo.personajes.Personaje;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registro de clases de personaje: nombre de la clase → cómo crearla.
 *
 * Reemplaza al switch que había en FabricaJuego. Para agregar una clase nueva (por ejemplo,
 * un Paladín) no hay que modificar esta clase ni agregar un "case": alcanza con registrarla,
 *   registro.registrar("Paladín", Paladin::new);
 * Abierto a la extensión, cerrado a la modificación.
 */
public class RegistroPersonajes {

    private Map<String, CreadorPersonaje> creadores = new LinkedHashMap<>();

    public void registrar(String clase, CreadorPersonaje creador) {
        creadores.put(clase, creador);
    }

    /** Crea un personaje a partir del nombre de su clase (se usa, por ejemplo, al cargar una partida). */
    public Personaje crear(String clase, String nombre) {
        CreadorPersonaje creador = creadores.get(clase);
        if (creador == null) {
            throw new IllegalArgumentException("Clase de personaje desconocida: " + clase);
        }
        return creador.crear(nombre);
    }

    public boolean existe(String clase) {
        return creadores.containsKey(clase);
    }
}
