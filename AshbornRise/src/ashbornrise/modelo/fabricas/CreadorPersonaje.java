package ashbornrise.modelo.fabricas;

import ashbornrise.modelo.personajes.Personaje;

/** Sabe crear un personaje de una clase concreta. Ej.: Guerrero::new (el constructor que recibe el nombre). */
public interface CreadorPersonaje {

    Personaje crear(String nombre);
}
