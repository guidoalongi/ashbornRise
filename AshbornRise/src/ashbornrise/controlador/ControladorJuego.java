package ashbornrise.controlador;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.EstadoBatalla;
import ashbornrise.modelo.FabricaJuego;
import ashbornrise.modelo.Partida;
import ashbornrise.modelo.Recompensa;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;
import ashbornrise.persistencia.IPersistencia;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador (GRASP) de los eventos "generales" del juego: nueva partida, cargar,
 * guardar, pasar a la siguiente batalla y administrar el inventario entre batallas.
 * La Vista nunca toca el modelo para modificarlo: siempre pasa por acá.
 */
public class ControladorJuego {

    public static final String RUTA_GUARDADO = "savegame.json";

    private Partida partida;
    private IPersistencia persistencia;
    private IVentanaJuego ventana;
    private ControladorBatalla controladorBatalla;

    public ControladorJuego(IPersistencia persistencia, IVentanaJuego ventana) {
        this.persistencia = persistencia;
        this.ventana = ventana;
    }

    public void setControladorBatalla(ControladorBatalla controladorBatalla) {
        this.controladorBatalla = controladorBatalla;
    }

    // ---------------------------------------------------------------- pantalla inicial

    public void nuevaPartida() {
        partida = FabricaJuego.crearPartidaNueva();
        ventana.mostrarJuego(partida);
    }

    public void cargarPartida() {
        try {
            partida = persistencia.cargar(RUTA_GUARDADO);
            ventana.mostrarJuego(partida);
            ventana.mostrarMensaje("Partida cargada (guardada el " + partida.getFechaGuardado() + ")");
        } catch (IOException e) {
            ventana.mostrarError("No se pudo cargar la partida: " + e.getMessage());
        }
    }

    public void salir() {
        System.exit(0);
    }

    // ---------------------------------------------------------------- pantalla de estado

    public void guardarPartida() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        partida.setFechaGuardado(fecha);
        try {
            persistencia.guardar(partida, RUTA_GUARDADO);
            ventana.mostrarMensaje("Partida guardada correctamente");
        } catch (IOException e) {
            ventana.mostrarError("No se pudo guardar la partida: " + e.getMessage());
        }
    }

    /** Usa un ítem fuera de batalla sobre un personaje del equipo. */
    public void usarItem(Item item, Personaje objetivo) {
        if (item == null || objetivo == null || !partida.getInventario().contiene(item)) {
            return;
        }
        if (!objetivo.estaVivo()) {
            ventana.mostrarError(objetivo.getNombre() + " está fuera de combate.");
            return;
        }
        String resultado = objetivo.usarItem(item, objetivo);
        partida.getInventario().quitarItem(item);
        ventana.mostrarJuego(partida);
        ventana.mostrarMensaje(resultado);
    }

    public void siguienteBatalla() {
        if (partida.estaCompleta()) {
            ventana.mostrarMensaje("¡Ya completaste todas las batallas!");
            return;
        }
        Batalla batalla = partida.crearSiguienteBatalla();
        ventana.mostrarBatalla();
        controladorBatalla.iniciarBatalla(batalla);
    }

    // ---------------------------------------------------------------- fin de batalla

    /** Lo llama ControladorBatalla cuando la batalla terminó y el jugador tocó "Continuar". */
    public void finalizarBatalla(Batalla batalla) {
        if (batalla.getEstado() == EstadoBatalla.VICTORIA) {
            Recompensa recompensa = batalla.otorgarRecompensas();
            partida.registrarVictoria();
            ventana.mostrarResultadosVictoria(recompensa, partida, batalla.getRonda());
        } else {
            ventana.mostrarResultadosDerrota(batalla.getNumeroBatalla());
        }
    }

    /** Después de perder: el equipo se recupera por completo y se repite la misma batalla. */
    public void reintentarBatalla() {
        partida.getEquipo().restaurarCompleto();
        siguienteBatalla();
    }

    public void continuarTrasResultados() {
        ventana.mostrarJuego(partida);
        if (partida.estaCompleta()) {
            ventana.mostrarMensaje("¡Derrotaste al Señor de las Sombras! Eldara está a salvo.");
        }
    }

    public void volverAlInicio() {
        ventana.mostrarInicio();
    }

    public Partida getPartida() {
        return partida;
    }
}
