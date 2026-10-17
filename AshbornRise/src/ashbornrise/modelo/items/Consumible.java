package ashbornrise.modelo.items;

import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.personajes.Personaje;

/** Ítem de un solo uso con efecto inmediato (pociones, antídoto...). */
public class Consumible extends Item {

    private TipoEfectoConsumible efecto;
    private int magnitud;

    public Consumible(String nombre, String descripcion, TipoEfectoConsumible efecto, int magnitud) {
        super(nombre, descripcion);
        this.efecto = efecto;
        this.magnitud = magnitud;
    }

    @Override
    public String usar(Personaje objetivo) {
        switch (efecto) {
            case RESTAURAR_VIDA:
                return objetivo.getNombre() + " recupera " + objetivo.curar(magnitud) + " de vida.";
            case RESTAURAR_MANA:
                if (objetivo.getTipoRecurso() != TipoRecurso.MANA) {
                    return "No tiene efecto: " + objetivo.getNombre() + " usa Energía.";
                }
                return objetivo.getNombre() + " recupera " + objetivo.recuperarRecurso(magnitud) + " de maná.";
            case QUITAR_VENENO:
                if (objetivo.quitarEstado(TipoEstado.VENENO)) {
                    return objetivo.getNombre() + " ya no está envenenado.";
                }
                return "No tiene efecto: " + objetivo.getNombre() + " no estaba envenenado.";
            default:
                if (objetivo.quitarEstado(TipoEstado.ATURDIMIENTO)) {
                    return objetivo.getNombre() + " ya no está aturdido.";
                }
                return "No tiene efecto: " + objetivo.getNombre() + " no estaba aturdido.";
        }
    }

    public TipoEfectoConsumible getEfecto() {
        return efecto;
    }

    public int getMagnitud() {
        return magnitud;
    }
}
