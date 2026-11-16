package ashbornrise.modelo.acciones;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.habilidades.Habilidad;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que un combatiente hace en su turno (atacar, defender, usar habilidad o ítem).
 * Batalla no necesita saber qué acción es: primero llama a validar() y después a ejecutar()
 * (Polimorfismo). Cada acción conoce sus propias reglas (Experto).
 */
public abstract class Accion {

    /**
     * Verifica que la acción respete las reglas del juego. Si no, lanza AccionInvalidaException.
     * Por defecto no hay nada que validar (por ejemplo, Defender).
     */
    public void validar(Combatiente actor, Batalla batalla) {
    }

    public abstract void ejecutar(Combatiente actor, Batalla batalla);

    // Consultas para mostrar la acción (las usa el controlador para armar los DTO).

    public abstract TipoAccion getTipo();

    /** A quiénes afecta. Por defecto, a nadie (por ejemplo, Defender). */
    public List<Combatiente> getObjetivos() {
        return new ArrayList<>();
    }

    /** Nombre de la habilidad o del ítem usado; null si no corresponde. */
    public String getNombreDeLoUsado() {
        return null;
    }

    /** Habilidad usada; null si la acción no es una habilidad. */
    public Habilidad getHabilidadUsada() {
        return null;
    }
}
