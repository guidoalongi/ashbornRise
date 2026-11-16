package ashbornrise.modelo.items;

import ashbornrise.modelo.personajes.Personaje;

/**
 * Lo que hace un consumible al usarse. Cada efecto es una clase (RestaurarVida, RestaurarMana,
 * QuitarEstado...), así Consumible no necesita un switch para decidir qué hacer (Polimorfismo).
 */
public interface EfectoConsumible {

    /** Aplica el efecto y devuelve un texto con el resultado. */
    String aplicar(Personaje objetivo);
}
