package ashbornrise.modelo.acciones;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;

public class AccionAtacar extends Accion {

    private Combatiente objetivo;

    public AccionAtacar(Combatiente objetivo) {
        this.objetivo = objetivo;
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
}
