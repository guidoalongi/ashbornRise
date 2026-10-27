package ashbornrise.controlador;

import ashbornrise.modelo.Partida;
import ashbornrise.modelo.Recompensa;

/**
 * Lo que ControladorJuego necesita de la ventana del juego: cambiar de pantalla y mostrar mensajes.
 *
 * El controlador depende de esta interfaz y no de VentanaPrincipal (un JFrame de Swing),
 * igual que ControladorBatalla con IVistaBatalla.
 */
public interface IVentanaJuego {

    void mostrarInicio();

    void mostrarJuego(Partida partida);

    void mostrarBatalla();

    void mostrarResultadosVictoria(Recompensa recompensa, Partida partida, int rondas);

    void mostrarResultadosDerrota(int numeroBatalla);

    /** Mensaje informativo en la pantalla de estado del equipo (por ejemplo, "Partida guardada"). */
    void mostrarMensaje(String mensaje);

    void mostrarError(String mensaje);
}
