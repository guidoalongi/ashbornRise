package ashbornrise.modelo.items;

import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.personajes.Personaje;

/** Quita un estado concreto (el Antídoto quita el Veneno, la Poción de Tenacidad el Aturdimiento). */
public class QuitarEstado implements EfectoConsumible {

    private TipoEstado estado;
    private String descripcionEstado;   // "envenenado", "aturdido" (para el mensaje)

    public QuitarEstado(TipoEstado estado, String descripcionEstado) {
        this.estado = estado;
        this.descripcionEstado = descripcionEstado;
    }

    @Override
    public String aplicar(Personaje objetivo) {
        if (objetivo.quitarEstado(estado)) {
            return objetivo.getNombre() + " ya no está " + descripcionEstado + ".";
        }
        return "No tiene efecto: " + objetivo.getNombre() + " no estaba " + descripcionEstado + ".";
    }
}
