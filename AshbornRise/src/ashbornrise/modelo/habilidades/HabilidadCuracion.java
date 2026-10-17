package ashbornrise.modelo.habilidades;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;

import java.util.List;

/** Restaura vida: curación = curacionBase × poder de curación de quien la usa. */
public class HabilidadCuracion extends Habilidad {

    private int curacionBase;
    private int cantidadObjetivos;

    public HabilidadCuracion(String nombre, int costoRecurso, TipoRecurso tipoRecurso,
                             TipoAlcance alcance, int curacionBase, int cantidadObjetivos) {
        super(nombre, costoRecurso, tipoRecurso, alcance);
        this.curacionBase = curacionBase;
        this.cantidadObjetivos = cantidadObjetivos;
    }

    @Override
    public String ejecutar(Combatiente origen, List<Combatiente> objetivos) {
        String resultado = "";
        int cantidad = (int) Math.round(curacionBase * origen.getPoderCuracion());
        for (Combatiente objetivo : objetivos) {
            int curado = objetivo.curar(cantidad);
            resultado = resultado + objetivo.getNombre() + " recupera " + curado + " de vida. ";
        }
        return resultado;
    }

    @Override
    public int getCantidadObjetivos() {
        return cantidadObjetivos;
    }

    public int getCuracionBase() {
        return curacionBase;
    }
}
