package ashbornrise.modelo.personajes;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.habilidades.HabilidadEstado;
import ashbornrise.modelo.habilidades.HabilidadOfensiva;
import ashbornrise.modelo.habilidades.TipoAlcance;

import java.util.Random;

/** El más resistente. Usa Energía y puede contraatacar cuando lo golpean. */
public class Guerrero extends Personaje {

    private int probabilidadContraataque; // en porcentaje (20 = 20%)
    private Random random;

    public Guerrero(String nombre) {
        super(nombre, 150, 18, 16, 7, TipoRecurso.ENERGIA, 50, 0);
        this.probabilidadContraataque = 20;
        this.random = new Random();
        aprenderHabilidad(new HabilidadOfensiva("Golpe Devastador", 25, TipoRecurso.ENERGIA,
                TipoAlcance.UN_ENEMIGO, 1.8, 1));
        aprenderHabilidad(new HabilidadEstado("Postura Defensiva", 15, TipoRecurso.ENERGIA,
                TipoAlcance.PROPIO, TipoEstado.ESCUDO, 30, 2, false));
    }

    /** Con cierta probabilidad devuelve un ataque básico al que lo golpeó. */
    @Override
    public void reaccionarAAtaque(Combatiente atacante, Batalla batalla) {
        if (atacante.estaVivo() && random.nextInt(100) < probabilidadContraataque) {
            int danio = atacar(atacante);
            batalla.registrar("¡" + nombre + " contraataca a " + atacante.getNombre()
                    + " por " + danio + " de daño!");
        }
    }

    @Override
    protected void aplicarMejorasDeNivel() {
        mejorarEstadisticas(15, 3, 3, 3);
    }

    @Override
    public String getNombreClase() {
        return "Guerrero";
    }

    public int getProbabilidadContraataque() {
        return probabilidadContraataque;
    }
}
