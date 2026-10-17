package ashbornrise.modelo.enemigos;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionAtacar;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.estados.Veneno;

/** Daño directo bajo, pero envenena en cada ataque. Prefiere objetivos sin veneno. */
public class AranaVenenosa extends Enemigo {

    private int potenciaVeneno;

    public AranaVenenosa(String nombre, int numeroBatalla) {
        super(nombre, escalar(65, numeroBatalla), escalar(11, numeroBatalla), escalar(8, numeroBatalla),
                12, escalar(35, numeroBatalla), escalar(15, numeroBatalla));
        this.potenciaVeneno = escalar(6, numeroBatalla);
    }

    @Override
    public Accion decidirAccion(Batalla batalla) {
        for (Combatiente aliado : batalla.getAliadosVivos()) {
            if (!aliado.tieneEstado(TipoEstado.VENENO)) {
                return new AccionAtacar(aliado);
            }
        }
        return new AccionAtacar(elegirAlAzar(batalla.getAliadosVivos()));
    }

    @Override
    public void alGolpear(Combatiente objetivo, Batalla batalla) {
        if (objetivo.estaVivo() && objetivo.agregarEstado(new Veneno(potenciaVeneno, 3))) {
            batalla.registrar(nombre + " aplica Veneno a " + objetivo.getNombre() + ".");
        }
    }

    public int getPotenciaVeneno() {
        return potenciaVeneno;
    }
}
