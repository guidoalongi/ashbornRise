package ashbornrise.modelo.items;

import ashbornrise.modelo.personajes.Personaje;

/**
 * Objeto del inventario. Es abstracta aunque hoy tenga una sola subclase (Consumible)
 * para que el Inventario no dependa de un tipo concreto de ítem.
 */
public abstract class Item {

    private String nombre;
    private String descripcion;

    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Aplica el efecto del ítem sobre el personaje. Devuelve un texto con el resultado. */
    public abstract String usar(Personaje objetivo);

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
