package ashbornrise.modelo.acciones;

import ashbornrise.modelo.AccionInvalidaException;
import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.habilidades.Habilidad;

import java.util.ArrayList;
import java.util.List;

/**
 * Usar una habilidad. Sirve para personajes y enemigos: el actor resuelve el costo
 * (Personaje descuenta recurso, Enemigo no paga nada) y la habilidad produce el efecto.
 */
public class AccionHabilidad extends Accion {

    private Habilidad habilidad;
    private List<Combatiente> objetivos;

    public AccionHabilidad(Habilidad habilidad, List<Combatiente> objetivos) {
        this.habilidad = habilidad;
        this.objetivos = new ArrayList<>(objetivos);
    }

    /**
     * El actor tiene que poder usar la habilidad (tenerla y que le alcance el recurso) y cada
     * objetivo tiene que estar en la batalla y ser compatible con el alcance: TipoAlcance decide
     * si es un enemigo vivo, un aliado vivo o el propio actor.
     */
    @Override
    public void validar(Combatiente actor, Batalla batalla) {
        if (habilidad == null || !actor.puedeUsar(habilidad)) {
            throw new AccionInvalidaException(actor.getNombre() + " no puede usar esa habilidad ahora.");
        }
        if (objetivos.isEmpty()) {
            throw new AccionInvalidaException(habilidad.getNombre() + " necesita un objetivo válido.");
        }
        if (objetivos.size() > habilidad.getCantidadObjetivos()) {
            throw new AccionInvalidaException(habilidad.getNombre() + " admite como máximo "
                    + habilidad.getCantidadObjetivos() + " objetivo(s).");
        }
        for (Combatiente objetivo : objetivos) {
            if (objetivo == null || !batalla.participa(objetivo)
                    || !habilidad.getAlcance().admite(actor, objetivo)) {
                throw new AccionInvalidaException("Objetivo no válido para " + habilidad.getNombre() + ".");
            }
        }
    }

    @Override
    public void ejecutar(Combatiente actor, Batalla batalla) {
        String resultado = actor.usarHabilidad(habilidad, objetivos);
        batalla.registrar(actor.getNombre() + " usa " + habilidad.getNombre() + ". " + resultado);
        for (Combatiente objetivo : objetivos) {
            if (!objetivo.estaVivo()) {
                batalla.registrar(objetivo.getNombre() + " fue derrotado.");
            }
        }
    }

    public Habilidad getHabilidad() {
        return habilidad;
    }

    @Override
    public TipoAccion getTipo() {
        return TipoAccion.HABILIDAD;
    }

    @Override
    public String getNombreDeLoUsado() {
        return habilidad.getNombre();
    }

    @Override
    public Habilidad getHabilidadUsada() {
        return habilidad;
    }

    @Override
    public List<Combatiente> getObjetivos() {
        return new ArrayList<>(objetivos);
    }
}
