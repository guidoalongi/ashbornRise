package ashbornrise.modelo.fabricas;

import ashbornrise.modelo.items.Item;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catálogo de ítems del juego: nombre → cómo crearlo (consumibles y equipables por igual).
 * Reemplaza al switch de FabricaJuego: un ítem nuevo se agrega registrándolo, sin modificar código existente.
 */
public class CatalogoItems {

    private Map<String, CreadorItem> creadores = new LinkedHashMap<>();

    public void registrar(String nombre, CreadorItem creador) {
        creadores.put(nombre, creador);
    }

    /** Crea una instancia nueva del ítem (cada poción del inventario es un objeto distinto). */
    public Item crear(String nombre) {
        CreadorItem creador = creadores.get(nombre);
        if (creador == null) {
            throw new IllegalArgumentException("Ítem desconocido: " + nombre);
        }
        return creador.crear();
    }

    public boolean existe(String nombre) {
        return creadores.containsKey(nombre);
    }
}
