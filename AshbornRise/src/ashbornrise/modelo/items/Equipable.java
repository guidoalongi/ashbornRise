package ashbornrise.modelo.items;

import ashbornrise.modelo.Inventario;
import ashbornrise.modelo.personajes.Personaje;

/**
 * Ítem que se equipa en una ranura (arma, armadura o accesorio) y modifica las estadísticas
 * del personaje mientras lo tiene puesto. Al equiparlo sale del inventario; si en esa ranura
 * había otro, ese vuelve al inventario.
 */
public class Equipable extends Item {

    private TipoRanura ranura;
    private int bonusAtaque;
    private int bonusDefensa;
    private int bonusVelocidad;

    public Equipable(String nombre, TipoRanura ranura, int bonusAtaque, int bonusDefensa, int bonusVelocidad) {
        super(nombre, armarDescripcion(ranura, bonusAtaque, bonusDefensa, bonusVelocidad));
        this.ranura = ranura;
        this.bonusAtaque = bonusAtaque;
        this.bonusDefensa = bonusDefensa;
        this.bonusVelocidad = bonusVelocidad;
    }

    @Override
    public String usar(Personaje objetivo, Inventario inventario) {
        inventario.quitarItem(this);
        Equipable anterior = objetivo.equipar(this);
        if (anterior != null) {
            inventario.agregarItem(anterior);
            return objetivo.getNombre() + " equipa " + getNombre() + " (guarda " + anterior.getNombre() + ").";
        }
        return objetivo.getNombre() + " equipa " + getNombre() + ".";
    }

    /** Se puede equipar aunque el personaje esté caído. */
    @Override
    public boolean puedeUsarseSobre(Personaje objetivo) {
        return true;
    }

    @Override
    public boolean esEquipable() {
        return true;
    }

    /** Cambiar el equipo lleva tiempo: solo se hace entre batallas. */
    @Override
    public boolean sePuedeUsarEnBatalla() {
        return false;
    }

    private static String armarDescripcion(TipoRanura ranura, int atq, int def, int vel) {
        String texto = ranura.getNombre() + ":";
        if (atq != 0) {
            texto = texto + " " + conSigno(atq) + " ATQ";
        }
        if (def != 0) {
            texto = texto + " " + conSigno(def) + " DEF";
        }
        if (vel != 0) {
            texto = texto + " " + conSigno(vel) + " VEL";
        }
        return texto;
    }

    private static String conSigno(int valor) {
        return valor > 0 ? "+" + valor : String.valueOf(valor);
    }

    public TipoRanura getRanura() {
        return ranura;
    }

    public int getBonusAtaque() {
        return bonusAtaque;
    }

    public int getBonusDefensa() {
        return bonusDefensa;
    }

    public int getBonusVelocidad() {
        return bonusVelocidad;
    }
}
