package ashbornrise.modelo.personajes;

import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.habilidades.HabilidadCuracion;
import ashbornrise.modelo.habilidades.HabilidadEstado;
import ashbornrise.modelo.habilidades.TipoAlcance;

/** Sostiene al equipo. Usa Maná y cura más que cualquier otro gracias a su poder de curación. */
public class Curador extends Personaje {

    private double poderCuracion;

    public Curador(String nombre) {
        super(nombre, 95, 10, 12, 10, TipoRecurso.MANA, 85, 14);
        this.poderCuracion = 1.3;
        aprenderHabilidad(new HabilidadCuracion("Curación", 22, TipoRecurso.MANA,
                TipoAlcance.UN_ALIADO, 35, 1));
        aprenderHabilidad(new HabilidadEstado("Bendición", 20, TipoRecurso.MANA,
                TipoAlcance.UN_ALIADO, TipoEstado.ESCUDO, 20, 2, true));
    }

    @Override
    public double getPoderCuracion() {
        return poderCuracion;
    }

    @Override
    protected void aplicarMejorasDeNivel() {
        mejorarEstadisticas(10, 7, 1, 2);
        poderCuracion = poderCuracion + 0.05;
    }

    @Override
    public String getNombreClase() {
        return "Curador";
    }
}
