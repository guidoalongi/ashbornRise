package ashbornrise.modelo.fabricas;

import ashbornrise.modelo.enemigos.AranaVenenosa;
import ashbornrise.modelo.enemigos.ChamanOscuro;
import ashbornrise.modelo.enemigos.EsqueletoGuerrero;
import ashbornrise.modelo.enemigos.GoblinMerodeador;
import ashbornrise.modelo.enemigos.SenorDeLasSombras;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.items.Consumible;
import ashbornrise.modelo.items.Equipable;
import ashbornrise.modelo.items.QuitarEstado;
import ashbornrise.modelo.items.RestaurarMana;
import ashbornrise.modelo.items.RestaurarVida;
import ashbornrise.modelo.items.TipoRanura;
import ashbornrise.modelo.personajes.Arquero;
import ashbornrise.modelo.personajes.Curador;
import ashbornrise.modelo.personajes.Guerrero;
import ashbornrise.modelo.personajes.Mago;

/**
 * El contenido del juego base: qué clases de personaje, ítems y batallas existen.
 *
 * Es configuración (datos), no lógica: no hay ningún switch ni if que decida qué crear.
 * Las fábricas (RegistroPersonajes, CatalogoItems, CatalogoBatallas) no cambian cuando se agrega
 * contenido; solo se registra lo nuevo, acá o desde cualquier otra clase.
 */
public class ContenidoBase {

    public static final String POCION_VIDA = "Poción de Vida";
    public static final String POCION_MANA = "Poción de Maná";
    public static final String ANTIDOTO = "Antídoto";
    public static final String POCION_TENACIDAD = "Poción de Tenacidad";

    public static final String ESPADA_HIERRO = "Espada de Hierro";
    public static final String ESCUDO_ROBLE = "Escudo de Roble";
    public static final String BOTAS_LIGERAS = "Botas Ligeras";
    public static final String ARCO_ELFICO = "Arco Élfico";
    public static final String COTA_MALLA = "Cota de Malla";
    public static final String AMULETO_HALCON = "Amuleto del Halcón";
    public static final String HOJA_RUNICA = "Hoja Rúnica";

    public static void registrarPersonajes(RegistroPersonajes registro) {
        registro.registrar("Guerrero", Guerrero::new);
        registro.registrar("Mago", Mago::new);
        registro.registrar("Arquero", Arquero::new);
        registro.registrar("Curador", Curador::new);
    }

    public static void registrarItems(CatalogoItems catalogo) {
        // Consumibles: cada uno con su efecto (sin switch).
        catalogo.registrar(POCION_VIDA, () -> new Consumible(POCION_VIDA, "Restaura 50 de vida",
                new RestaurarVida(50)));
        catalogo.registrar(POCION_MANA, () -> new Consumible(POCION_MANA, "Restaura 30 de maná",
                new RestaurarMana(30)));
        catalogo.registrar(ANTIDOTO, () -> new Consumible(ANTIDOTO, "Quita el Veneno",
                new QuitarEstado(TipoEstado.VENENO, "envenenado")));
        catalogo.registrar(POCION_TENACIDAD, () -> new Consumible(POCION_TENACIDAD, "Quita el Aturdimiento",
                new QuitarEstado(TipoEstado.ATURDIMIENTO, "aturdido")));

        // Equipables: ranura y bonus de ATQ / DEF / VEL.
        catalogo.registrar(ESPADA_HIERRO, () -> new Equipable(ESPADA_HIERRO, TipoRanura.ARMA, 4, 0, 0));
        catalogo.registrar(ESCUDO_ROBLE, () -> new Equipable(ESCUDO_ROBLE, TipoRanura.ARMADURA, 0, 4, 0));
        catalogo.registrar(BOTAS_LIGERAS, () -> new Equipable(BOTAS_LIGERAS, TipoRanura.ACCESORIO, 0, 0, 3));
        catalogo.registrar(ARCO_ELFICO, () -> new Equipable(ARCO_ELFICO, TipoRanura.ARMA, 6, 0, 1));
        catalogo.registrar(COTA_MALLA, () -> new Equipable(COTA_MALLA, TipoRanura.ARMADURA, 0, 7, -1));
        catalogo.registrar(AMULETO_HALCON, () -> new Equipable(AMULETO_HALCON, TipoRanura.ACCESORIO, 2, 0, 3));
        catalogo.registrar(HOJA_RUNICA, () -> new Equipable(HOJA_RUNICA, TipoRanura.ARMA, 9, 0, 0));
    }

    /** Las 8 batallas, en orden. La 8 es el jefe final. */
    public static void registrarBatallas(CatalogoBatallas batallas) {
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Goblin Merodeador A", GoblinMerodeador::new)
                .enemigo("Goblin Merodeador B", GoblinMerodeador::new)
                .botin(POCION_VIDA).botin(BOTAS_LIGERAS));
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Goblin Merodeador", GoblinMerodeador::new)
                .enemigo("Araña Venenosa", AranaVenenosa::new)
                .botin(POCION_VIDA).botin(POCION_MANA).botin(ANTIDOTO));
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Goblin Merodeador", GoblinMerodeador::new)
                .enemigo("Esqueleto Guerrero", EsqueletoGuerrero::new)
                .enemigo("Chamán Oscuro", ChamanOscuro::new)
                .botin(POCION_VIDA).botin(ARCO_ELFICO));
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Araña Venenosa A", AranaVenenosa::new)
                .enemigo("Araña Venenosa B", AranaVenenosa::new)
                .enemigo("Esqueleto Guerrero", EsqueletoGuerrero::new)
                .botin(POCION_VIDA).botin(POCION_MANA).botin(ANTIDOTO));
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Esqueleto Guerrero A", EsqueletoGuerrero::new)
                .enemigo("Esqueleto Guerrero B", EsqueletoGuerrero::new)
                .enemigo("Chamán Oscuro", ChamanOscuro::new)
                .botin(POCION_VIDA).botin(COTA_MALLA));
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Goblin Merodeador", GoblinMerodeador::new)
                .enemigo("Araña Venenosa", AranaVenenosa::new)
                .enemigo("Chamán Oscuro", ChamanOscuro::new)
                .botin(POCION_VIDA).botin(POCION_MANA).botin(ANTIDOTO).botin(AMULETO_HALCON));
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Esqueleto Guerrero", EsqueletoGuerrero::new)
                .enemigo("Araña Venenosa", AranaVenenosa::new)
                .enemigo("Chamán Oscuro A", ChamanOscuro::new)
                .enemigo("Chamán Oscuro B", ChamanOscuro::new)
                .botin(POCION_VIDA).botin(POCION_TENACIDAD).botin(POCION_VIDA).botin(HOJA_RUNICA));
        batallas.agregar(new DefinicionBatalla()
                .enemigo("Señor de las Sombras", SenorDeLasSombras::new)
                .botin(POCION_VIDA).botin(POCION_MANA));
    }
}
