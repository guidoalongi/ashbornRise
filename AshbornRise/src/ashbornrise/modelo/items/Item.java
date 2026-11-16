package ashbornrise.modelo.items;

import ashbornrise.modelo.Inventario;
import ashbornrise.modelo.personajes.Personaje;

/**
 * Objeto del inventario. Hay dos tipos: Consumible (se gasta al usarlo) y Equipable
 * (se equipa y modifica las estadísticas). El Inventario, las acciones y los controladores
 * trabajan con Item sin saber cuál es (Polimorfismo).
 */
public abstract class Item {

    private String nombre;
    private String descripcion;

    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Usa el ítem sobre un personaje, sacándolo del inventario.
     * Cada tipo decide qué pasa (el Equipable, por ejemplo, devuelve al inventario lo que reemplaza).
     * Devuelve un texto con el resultado.
     */
    public abstract String usar(Personaje objetivo, Inventario inventario);

    /** ¿Se puede usar sobre este personaje? Por defecto, solo si está vivo. */
    public boolean puedeUsarseSobre(Personaje objetivo) {
        return objetivo.estaVivo();
    }

    /** ¿Se equipa (en lugar de gastarse)? Por defecto no; el Equipable sí. */
    public boolean esEquipable() {
        return false;
    }

    /** ¿Se puede usar en medio de una batalla? Por defecto sí; el Equipable no. */
    public boolean sePuedeUsarEnBatalla() {
        return true;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
