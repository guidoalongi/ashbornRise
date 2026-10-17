package ashbornrise.modelo.acciones;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;

public class AccionDefender extends Accion {

    @Override
    public void ejecutar(Combatiente actor, Batalla batalla) {
        actor.defenderse();
        batalla.registrar(actor.getNombre() + " se pone en guardia.");
    }
}
