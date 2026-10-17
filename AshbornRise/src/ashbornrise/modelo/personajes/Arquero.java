package ashbornrise.modelo.personajes;

import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.habilidades.HabilidadEstado;
import ashbornrise.modelo.habilidades.HabilidadOfensiva;
import ashbornrise.modelo.habilidades.TipoAlcance;

import java.util.Random;

/** El más rápido. Usa Energía y puede hacer golpes críticos (daño doble). */
public class Arquero extends Personaje {

    private int probabilidadCritico; // en porcentaje (15 = 15%)
    private Random random;

    public Arquero(String nombre) {
        super(nombre, 110, 19, 11, 15, TipoRecurso.ENERGIA, 60, 0);
        this.probabilidadCritico = 15;
        this.random = new Random();
        aprenderHabilidad(new HabilidadOfensiva("Disparo Múltiple", 20, TipoRecurso.ENERGIA,
                TipoAlcance.VARIOS_ENEMIGOS, 1.0, 2));
        aprenderHabilidad(new HabilidadEstado("Flecha Envenenada", 18, TipoRecurso.ENERGIA,
                TipoAlcance.UN_ENEMIGO, TipoEstado.VENENO, 6, 3, false));
    }

    /** Redefine el cálculo de daño: a veces es crítico. Vale para ataques y habilidades ofensivas. */
    @Override
    public int calcularDanio() {
        int danio = super.calcularDanio();
        if (random.nextInt(100) < probabilidadCritico) {
            danio = danio * 2;
        }
        return danio;
    }

    @Override
    protected void aplicarMejorasDeNivel() {
        mejorarEstadisticas(10, 4, 3, 2);
    }

    @Override
    public String getNombreClase() {
        return "Arquero";
    }

    public int getProbabilidadCritico() {
        return probabilidadCritico;
    }
}
