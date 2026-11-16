package ashbornrise.controlador;

import ashbornrise.modelo.AccionInvalidaException;
import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.EstadoBatalla;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionAtacar;
import ashbornrise.modelo.acciones.AccionDefender;
import ashbornrise.modelo.acciones.AccionHabilidad;
import ashbornrise.modelo.acciones.AccionItem;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import java.util.List;

/**
 * Controlador (GRASP) de la pantalla de batalla. Recibe lo que el jugador eligió (ids e índices
 * que vienen en los DTO), busca los objetos del modelo correspondientes, arma la Accion y se la
 * pasa a Batalla. Las reglas (a quién se puede atacar, qué objetivos admite una habilidad...)
 * las valida el MODELO: si la jugada no es válida, Batalla lanza AccionInvalidaException y acá
 * solo se muestra el mensaje. Después hace jugar a los enemigos hasta que vuelva a ser el turno
 * de un personaje.
 */
public class ControladorBatalla {

    private Batalla batalla;
    private IVistaBatalla vista;
    private ControladorJuego controladorJuego;

    public ControladorBatalla(ControladorJuego controladorJuego, IVistaBatalla vista) {
        this.controladorJuego = controladorJuego;
        this.vista = vista;
    }

    public void iniciarBatalla(Batalla nuevaBatalla) {
        this.batalla = nuevaBatalla;
        batalla.iniciar();
        vista.iniciarBatalla(EnsambladorDTO.batalla(batalla), new ContinuarTurnos(this));
    }

    // ---------------------------------------------------------------- acciones del jugador

    public void atacar(int objetivoId) {
        ejecutar(new AccionAtacar(combatiente(objetivoId)));
    }

    public void defender() {
        ejecutar(new AccionDefender());
    }

    /** objetivoId puede ser -1 si la habilidad es PROPIO o VARIOS_ENEMIGOS (los elige el modelo). */
    public void usarHabilidad(int indiceHabilidad, int objetivoId) {
        Personaje actor = batalla.getPersonajeActual();
        Habilidad habilidad = habilidad(actor, indiceHabilidad);
        if (habilidad == null) {
            vista.mostrarError("Esa habilidad no existe.");
            return;
        }
        List<Combatiente> objetivos = batalla.resolverObjetivos(habilidad, actor, combatiente(objetivoId));
        ejecutar(new AccionHabilidad(habilidad, objetivos));
    }

    /**
     * Consulta previa para avisar enseguida si no alcanza el recurso (antes de pedir un objetivo).
     * La regla la sigue decidiendo el modelo (Personaje.puedeUsar).
     */
    public boolean puedeUsarHabilidad(int indiceHabilidad) {
        Personaje actor = batalla.getPersonajeActual();
        Habilidad habilidad = habilidad(actor, indiceHabilidad);
        if (habilidad == null) {
            return false;
        }
        if (actor.puedeUsar(habilidad)) {
            return true;
        }
        vista.mostrarError(actor.getTipoRecurso() == TipoRecurso.MANA ? "Maná insuficiente" : "Energía insuficiente");
        return false;
    }

    public void usarItem(String nombreItem, int objetivoId) {
        Item item = batalla.getInventario().buscarPorNombre(nombreItem);
        ejecutar(new AccionItem(batalla.getPersonajeActual(), item, aliado(objetivoId)));
    }

    /** Cuando la batalla terminó, el jugador toca "Continuar" para ver los resultados. */
    public void continuar() {
        if (batalla.getEstado() != EstadoBatalla.EN_CURSO) {
            controladorJuego.finalizarBatalla(batalla);
        }
    }

    // ---------------------------------------------------------------- privado

    /** Le pasa la acción al modelo. Si el modelo la rechaza, se muestra el motivo y el turno sigue igual. */
    private void ejecutar(Accion accion) {
        try {
            batalla.ejecutarTurno(batalla.getPersonajeActual(), accion);
        } catch (AccionInvalidaException e) {
            vista.mostrarError(e.getMessage());
            return;
        }
        vista.animarTurno(EnsambladorDTO.batalla(batalla), new ContinuarTurnos(this));
    }

    /** Del id que usa la vista al combatiente del modelo (null si no existe). */
    private Combatiente combatiente(int id) {
        List<Combatiente> participantes = batalla.getCombatientes();
        return id >= 0 && id < participantes.size() ? participantes.get(id) : null;
    }

    /** Igual que combatiente(), pero solo si es un personaje del jugador. */
    private Personaje aliado(int id) {
        Combatiente c = combatiente(id);
        for (Personaje p : batalla.getAliados()) {
            if (p == c) {
                return p;
            }
        }
        return null;
    }

    private Habilidad habilidad(Personaje actor, int indice) {
        if (actor == null || indice < 0 || indice >= actor.getHabilidades().size()) {
            return null;
        }
        return actor.getHabilidades().get(indice);
    }

    /**
     * Juegan los enemigos de a uno (cada turno se anima antes de pasar al siguiente)
     * hasta que le toque a un personaje o termine la batalla.
     * Lo llama ContinuarTurnos cuando la vista termina de animar (por eso no es privado).
     */
    void continuarTurnos() {
        if (batalla.getEstado() == EstadoBatalla.EN_CURSO && !batalla.esTurnoDelJugador()) {
            batalla.ejecutarTurnoEnemigo();
            vista.animarTurno(EnsambladorDTO.batalla(batalla), new ContinuarTurnos(this));
        } else {
            vista.actualizarVista(EnsambladorDTO.batalla(batalla));
        }
    }
}
