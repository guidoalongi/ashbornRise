package ashbornrise.modelo.fabricas;

import ashbornrise.modelo.items.Item;

/** Sabe crear un ítem ya configurado. Ej.:  () -> new Consumible("Poción de Vida", ...). */
public interface CreadorItem {

    Item crear();
}
