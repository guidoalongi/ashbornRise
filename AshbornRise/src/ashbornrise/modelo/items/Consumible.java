package ashbornrise.modelo.items;

import ashbornrise.modelo.Inventario;
import ashbornrise.modelo.personajes.Personaje;

/**
 * Ítem de un solo uso con efecto inmediato (pociones, antídoto...).
 * El efecto concreto lo resuelve un EfectoConsumible (sin switch).
 */
public class Consumible extends Item {

    private EfectoConsumible efecto;

    public Consumible(String nombre, String descripcion, EfectoConsumible efecto) {
        super(nombre, descripcion);
        this.efecto = efecto;
    }

    @Override
    public String usar(Personaje objetivo, Inventario inventario) {
        inventario.quitarItem(this);
        return efecto.aplicar(objetivo);
    }

    public EfectoConsumible getEfecto() {
        return efecto;
    }
}
