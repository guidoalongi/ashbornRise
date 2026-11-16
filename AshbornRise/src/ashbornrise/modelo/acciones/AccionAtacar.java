package ashbornrise.modelo.acciones;

import ashbornrise.modelo.AccionInvalidaException;
import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;

import java.util.ArrayList;
import java.util.List;

public class AccionAtacar extends Accion {

    private Combatiente objetivo;

    public AccionAtacar(Combatiente objetivo) {
        this.objetivo = objetivo;
    }

    /** Solo se puede atacar a un enemigo vivo que esté en esta batalla. */
    @Override
    public void validar(Combatiente actor, Batalla batalla) {
        if (objetivo == null || !batalla.participa(objetivo)) {
            throw new AccionInvalidaException("El objetivo no está en esta batalla.");
        }
        if (actor.esAliadoDe(objetivo)) {
            throw new AccionInvalidaException("No se puede atacar a un aliado.");
        }
        if (!objetivo.estaVivo()) {
            throw new AccionInvalidaException(objetivo.getNombre() + " ya fue derrotado.");
        }
    }

    @Override
    public void ejecutar(Combatiente actor, Batalla batalla) {
        int danio = actor.atacar(objetivo);
        batalla.registrar(actor.getNombre() + " ataca a " + objetivo.getNombre()
                + " por " + danio + " de daño.");
        actor.alGolpear(objetivo, batalla);
        if (objetivo.estaVivo()) {
            objetivo.reaccionarAAtaque(actor, batalla);
        } else {
            batalla.registrar(objetivo.getNombre() + " fue derrotado.");
        }
    }

    public Combatiente getObjetivo() {
        return objetivo;
    }

    @Override
    public TipoAccion getTipo() {
        return TipoAccion.ATAQUE;
    }

    @Override
    public List<Combatiente> getObjetivos() {
        List<Combatiente> lista = new ArrayList<>();
        lista.add(objetivo);
        return lista;
    }
}
