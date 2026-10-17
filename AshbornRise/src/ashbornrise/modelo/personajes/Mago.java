package ashbornrise.modelo.personajes;

import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.habilidades.HabilidadEstado;
import ashbornrise.modelo.habilidades.HabilidadOfensiva;
import ashbornrise.modelo.habilidades.TipoAlcance;

/** El de mayor daño y el más frágil. Usa Maná, que se regenera cada turno. */
public class Mago extends Personaje {

    private TipoElemento elementoDominante;

    public Mago(String nombre) {
        super(nombre, 80, 22, 8, 11, TipoRecurso.MANA, 90, 12);
        this.elementoDominante = TipoElemento.FUEGO;
        aprenderHabilidad(new HabilidadOfensiva("Bola de Fuego", 30, TipoRecurso.MANA,
                TipoAlcance.VARIOS_ENEMIGOS, 1.2, 3));
        aprenderHabilidad(new HabilidadEstado("Escudo Arcano", 20, TipoRecurso.MANA,
                TipoAlcance.UN_ALIADO, TipoEstado.ESCUDO, 25, 2, false));
    }

    @Override
    protected void aplicarMejorasDeNivel() {
        mejorarEstadisticas(8, 8, 4, 1);
    }

    @Override
    public String getNombreClase() {
        return "Mago";
    }

    public TipoElemento getElementoDominante() {
        return elementoDominante;
    }
}
