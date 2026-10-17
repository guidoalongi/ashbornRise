package ashbornrise.modelo.estados;

/**
 * Absorbe una cantidad fija de daño. Desaparece cuando se agota
 * o cuando se termina su duración.
 */
public class Escudo extends EstadoEfecto {

    private int puntosAbsorcion;

    public Escudo(int puntosAbsorcion, int duracion) {
        super("Escudo", duracion);
        this.puntosAbsorcion = puntosAbsorcion;
    }

    @Override
    public int modificarDanioRecibido(int danio) {
        int absorbido = Math.min(danio, puntosAbsorcion);
        puntosAbsorcion = puntosAbsorcion - absorbido;
        return danio - absorbido;
    }

    @Override
    public boolean haExpirado() {
        return super.haExpirado() || puntosAbsorcion <= 0;
    }

    @Override
    public TipoEstado getTipo() {
        return TipoEstado.ESCUDO;
    }

    public int getPuntosAbsorcion() {
        return puntosAbsorcion;
    }
}
