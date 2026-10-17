package ashbornrise.modelo.enemigos;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionAtacar;
import ashbornrise.modelo.acciones.AccionDefender;
import ashbornrise.modelo.estados.TipoEstado;

/** Defensivo: alterna entre atacar y defender. Es inmune al Veneno. */
public class EsqueletoGuerrero extends Enemigo {

    private boolean inmuneAVeneno;
    private boolean ultimoFueAtaque;

    public EsqueletoGuerrero(String nombre, int numeroBatalla) {
        super(nombre, escalar(90, numeroBatalla), escalar(15, numeroBatalla), escalar(16, numeroBatalla),
                6, escalar(40, numeroBatalla), escalar(20, numeroBatalla));
        this.inmuneAVeneno = true;
        this.ultimoFueAtaque = false;
    }

    @Override
    public Accion decidirAccion(Batalla batalla) {
        ultimoFueAtaque = !ultimoFueAtaque;
        if (ultimoFueAtaque) {
            return new AccionAtacar(elegirAlAzar(batalla.getAliadosVivos()));
        }
        return new AccionDefender();
    }

    @Override
    public boolean puedeRecibirEstado(TipoEstado tipo) {
        if (inmuneAVeneno && tipo == TipoEstado.VENENO) {
            return false;
        }
        return true;
    }

    public boolean isInmuneAVeneno() {
        return inmuneAVeneno;
    }
}
