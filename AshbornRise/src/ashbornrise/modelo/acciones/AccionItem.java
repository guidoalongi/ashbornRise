package ashbornrise.modelo.acciones;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

public class AccionItem extends Accion {

    private Personaje usuario;
    private Item item;
    private Personaje objetivo;

    public AccionItem(Personaje usuario, Item item, Personaje objetivo) {
        this.usuario = usuario;
        this.item = item;
        this.objetivo = objetivo;
    }

    @Override
    public void ejecutar(Combatiente actor, Batalla batalla) {
        String resultado = usuario.usarItem(item, objetivo);
        batalla.consumirItem(item);
        batalla.registrar(usuario.getNombre() + " usa " + item.getNombre() + ". " + resultado);
    }

    public Item getItem() {
        return item;
    }

    public Personaje getObjetivo() {
        return objetivo;
    }
}
