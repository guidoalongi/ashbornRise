package ashbornrise.modelo.estados;

import ashbornrise.modelo.Combatiente;

/**
 * Estado alterado (Veneno, Escudo, Aturdimiento).
 * Cada estado sabe aplicar su propio efecto y llevar su propia duración (Experto).
 * Los métodos con comportamiento "por defecto" permiten que Combatiente no tenga
 * que preguntar de qué tipo es cada estado (Polimorfismo, sin instanceof).
 */
public abstract class EstadoEfecto {

    private String nombre;
    private int duracionRestante;

    public EstadoEfecto(String nombre, int duracion) {
        this.nombre = nombre;
        this.duracionRestante = duracion;
    }

    /** Efecto que se produce al comienzo de cada turno del afectado. Por defecto, ninguno. */
    public void aplicarEfecto(Combatiente objetivo) {
    }

    /** Permite modificar el daño que recibe el afectado. Por defecto no lo cambia. */
    public int modificarDanioRecibido(int danio) {
        return danio;
    }

    /** Si devuelve true, el afectado pierde su turno. Por defecto, false. */
    public boolean impideActuar() {
        return false;
    }

    /** Descuenta un turno de duración. */
    public void tick() {
        if (duracionRestante > 0) {
            duracionRestante--;
        }
    }

    public boolean haExpirado() {
        return duracionRestante <= 0;
    }

    public abstract TipoEstado getTipo();

    public String getNombre() {
        return nombre;
    }

    public int getDuracionRestante() {
        return duracionRestante;
    }
}
