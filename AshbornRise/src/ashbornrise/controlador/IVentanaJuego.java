package ashbornrise.controlador;

import ashbornrise.dto.PartidaDTO;
import ashbornrise.dto.RecompensaDTO;

/**
 * Lo que ControladorJuego necesita de la ventana del juego: cambiar de pantalla y mostrar mensajes.
 *
 * El controlador depende de esta interfaz y no de VentanaPrincipal (un JFrame de Swing),
 * igual que ControladorBatalla con IVistaBatalla. El contrato solo usa DTOs, no clases del modelo.
 */
public interface IVentanaJuego {

    void mostrarInicio();

    void mostrarJuego(PartidaDTO partida);

    void mostrarBatalla();

    void mostrarResultadosVictoria(RecompensaDTO recompensa, PartidaDTO partida, int rondas);

    void mostrarResultadosDerrota(int numeroBatalla);

    /** Mensaje informativo en la pantalla de estado del equipo (por ejemplo, "Partida guardada"). */
    void mostrarMensaje(String mensaje);

    void mostrarError(String mensaje);
}
