package ashbornrise.modelo.estados;

import ashbornrise.modelo.Combatiente;

/** Inflige un daño fijo al comienzo de cada turno del afectado. */
public class Veneno extends EstadoEfecto {

    private int danioPorTurno;

    public Veneno(int danioPorTurno, int duracion) {
        super("Veneno", duracion);
        this.danioPorTurno = danioPorTurno;
    }

    @Override
    public void aplicarEfecto(Combatiente objetivo) {
        objetivo.perderVida(danioPorTurno);
    }

    @Override
    public TipoEstado getTipo() {
        return TipoEstado.VENENO;
    }

    public int getDanioPorTurno() {
        return danioPorTurno;
    }
}
