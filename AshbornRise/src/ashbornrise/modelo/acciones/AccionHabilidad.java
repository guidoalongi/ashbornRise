package ashbornrise.modelo.acciones;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.habilidades.Habilidad;

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
        this.objetivos = objetivos;
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

    public List<Combatiente> getObjetivos() {
        return objetivos;
    }
}
