package ashbornrise.modelo;

import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.habilidades.TipoAlcance;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de combate por turnos.
 * - Arma la cola de turnos de cada ronda (con ayuda de GestorTurnos).
 * - Ejecuta la acción de quien tiene el turno.
 * - Verifica si la batalla terminó y crea la Recompensa (Creador).
 */
public class Batalla {

    private Equipo aliados;
    private List<Enemigo> enemigos;
    private List<Combatiente> colaTurnos;
    private int ronda;
    private EstadoBatalla estado;
    private Inventario inventario;
    private List<Item> botin;
    private GestorTurnos gestorTurnos;
    private Combatiente actorActual;
    private List<String> registro;
    private Recompensa recompensa;
    private int numeroBatalla;
    private Combatiente ultimoActor;
    private Accion ultimaAccion;

    public Batalla(int numeroBatalla, Equipo aliados, List<Enemigo> enemigos,
                   Inventario inventario, List<Item> botin) {
        this.numeroBatalla = numeroBatalla;
        this.aliados = aliados;
        this.enemigos = new ArrayList<>(enemigos);
        this.inventario = inventario;
        this.botin = new ArrayList<>(botin);
        this.colaTurnos = new ArrayList<>();
        this.gestorTurnos = new GestorTurnos();
        this.registro = new ArrayList<>();
        this.ronda = 0;
        this.estado = EstadoBatalla.EN_CURSO;
    }

    // ---------------------------------------------------------------- ciclo de la batalla

    public void iniciar() {
        ronda = 1;
        estado = EstadoBatalla.EN_CURSO;
        registrar("¡Comienza la batalla! — Ronda 1");
        armarColaDeTurnos();
        avanzarAlSiguienteActor();
    }

    /** Ejecuta la acción elegida para el combatiente que tiene el turno. */
    public void ejecutarTurno(Combatiente actor, Accion accion) {
        if (estado != EstadoBatalla.EN_CURSO || actor != actorActual) {
            return;
        }
        ultimoActor = actor;
        ultimaAccion = accion;
        accion.ejecutar(actor, this);
        verificarFin();
        if (estado == EstadoBatalla.EN_CURSO) {
            avanzarAlSiguienteActor();
        } else {
            actorActual = null;
        }
    }

    /** Si el turno es de un enemigo, este decide solo qué hacer. */
    public void ejecutarTurnoEnemigo() {
        Enemigo enemigo = buscarEnemigo(actorActual);
        if (enemigo != null) {
            ejecutarTurno(enemigo, enemigo.decidirAccion(this));
        }
    }

    public EstadoBatalla verificarFin() {
        if (getEnemigosVivos().isEmpty()) {
            estado = EstadoBatalla.VICTORIA;
        } else if (aliados.estaDerrotado()) {
            estado = EstadoBatalla.DERROTA;
        }
        return estado;
    }

    /**
     * Crea la Recompensa a partir de los enemigos derrotados y la aplica:
     * reparte la experiencia entre los vivos y guarda oro e ítems en el inventario.
     * Si se llama más de una vez, devuelve la misma recompensa (no se cobra dos veces).
     */
    public Recompensa otorgarRecompensas() {
        if (estado != EstadoBatalla.VICTORIA) {
            return null;
        }
        if (recompensa != null) {
            return recompensa;
        }
        int experienciaTotal = 0;
        int oroTotal = 0;
        for (Enemigo e : enemigos) {
            experienciaTotal += e.getExperienciaOtorgada();
            oroTotal += e.getOroOtorgado();
        }
        List<Personaje> vivos = aliados.obtenerVivos();
        int experienciaPorPersonaje = experienciaTotal / vivos.size();
        recompensa = new Recompensa(experienciaTotal, experienciaPorPersonaje, oroTotal, botin);

        for (Personaje p : vivos) {
            if (p.ganarExperiencia(experienciaPorPersonaje) > 0) {
                recompensa.registrarSubidaDeNivel(p.getNombre() + " subió a Nvl. " + p.getNivel());
            }
        }
        inventario.agregarOro(oroTotal);
        for (Item item : botin) {
            inventario.agregarItem(item);
        }
        return recompensa;
    }

    // ---------------------------------------------------------------- turnos (privado)

