package ashbornrise.modelo.habilidades;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.estados.Aturdimiento;
import ashbornrise.modelo.estados.Escudo;
import ashbornrise.modelo.estados.EstadoEfecto;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.estados.Veneno;

import java.util.List;

/**
 * Aplica un estado (Veneno, Escudo o Aturdimiento) sobre el objetivo.
 * Si "limpiaEstadosNegativos" es true (Bendición), primero intenta quitar
 * Veneno/Aturdimiento; solo si el objetivo no tenía ninguno, aplica el estado.
 */
public class HabilidadEstado extends Habilidad {

    private TipoEstado efectoAplicado;
    private int potencia;   // daño por turno (Veneno) o puntos que absorbe (Escudo)
    private int duracion;
    private boolean limpiaEstadosNegativos;

    public HabilidadEstado(String nombre, int costoRecurso, TipoRecurso tipoRecurso, TipoAlcance alcance,
                           TipoEstado efectoAplicado, int potencia, int duracion,
                           boolean limpiaEstadosNegativos) {
        super(nombre, costoRecurso, tipoRecurso, alcance);
        this.efectoAplicado = efectoAplicado;
        this.potencia = potencia;
        this.duracion = duracion;
        this.limpiaEstadosNegativos = limpiaEstadosNegativos;
    }

    @Override
    public String ejecutar(Combatiente origen, List<Combatiente> objetivos) {
        String resultado = "";
        for (Combatiente objetivo : objetivos) {
            if (limpiaEstadosNegativos && tieneEstadoNegativo(objetivo)) {
                objetivo.quitarEstado(TipoEstado.VENENO);
                objetivo.quitarEstado(TipoEstado.ATURDIMIENTO);
                resultado = resultado + objetivo.getNombre() + " queda libre de estados negativos. ";
            } else if (objetivo.agregarEstado(crearEstado())) {
                resultado = resultado + objetivo.getNombre() + " recibe " + crearEstado().getNombre() + ". ";
            } else {
                resultado = resultado + objetivo.getNombre() + " es inmune. ";
            }
        }
        return resultado;
    }

    /** Crea una instancia nueva del estado que aplica esta habilidad (Creador). */
    private EstadoEfecto crearEstado() {
        switch (efectoAplicado) {
            case VENENO:
                return new Veneno(potencia, duracion);
            case ESCUDO:
                return new Escudo(potencia, duracion);
            default:
                return new Aturdimiento();
        }
    }

    private boolean tieneEstadoNegativo(Combatiente objetivo) {
        return objetivo.tieneEstado(TipoEstado.VENENO) || objetivo.tieneEstado(TipoEstado.ATURDIMIENTO);
    }

    public TipoEstado getEfectoAplicado() {
        return efectoAplicado;
    }
}
