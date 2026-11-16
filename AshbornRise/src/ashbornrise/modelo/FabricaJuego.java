package ashbornrise.modelo;

import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.fabricas.CatalogoBatallas;
import ashbornrise.modelo.fabricas.CatalogoItems;
import ashbornrise.modelo.fabricas.ContenidoBase;
import ashbornrise.modelo.fabricas.RegistroPersonajes;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabricación Pura: punto de acceso único para crear los objetos del juego.
 *
 * Ya no decide con switch qué clase crear: delega en tres registros (personajes, ítems y batallas)
 * que se llenan con el contenido de ContenidoBase. Agregar un personaje, un ítem o una batalla
 * nueva no obliga a modificar esta clase: se registra en el registro correspondiente.
 */
public class FabricaJuego {

    private static final RegistroPersonajes PERSONAJES = new RegistroPersonajes();
    private static final CatalogoItems ITEMS = new CatalogoItems();
    private static final CatalogoBatallas BATALLAS = new CatalogoBatallas();

    static {
        ContenidoBase.registrarPersonajes(PERSONAJES);
        ContenidoBase.registrarItems(ITEMS);
        ContenidoBase.registrarBatallas(BATALLAS);
    }

    public static Partida crearPartidaNueva() {
        Equipo equipo = new Equipo();
        equipo.agregarIntegrante(crearPersonaje("Guerrero", "Bram"));
        equipo.agregarIntegrante(crearPersonaje("Mago", "Yssa"));
        equipo.agregarIntegrante(crearPersonaje("Arquero", "Kel"));
        equipo.agregarIntegrante(crearPersonaje("Curador", "Nia"));

        Inventario inventario = new Inventario();
        inventario.agregarOro(50);
        for (int i = 0; i < 3; i++) {
            inventario.agregarItem(crearItem(ContenidoBase.POCION_VIDA));
        }
        inventario.agregarItem(crearItem(ContenidoBase.POCION_MANA));
        inventario.agregarItem(crearItem(ContenidoBase.POCION_MANA));
        inventario.agregarItem(crearItem(ContenidoBase.ANTIDOTO));
        inventario.agregarItem(crearItem(ContenidoBase.POCION_TENACIDAD));
        inventario.agregarItem(crearItem(ContenidoBase.ESPADA_HIERRO));
        inventario.agregarItem(crearItem(ContenidoBase.ESCUDO_ROBLE));
        return new Partida(equipo, inventario, 0);
    }

    /** Crea un personaje a partir del nombre de su clase (se usa al cargar una partida). */
    public static Personaje crearPersonaje(String clase, String nombre) {
        return PERSONAJES.crear(clase, nombre);
    }

    /** Crea un ítem (consumible o equipable) a partir de su nombre. */
    public static Item crearItem(String nombre) {
        return ITEMS.crear(nombre);
    }

    /** Enemigos de la batalla n de la secuencia. */
    public static List<Enemigo> crearEnemigos(int n) {
        return BATALLAS.getBatalla(n).crearEnemigos(n);
    }

    /** Ítems que se ganan al vencer la batalla n. */
    public static List<Item> crearBotin(int n) {
        List<Item> botin = new ArrayList<>();
        for (String nombre : BATALLAS.getBatalla(n).getBotin()) {
            botin.add(crearItem(nombre));
        }
        return botin;
    }

    public static int getCantidadBatallas() {
        return BATALLAS.getCantidad();
    }

    // Acceso a los registros para agregar contenido nuevo sin modificar esta clase.

    public static RegistroPersonajes getRegistroPersonajes() {
        return PERSONAJES;
    }

    public static CatalogoItems getCatalogoItems() {
        return ITEMS;
    }

    public static CatalogoBatallas getCatalogoBatallas() {
        return BATALLAS;
    }
}
