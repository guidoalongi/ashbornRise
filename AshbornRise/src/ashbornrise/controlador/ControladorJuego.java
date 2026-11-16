package ashbornrise.controlador;

import ashbornrise.modelo.AccionInvalidaException;
import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.EstadoBatalla;
import ashbornrise.modelo.FabricaJuego;
import ashbornrise.modelo.Partida;
import ashbornrise.modelo.Recompensa;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.items.TipoRanura;
import ashbornrise.modelo.personajes.Personaje;
import ashbornrise.persistencia.IPersistencia;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        ventana.mostrarJuego(EnsambladorDTO.partida(partida));
    }

    public void cargarPartida() {
        try {
            partida = persistencia.cargar(RUTA_GUARDADO);
            ventana.mostrarJuego(EnsambladorDTO.partida(partida));
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

    /**
     * Usa (o equipa) un ítem fuera de batalla sobre un personaje del equipo.
     * La vista manda el nombre del ítem y la posición del personaje; las reglas las valida Partida.
     */
    public void usarItem(String nombreItem, int indicePersonaje) {
        Item item = partida.getInventario().buscarPorNombre(nombreItem);
        try {
            String resultado = partida.usarItem(item, personaje(indicePersonaje));
            ventana.mostrarJuego(EnsambladorDTO.partida(partida));
            ventana.mostrarMensaje(resultado);
        } catch (AccionInvalidaException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** Saca lo que el personaje tiene en esa ranura ("ARMA", "ARMADURA", "ACCESORIO") y lo guarda en el inventario. */
    public void desequipar(int indicePersonaje, String ranura) {
        try {
            String resultado = partida.desequipar(personaje(indicePersonaje), TipoRanura.valueOf(ranura));
            ventana.mostrarJuego(EnsambladorDTO.partida(partida));
            ventana.mostrarMensaje(resultado);
        } catch (AccionInvalidaException | IllegalArgumentException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** De la posición que usa la vista al personaje del modelo (null si no existe). */
    private Personaje personaje(int indice) {
        List<Personaje> integrantes = partida.getEquipo().getIntegrantes();
        return indice >= 0 && indice < integrantes.size() ? integrantes.get(indice) : null;
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
            ventana.mostrarResultadosVictoria(EnsambladorDTO.recompensa(recompensa),
                    EnsambladorDTO.partida(partida), batalla.getRonda());
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
        ventana.mostrarJuego(EnsambladorDTO.partida(partida));
        if (partida.estaCompleta()) {
            ventana.mostrarMensaje("¡Derrotaste al Señor de las Sombras! Eldara está a salvo.");
        }
    }

    public void volverAlInicio() {
        ventana.mostrarInicio();
    }
}
