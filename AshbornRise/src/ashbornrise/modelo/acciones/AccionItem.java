package ashbornrise.modelo.acciones;

import ashbornrise.modelo.AccionInvalidaException;
import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import java.util.ArrayList;
import java.util.List;

public class AccionItem extends Accion {

    private Personaje usuario;
    private Item item;
    private Personaje objetivo;

    public AccionItem(Personaje usuario, Item item, Personaje objetivo) {
        this.usuario = usuario;
        this.item = item;
        this.objetivo = objetivo;
    }

    /** El ítem tiene que estar en el inventario, servir en batalla y usarse sobre un aliado válido. */
    @Override
    public void validar(Combatiente actor, Batalla batalla) {
        if (usuario != actor) {
            throw new AccionInvalidaException("Solo el personaje que tiene el turno puede usar un ítem.");
        }
        if (item == null || !batalla.getInventario().contiene(item)) {
            throw new AccionInvalidaException("Ese ítem no está en el inventario.");
        }
        if (!item.sePuedeUsarEnBatalla()) {
            throw new AccionInvalidaException(item.getNombre() + " no se puede usar en batalla.");
        }
        if (objetivo == null || !batalla.participa(objetivo) || !actor.esAliadoDe(objetivo)) {
            throw new AccionInvalidaException("Los ítems solo se usan sobre aliados.");
        }
        if (!item.puedeUsarseSobre(objetivo)) {
            throw new AccionInvalidaException(objetivo.getNombre() + " está fuera de combate.");
        }
    }

    @Override
    public void ejecutar(Combatiente actor, Batalla batalla) {
        String resultado = usuario.usarItem(item, objetivo, batalla.getInventario());
        batalla.registrar(usuario.getNombre() + " usa " + item.getNombre() + ". " + resultado);
    }

    public Item getItem() {
        return item;
    }

    public Personaje getObjetivo() {
        return objetivo;
    }

    @Override
    public TipoAccion getTipo() {
        return TipoAccion.ITEM;
    }

    @Override
    public String getNombreDeLoUsado() {
        return item.getNombre();
    }

    @Override
    public List<Combatiente> getObjetivos() {
        List<Combatiente> lista = new ArrayList<>();
        lista.add(objetivo);
        return lista;
    }
}
