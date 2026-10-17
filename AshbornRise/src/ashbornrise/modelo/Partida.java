package ashbornrise.modelo;

import java.util.List;

import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.items.Item;

/** Todo lo necesario para continuar el juego más tarde: equipo, inventario y progreso. */
public class Partida {

    private Equipo equipo;
    private Inventario inventario;
    private int progresoBatallas;   // cuántas batallas se ganaron
    private String fechaGuardado;

    public Partida(Equipo equipo, Inventario inventario, int progresoBatallas) {
        this.equipo = equipo;
        this.inventario = inventario;
        this.progresoBatallas = progresoBatallas;
        this.fechaGuardado = "";
    }

    /** Creador: la Partida tiene el equipo, el inventario y sabe qué batalla sigue. */
    public Batalla crearSiguienteBatalla() {
        int numero = getNumeroBatallaActual();
        List<Enemigo> enemigos = FabricaJuego.crearEnemigos(numero);
        List<Item> botin = FabricaJuego.crearBotin(numero);
        return new Batalla(numero, equipo, enemigos, inventario, botin);
    }

    public void registrarVictoria() {
        progresoBatallas++;
        equipo.recuperarTrasBatalla();
    }

    public boolean estaCompleta() {
        return progresoBatallas >= FabricaJuego.CANTIDAD_BATALLAS;
    }

    /** Número de la próxima batalla a jugar (empieza en 1). */
    public int getNumeroBatallaActual() {
        return progresoBatallas + 1;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public int getProgresoBatallas() {
        return progresoBatallas;
    }

    public String getFechaGuardado() {
        return fechaGuardado;
    }

    public void setFechaGuardado(String fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
    }
}
