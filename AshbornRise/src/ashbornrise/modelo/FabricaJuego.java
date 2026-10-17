package ashbornrise.modelo;

import ashbornrise.modelo.enemigos.AranaVenenosa;
import ashbornrise.modelo.enemigos.ChamanOscuro;
import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.enemigos.EsqueletoGuerrero;
import ashbornrise.modelo.enemigos.GoblinMerodeador;
import ashbornrise.modelo.enemigos.SenorDeLasSombras;
import ashbornrise.modelo.items.Consumible;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.items.TipoEfectoConsumible;
import ashbornrise.modelo.personajes.Arquero;
import ashbornrise.modelo.personajes.Curador;
import ashbornrise.modelo.personajes.Guerrero;
import ashbornrise.modelo.personajes.Mago;
import ashbornrise.modelo.personajes.Personaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabricación Pura que concentra la "configuración" del juego:
 * la party inicial, los ítems, y qué enemigos y botín tiene cada batalla.
 * Así, si se quiere cambiar el contenido del juego, se toca solo esta clase.
 */
public class FabricaJuego {

    public static final int CANTIDAD_BATALLAS = 8;

    public static final String POCION_VIDA = "Poción de Vida";
    public static final String POCION_MANA = "Poción de Maná";
    public static final String ANTIDOTO = "Antídoto";
    public static final String POCION_TENACIDAD = "Poción de Tenacidad";

    public static Partida crearPartidaNueva() {
        Equipo equipo = new Equipo();
        equipo.agregarIntegrante(new Guerrero("Bram"));
        equipo.agregarIntegrante(new Mago("Yssa"));
        equipo.agregarIntegrante(new Arquero("Kel"));
        equipo.agregarIntegrante(new Curador("Nia"));

        Inventario inventario = new Inventario();
        inventario.agregarOro(50);
        for (int i = 0; i < 3; i++) {
            inventario.agregarItem(crearConsumible(POCION_VIDA));
        }
        inventario.agregarItem(crearConsumible(POCION_MANA));
        inventario.agregarItem(crearConsumible(POCION_MANA));
        inventario.agregarItem(crearConsumible(ANTIDOTO));
        inventario.agregarItem(crearConsumible(POCION_TENACIDAD));

        return new Partida(equipo, inventario, 0);
    }

    /** Crea un personaje a partir del nombre de su clase (se usa al cargar una partida). */
    public static Personaje crearPersonaje(String clase, String nombre) {
        switch (clase) {
            case "Guerrero":
                return new Guerrero(nombre);
            case "Mago":
                return new Mago(nombre);
            case "Arquero":
                return new Arquero(nombre);
            case "Curador":
                return new Curador(nombre);
            default:
                throw new IllegalArgumentException("Clase de personaje desconocida: " + clase);
        }
    }

    public static Item crearConsumible(String nombre) {
        switch (nombre) {
            case POCION_VIDA:
                return new Consumible(POCION_VIDA, "Restaura 50 de vida",
                        TipoEfectoConsumible.RESTAURAR_VIDA, 50);
            case POCION_MANA:
                return new Consumible(POCION_MANA, "Restaura 30 de maná",
                        TipoEfectoConsumible.RESTAURAR_MANA, 30);
            case ANTIDOTO:
                return new Consumible(ANTIDOTO, "Quita el Veneno",
                        TipoEfectoConsumible.QUITAR_VENENO, 0);
            case POCION_TENACIDAD:
                return new Consumible(POCION_TENACIDAD, "Quita el Aturdimiento",
                        TipoEfectoConsumible.QUITAR_ATURDIMIENTO, 0);
            default:
                throw new IllegalArgumentException("Ítem desconocido: " + nombre);
        }
    }

    /** Enemigos de cada batalla de la secuencia. La 8 es el jefe final. */
    public static List<Enemigo> crearEnemigos(int n) {
        List<Enemigo> enemigos = new ArrayList<>();
        switch (n) {
            case 1:
                enemigos.add(new GoblinMerodeador("Goblin Merodeador A", n));
                enemigos.add(new GoblinMerodeador("Goblin Merodeador B", n));
                break;
            case 2:
                enemigos.add(new GoblinMerodeador("Goblin Merodeador", n));
                enemigos.add(new AranaVenenosa("Araña Venenosa", n));
                break;
            case 3:
                enemigos.add(new GoblinMerodeador("Goblin Merodeador", n));
                enemigos.add(new EsqueletoGuerrero("Esqueleto Guerrero", n));
                enemigos.add(new ChamanOscuro("Chamán Oscuro", n));
                break;
            case 4:
                enemigos.add(new AranaVenenosa("Araña Venenosa A", n));
                enemigos.add(new AranaVenenosa("Araña Venenosa B", n));
                enemigos.add(new EsqueletoGuerrero("Esqueleto Guerrero", n));
                break;
            case 5:
                enemigos.add(new EsqueletoGuerrero("Esqueleto Guerrero A", n));
                enemigos.add(new EsqueletoGuerrero("Esqueleto Guerrero B", n));
                enemigos.add(new ChamanOscuro("Chamán Oscuro", n));
                break;
            case 6:
                enemigos.add(new GoblinMerodeador("Goblin Merodeador", n));
                enemigos.add(new AranaVenenosa("Araña Venenosa", n));
                enemigos.add(new ChamanOscuro("Chamán Oscuro", n));
                break;
            case 7:
                enemigos.add(new EsqueletoGuerrero("Esqueleto Guerrero", n));
                enemigos.add(new AranaVenenosa("Araña Venenosa", n));
                enemigos.add(new ChamanOscuro("Chamán Oscuro A", n));
                enemigos.add(new ChamanOscuro("Chamán Oscuro B", n));
                break;
            default:
                enemigos.add(new SenorDeLasSombras("Señor de las Sombras", n));
                break;
        }
        return enemigos;
    }

    /** Ítems que se ganan al vencer cada batalla. */
    public static List<Item> crearBotin(int n) {
        List<Item> botin = new ArrayList<>();
        botin.add(crearConsumible(POCION_VIDA));
        if (n % 2 == 0) {
            botin.add(crearConsumible(POCION_MANA));
        }
        if (n == 2 || n == 4 || n == 6) {
            botin.add(crearConsumible(ANTIDOTO));
        }
        if (n == 7) {
            botin.add(crearConsumible(POCION_TENACIDAD));
            botin.add(crearConsumible(POCION_VIDA));
        }
        return botin;
    }
}
