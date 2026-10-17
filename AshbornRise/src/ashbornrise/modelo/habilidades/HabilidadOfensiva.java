package ashbornrise.modelo.habilidades;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;

import java.util.List;

/** Inflige daño a uno o varios enemigos: daño = daño del origen × multiplicador. */
public class HabilidadOfensiva extends Habilidad {

    private double multiplicadorDanio;
    private int cantidadObjetivos;

    public HabilidadOfensiva(String nombre, int costoRecurso, TipoRecurso tipoRecurso,
                             TipoAlcance alcance, double multiplicadorDanio, int cantidadObjetivos) {
        super(nombre, costoRecurso, tipoRecurso, alcance);
        this.multiplicadorDanio = multiplicadorDanio;
        this.cantidadObjetivos = cantidadObjetivos;
    }

    @Override
    public String ejecutar(Combatiente origen, List<Combatiente> objetivos) {
        String resultado = "";
        for (Combatiente objetivo : objetivos) {
            if (objetivo.estaVivo()) {
                int danio = (int) Math.round(origen.calcularDanio() * multiplicadorDanio);
                int recibido = objetivo.recibirDanio(danio);
                resultado = resultado + objetivo.getNombre() + " recibe " + recibido + " de daño. ";
            }
        }
        return resultado;
    }

    @Override
    public int getCantidadObjetivos() {
        return cantidadObjetivos;
    }

    public double getMultiplicadorDanio() {
        return multiplicadorDanio;
    }
}
