package ashbornrise.modelo.acciones;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;

/**
 * Lo que un combatiente hace en su turno (atacar, defender, usar habilidad o ítem).
 * Batalla no necesita saber qué acción es: solo llama a ejecutar() (Polimorfismo).
 */
public abstract class Accion {

    public abstract void ejecutar(Combatiente actor, Batalla batalla);
}
