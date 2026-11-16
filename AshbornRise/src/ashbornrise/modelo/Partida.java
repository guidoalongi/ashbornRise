package ashbornrise.modelo;

import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.items.Equipable;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.items.TipoRanura;
import ashbornrise.modelo.personajes.Personaje;

import java.util.List;

/** Todo lo necesario para continuar el juego más tarde: equipo, inventario y progreso. */
public class Partida {

    private Equipo equipo;
    private Inventario inventario;
    private int progresoBatallas;   // cuántas batallas se ganaron
    private String fechaGuardado;

    /**
     * La regla "el equipo tiene entre 2 y 4 personajes" la hace cumplir el propio modelo:
     * no se puede crear una Partida inválida, venga de donde venga (nueva partida o archivo cargado).
     */
    public Partida(Equipo equipo, Inventario inventario, int progresoBatallas) {
        if (equipo == null || !equipo.esValido()) {
            throw new IllegalArgumentException("El equipo debe tener entre " + Equipo.MINIMO
                    + " y " + Equipo.MAXIMO + " personajes.");
        }
        if (inventario == null) {
            throw new IllegalArgumentException("La partida necesita un inventario.");
        }
        if (progresoBatallas < 0) {
            throw new IllegalArgumentException("El progreso no puede ser negativo.");
        }
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

    // ---------------------------------------------------------------- inventario y equipo (entre batallas)

    /**
     * Usa un ítem del inventario sobre un integrante del equipo (pociones o equipables).
     * Valida las reglas acá, en el modelo; el controlador solo traduce el pedido.
     */
    public String usarItem(Item item, Personaje objetivo) {
        if (item == null || !inventario.contiene(item)) {
            throw new AccionInvalidaException("Ese ítem no está en el inventario.");
        }
        validarIntegrante(objetivo);
        if (!item.puedeUsarseSobre(objetivo)) {
            throw new AccionInvalidaException(objetivo.getNombre() + " está fuera de combate.");
        }
        return objetivo.usarItem(item, objetivo, inventario);
    }

    /** Saca lo que el personaje tiene en esa ranura y lo devuelve al inventario. */
    public String desequipar(Personaje personaje, TipoRanura ranura) {
        validarIntegrante(personaje);
        Equipable quitado = personaje.desequipar(ranura);
        if (quitado == null) {
            throw new AccionInvalidaException(personaje.getNombre() + " no tiene nada en " + ranura.getNombre() + ".");
        }
        inventario.agregarItem(quitado);
        return personaje.getNombre() + " se quita " + quitado.getNombre() + ".";
    }

    private void validarIntegrante(Personaje p) {
        if (p == null || !equipo.getIntegrantes().contains(p)) {
            throw new AccionInvalidaException("Ese personaje no es parte del equipo.");
        }
    }

    public boolean estaCompleta() {
        return progresoBatallas >= FabricaJuego.getCantidadBatallas();
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