    private void armarColaDeTurnos() {
        List<Combatiente> todos = new ArrayList<>();
        todos.addAll(aliados.obtenerVivos());
        todos.addAll(getEnemigosVivos());
        colaTurnos = gestorTurnos.calcularOrden(todos);
    }

    /**
     * Busca al próximo combatiente que pueda actuar. Al comenzar su turno se aplican
     * sus estados (veneno, aturdimiento...). Si la cola se vacía, empieza otra ronda.
     */
    private void avanzarAlSiguienteActor() {
        actorActual = null;
        while (estado == EstadoBatalla.EN_CURSO) {
            if (colaTurnos.isEmpty()) {
                ronda++;
                registrar("— Ronda " + ronda + " —");
                armarColaDeTurnos();
            }
            Combatiente siguiente = colaTurnos.remove(0);
            if (!siguiente.estaVivo()) {
                continue;
            }
            int vidaAntes = siguiente.getVidaActual();
            boolean puedeActuar = siguiente.iniciarTurno();
            int perdida = vidaAntes - siguiente.getVidaActual();
            if (perdida > 0) {
                registrar(siguiente.getNombre() + " sufre " + perdida + " de daño por Veneno.");
            }
            if (!siguiente.estaVivo()) {
                registrar(siguiente.getNombre() + " fue derrotado.");
                verificarFin();
                continue;
            }
            if (!puedeActuar) {
                registrar(siguiente.getNombre() + " está aturdido y pierde el turno.");
                continue;
            }
            actorActual = siguiente;
            return;
        }
    }

    private Enemigo buscarEnemigo(Combatiente c) {
        for (Enemigo e : enemigos) {
            if (e == c) {
                return e;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- ayudas para acciones

    /** Decide a quién afecta una habilidad según su alcance (Batalla conoce a todos: Experto). */
    public List<Combatiente> resolverObjetivos(Habilidad h, Personaje origen, Combatiente elegido) {
        List<Combatiente> objetivos = new ArrayList<>();
        if (h.getAlcance() == TipoAlcance.PROPIO) {
            objetivos.add(origen);
        } else if (h.getAlcance() == TipoAlcance.VARIOS_ENEMIGOS) {
            List<Enemigo> vivos = getEnemigosVivos();
            for (int i = 0; i < vivos.size() && i < h.getCantidadObjetivos(); i++) {
                objetivos.add(vivos.get(i));
            }
        } else if (elegido != null) {
            objetivos.add(elegido);
        }
        return objetivos;
    }

    public int robarOro(int cantidad) {
        return inventario.quitarOro(cantidad);
    }

    public void consumirItem(Item item) {
        inventario.quitarItem(item);
    }

    public void registrar(String mensaje) {
        registro.add(mensaje);
    }

    // ---------------------------------------------------------------- consultas

    public boolean esTurnoDelJugador() {
        return estado == EstadoBatalla.EN_CURSO && actorActual != null
                && actorActual.esControladoPorJugador();
    }

    /** El personaje que tiene el turno, o null si el turno no es del jugador. */
    public Personaje getPersonajeActual() {
        for (Personaje p : aliados.getIntegrantes()) {
            if (p == actorActual) {
                return p;
            }
        }
        return null;
    }

    public List<Personaje> getAliadosVivos() {
        return aliados.obtenerVivos();
    }

    public List<Personaje> getAliados() {
        return aliados.getIntegrantes();
    }

    public List<Enemigo> getEnemigos() {
        return new ArrayList<>(enemigos);
    }

    public List<Enemigo> getEnemigosVivos() {
        List<Enemigo> vivos = new ArrayList<>();
        for (Enemigo e : enemigos) {
            if (e.estaVivo()) {
                vivos.add(e);
            }
        }
        return vivos;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public Combatiente getActorActual() {
        return actorActual;
    }

    /** Quién actuó en el último turno ejecutado (la vista lo usa para animarlo). */
    public Combatiente getUltimoActor() {
        return ultimoActor;
    }

    public Accion getUltimaAccion() {
        return ultimaAccion;
    }

    public int getRonda() {
        return ronda;
    }

    public EstadoBatalla getEstado() {
        return estado;
    }

    public int getNumeroBatalla() {
        return numeroBatalla;
    }

    public List<String> getRegistro() {
        return new ArrayList<>(registro);
    }
}
