package ashbornrise.modelo;

import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionDefender;
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
 * - Valida y ejecuta la acción de quien tiene el turno (el modelo no acepta jugadas inválidas).
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

    /**
     * Ejecuta la acción elegida para el combatiente que tiene el turno.
     * Antes la valida: si rompe alguna regla, lanza AccionInvalidaException y no pasa nada
     * (el turno sigue siendo del mismo combatiente). Esto vale aunque la llamada venga
     * de cualquier parte del sistema, no solo de la vista.
     */
    public void ejecutarTurno(Combatiente actor, Accion accion) {
        if (estado != EstadoBatalla.EN_CURSO) {
            throw new AccionInvalidaException("La batalla ya terminó.");
        }
        if (actor == null || actor != actorActual) {
            throw new AccionInvalidaException("No es el turno de ese combatiente.");
        }
        if (accion == null) {
            throw new AccionInvalidaException("Falta indicar la acción.");
        }
        accion.validar(actor, this);
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

    /**
     * Si el turno es de un enemigo, este decide solo qué hacer. Si lo que decide no fuera válido,
     * se defiende en su lugar (la IA tampoco puede saltarse las reglas).
     */
    public void ejecutarTurnoEnemigo() {
        Enemigo enemigo = buscarEnemigo(actorActual);
        if (enemigo == null) {
            return;
        }
        Accion accion = enemigo.decidirAccion(this);
        try {
            accion.validar(enemigo, this);
        } catch (AccionInvalidaException e) {
            accion = new AccionDefender();
        }
        ejecutarTurno(enemigo, accion);
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

    /**
     * Decide a quién afecta una habilidad según su alcance (Batalla conoce a todos: Experto).
     * Para PROPIO y VARIOS_ENEMIGOS los elige solo; para UN_ENEMIGO / UN_ALIADO usa el elegido.
     * No valida: la validación la hace la acción cuando se ejecuta.
     */
    public List<Combatiente> resolverObjetivos(Habilidad h, Combatiente origen, Combatiente elegido) {
        List<Combatiente> objetivos = new ArrayList<>();
        if (h.getAlcance() == TipoAlcance.PROPIO) {
            objetivos.add(origen);
        } else if (h.getAlcance() == TipoAlcance.VARIOS_ENEMIGOS) {
            for (Combatiente c : getCombatientes()) {
                if (objetivos.size() < h.getCantidadObjetivos() && h.getAlcance().admite(origen, c)) {
                    objetivos.add(c);
                }
            }
        } else if (elegido != null) {
            objetivos.add(elegido);
        }
        return objetivos;
    }

    /** ¿Este combatiente es parte de esta batalla (aliado o enemigo)? */
    public boolean participa(Combatiente c) {
        return getCombatientes().contains(c);
    }

    public int robarOro(int cantidad) {
        return inventario.quitarOro(cantidad);
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

    /** Todos los participantes: primero los aliados y después los enemigos (siempre en el mismo orden). */
    public List<Combatiente> getCombatientes() {
        List<Combatiente> todos = new ArrayList<>(aliados.getIntegrantes());
        todos.addAll(enemigos);
        return todos;
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

    /**
     * Los combatientes que todavía tienen que actuar en esta ronda, en orden
     * (sin contar al que tiene el turno ni a los derrotados). Es una copia:
     * la vista la usa para mostrar quién viene después sin poder modificar la cola.
     */
    public List<Combatiente> getProximosTurnos() {
        List<Combatiente> proximos = new ArrayList<>();
        for (Combatiente c : colaTurnos) {
            if (c.estaVivo()) {
                proximos.add(c);
            }
        }
        return proximos;
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
