package ashbornrise.modelo.habilidades;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;

import java.util.List;

/**
 * Comportamiento común de todas las habilidades.
 * Las habilidades concretas del juego (Bola de Fuego, Curación, etc.) son
 * INSTANCIAS de las subclases configuradas con distintos valores
 * (ver FabricaJuego), no una clase nueva por cada habilidad.
 */
public abstract class Habilidad {

    private String nombre;
    private int costoRecurso;
    private TipoRecurso tipoRecurso;
    private TipoAlcance alcance;

    public Habilidad(String nombre, int costoRecurso, TipoRecurso tipoRecurso, TipoAlcance alcance) {
        this.nombre = nombre;
        this.costoRecurso = costoRecurso;
        this.tipoRecurso = tipoRecurso;
        this.alcance = alcance;
    }

    /**
     * Produce el efecto de la habilidad sobre los objetivos.
     * Recibe un Combatiente como origen para que la usen tanto personajes como enemigos.
     * Si el origen es un Personaje, el recurso ya fue descontado antes de llamar a este método.
     * Devuelve un texto que describe lo que pasó (para el registro de batalla).
     */
    public abstract String ejecutar(Combatiente origen, List<Combatiente> objetivos);

    /** Cantidad máxima de objetivos. Por defecto 1; las subclases pueden cambiarlo. */
    public int getCantidadObjetivos() {
        return 1;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCostoRecurso() {
        return costoRecurso;
    }

    public TipoRecurso getTipoRecurso() {
        return tipoRecurso;
    }

    public TipoAlcance getAlcance() {
        return alcance;
    }
}
