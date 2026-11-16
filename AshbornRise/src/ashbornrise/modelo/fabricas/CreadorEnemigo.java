package ashbornrise.modelo.fabricas;

import ashbornrise.modelo.enemigos.Enemigo;

/** Sabe crear un enemigo de una clase concreta. Ej.: GoblinMerodeador::new (nombre, número de batalla). */
public interface CreadorEnemigo {

    Enemigo crear(String nombre, int numeroBatalla);
}
