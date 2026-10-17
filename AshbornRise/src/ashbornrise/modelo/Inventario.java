package ashbornrise.modelo;

import ashbornrise.modelo.items.Item;

import java.util.ArrayList;
import java.util.List;

/** Inventario compartido por todo el equipo: ítems y oro. */
public class Inventario {

    private List<Item> items;
    private int oro;

    public Inventario() {
        this.items = new ArrayList<>();
        this.oro = 0;
    }

    public void agregarItem(Item i) {
        items.add(i);
    }

    public void quitarItem(Item i) {
        items.remove(i);
    }

    public boolean contiene(Item i) {
        return items.contains(i);
    }

    public List<Item> obtenerItems() {
        return new ArrayList<>(items);
    }

    /** Un ítem de cada nombre (para mostrar "Poción de Vida x3" sin repetir). */
    public List<Item> obtenerItemsDistintos() {
        List<Item> distintos = new ArrayList<>();
        List<String> nombresVistos = new ArrayList<>();
        for (Item i : items) {
            if (!nombresVistos.contains(i.getNombre())) {
                nombresVistos.add(i.getNombre());
                distintos.add(i);
            }
        }
        return distintos;
    }

    public int cantidadDe(String nombreItem) {
        int cantidad = 0;
        for (Item i : items) {
            if (i.getNombre().equals(nombreItem)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public void agregarOro(int cantidad) {
        oro = oro + cantidad;
    }

    /** Quita oro sin quedar en negativo. Devuelve lo que realmente se quitó. */
    public int quitarOro(int cantidad) {
        int quitado = Math.min(cantidad, oro);
        oro = oro - quitado;
        return quitado;
    }

    public int getOro() {
        return oro;
    }
}
