package ashbornrise.modelo.enemigos;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionAtacar;

/** Agresivo: siempre ataca al aliado con menos vida y, si golpea, roba oro. */
public class GoblinMerodeador extends Enemigo {

    private static final int ORO_POR_ROBO = 5;

    private boolean robaOro;

    public GoblinMerodeador(String nombre, int numeroBatalla) {
        super(nombre, escalar(60, numeroBatalla), escalar(14, numeroBatalla), escalar(6, numeroBatalla),
                13, escalar(30, numeroBatalla), escalar(15, numeroBatalla));
        this.robaOro = true;
    }

    @Override
    public Accion decidirAccion(Batalla batalla) {
        return new AccionAtacar(elegirConMenosVida(batalla.getAliadosVivos()));
    }

    @Override
    public void alGolpear(Combatiente objetivo, Batalla batalla) {
        if (!robaOro) {
            return;
        }
        int robado = batalla.robarOro(ORO_POR_ROBO);
        if (robado > 0) {
            batalla.registrar(nombre + " le roba " + robado + " de oro al grupo.");
        }
    }

    public boolean isRobaOro() {
        return robaOro;
    }
}
