package ashbornrise.controlador;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.EstadoBatalla;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.acciones.AccionAtacar;
import ashbornrise.modelo.acciones.AccionDefender;
import ashbornrise.modelo.acciones.AccionHabilidad;
import ashbornrise.modelo.acciones.AccionItem;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import java.util.List;

/**
 * Controlador (GRASP) de la pantalla de batalla. Recibe lo que el jugador eligió,
 * arma la Accion correspondiente y se la pasa a Batalla. Después hace jugar
 * a los enemigos hasta que vuelva a ser el turno de un personaje.
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
        vista.iniciarBatalla(batalla, new ContinuarTurnos(this));
    }

    // ---------------------------------------------------------------- acciones del jugador

    public void atacar(Combatiente objetivo) {
        Personaje actor = batalla.getPersonajeActual();
        if (actor == null || objetivo == null || !objetivo.estaVivo()) {
            return;
        }
        batalla.ejecutarTurno(actor, new AccionAtacar(objetivo));
        despuesDeLaAccion();
    }

    public void defender() {
        Personaje actor = batalla.getPersonajeActual();
        if (actor == null) {
            return;
        }
        batalla.ejecutarTurno(actor, new AccionDefender());
        despuesDeLaAccion();
    }

    /** objetivoElegido puede ser null si la habilidad es PROPIO o VARIOS_ENEMIGOS. */
    public void usarHabilidad(Habilidad habilidad, Combatiente objetivoElegido) {
        Personaje actor = batalla.getPersonajeActual();
        if (actor == null) {
            return;
        }
        if (!puedeUsarHabilidad(habilidad)) {
            return;
        }
        List<Combatiente> objetivos = batalla.resolverObjetivos(habilidad, actor, objetivoElegido);
        if (objetivos.isEmpty()) {
            return;
        }
        batalla.ejecutarTurno(actor, new AccionHabilidad(habilidad, objetivos));
        despuesDeLaAccion();
    }

    /** Chequea el recurso antes de usar una habilidad; si no alcanza, avisa en la vista. */
    public boolean puedeUsarHabilidad(Habilidad habilidad) {
        Personaje actor = batalla.getPersonajeActual();
        if (actor == null) {
            return false;
        }
        if (actor.puedeUsar(habilidad)) {
            return true;
        }
        if (actor.getTipoRecurso() == TipoRecurso.MANA) {
            vista.mostrarError("Maná insuficiente");
        } else {
            vista.mostrarError("Energía insuficiente");
        }
        return false;
    }

    public void usarItem(Item item, Personaje objetivo) {
        Personaje actor = batalla.getPersonajeActual();
        if (actor == null || objetivo == null || !batalla.getInventario().contiene(item)) {
            return;
        }
        if (!objetivo.estaVivo()) {
            vista.mostrarError(objetivo.getNombre() + " está fuera de combate.");
            return;
        }
        batalla.ejecutarTurno(actor, new AccionItem(actor, item, objetivo));
        despuesDeLaAccion();
    }

    /** Cuando la batalla terminó, el jugador toca "Continuar" para ver los resultados. */
    public void continuar() {
        if (batalla.getEstado() != EstadoBatalla.EN_CURSO) {
            controladorJuego.finalizarBatalla(batalla);
        }
    }

    // ---------------------------------------------------------------- privado

    /** La vista anima la acción y, cuando termina, sigue con los turnos que vengan. */
    private void despuesDeLaAccion() {
        vista.animarTurno(batalla, new ContinuarTurnos(this));
    }

    /**
     * Juegan los enemigos de a uno (cada turno se anima antes de pasar al siguiente)
     * hasta que le toque a un personaje o termine la batalla.
     * Lo llama ContinuarTurnos cuando la vista termina de animar (por eso no es privado).
     */
    void continuarTurnos() {
        if (batalla.getEstado() == EstadoBatalla.EN_CURSO && !batalla.esTurnoDelJugador()) {
            batalla.ejecutarTurnoEnemigo();
            vista.animarTurno(batalla, new ContinuarTurnos(this));
        } else {
            vista.actualizarVista(batalla);
        }
    }
}
