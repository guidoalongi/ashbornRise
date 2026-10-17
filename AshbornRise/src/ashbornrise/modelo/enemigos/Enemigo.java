package ashbornrise.modelo.enemigos;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.personajes.Personaje;

import java.util.List;
import java.util.Random;

/**
 * Enemigo controlado por la computadora. Cada tipo de enemigo redefine
 * decidirAccion(): así los enemigos se diferencian por su COMPORTAMIENTO
 * y no solo por sus números (requisito de la consigna).
 */
public abstract class Enemigo extends Combatiente {

    private int experienciaOtorgada;
    private int oroOtorgado;
    protected Random random;

    public Enemigo(String nombre, int vida, int ataque, int defensa, int velocidad,
                   int experienciaOtorgada, int oroOtorgado) {
        super(nombre, vida, ataque, defensa, velocidad);
        this.experienciaOtorgada = experienciaOtorgada;
        this.oroOtorgado = oroOtorgado;
        this.random = new Random();
    }

    /** Elige qué hacer en su turno mirando el estado de la batalla. */
    public abstract Accion decidirAccion(Batalla batalla);

    @Override
    public boolean esControladoPorJugador() {
        return false;
    }

    /** Aumenta las estadísticas base según el número de batalla (10% por batalla). */
    protected static int escalar(int valorBase, int numeroBatalla) {
        return (int) Math.round(valorBase * (1 + 0.10 * (numeroBatalla - 1)));
    }

    protected Personaje elegirAlAzar(List<Personaje> personajes) {
        return personajes.get(random.nextInt(personajes.size()));
    }

    protected Personaje elegirConMenosVida(List<Personaje> personajes) {
        Personaje elegido = personajes.get(0);
        for (Personaje p : personajes) {
            if (p.getVidaActual() < elegido.getVidaActual()) {
                elegido = p;
            }
        }
        return elegido;
    }

    public int getExperienciaOtorgada() {
        return experienciaOtorgada;
    }

    public int getOroOtorgado() {
        return oroOtorgado;
    }
}
