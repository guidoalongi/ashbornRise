package ashbornrise.modelo.habilidades;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.estados.CreadorEstado;
import ashbornrise.modelo.estados.EstadoEfecto;
import ashbornrise.modelo.estados.TipoEstado;

import java.util.List;

/**
 * Aplica un estado (Veneno, Escudo, Aturdimiento...) sobre los objetivos.
 * Si "limpiaEstadosNegativos" es true (Bendición), primero intenta quitar los estados
 * negativos; solo si el objetivo no tenía ninguno, aplica el estado.
 *
 * No decide con un switch qué estado crear: recibe un CreadorEstado ya configurado
 * (por ejemplo  () -> new Veneno(6, 3)) y le delega la creación. Así, un estado nuevo
 * no obliga a modificar esta clase (Polimorfismo / Variaciones Protegidas).
 */
public class HabilidadEstado extends Habilidad {

    private CreadorEstado creadorEstado;
    private boolean limpiaEstadosNegativos;

    public HabilidadEstado(String nombre, int costoRecurso, TipoRecurso tipoRecurso, TipoAlcance alcance,
                           CreadorEstado creadorEstado, boolean limpiaEstadosNegativos) {
        super(nombre, costoRecurso, tipoRecurso, alcance);
        this.creadorEstado = creadorEstado;
        this.limpiaEstadosNegativos = limpiaEstadosNegativos;
    }

    @Override
    public String ejecutar(Combatiente origen, List<Combatiente> objetivos) {
        String resultado = "";
        for (Combatiente objetivo : objetivos) {
            if (limpiaEstadosNegativos && objetivo.quitarEstadosNegativos()) {
                resultado = resultado + objetivo.getNombre() + " queda libre de estados negativos. ";
            } else {
                EstadoEfecto estado = creadorEstado.crear();   // una instancia nueva por objetivo
                if (objetivo.agregarEstado(estado)) {
                    resultado = resultado + objetivo.getNombre() + " recibe " + estado.getNombre() + ". ";
                } else {
                    resultado = resultado + objetivo.getNombre() + " es inmune. ";
                }
            }
        }
        return resultado;
    }

    /** Tipo del estado que aplica (se consulta creando uno de muestra). */
    @Override
    public TipoEstado getEstadoAplicado() {
        return creadorEstado.crear().getTipo();
    }
}
