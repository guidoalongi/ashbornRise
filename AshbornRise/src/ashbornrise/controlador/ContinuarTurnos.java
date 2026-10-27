package ashbornrise.controlador;

/**
 * Lo que tiene que pasar cuando la vista termina de mostrar un turno: seguir con el juego.
 *
 * Runnable es una interfaz de Java con un solo método, run(). La vista recibe un Runnable
 * y lo ejecuta cuando termina la animación, sin saber qué hay adentro ni conocer al controlador.
 * Esta clase es ese Runnable: su run() le avisa a ControladorBatalla que continúe con los turnos.
 */
public class ContinuarTurnos implements Runnable {

    private ControladorBatalla controlador;

    public ContinuarTurnos(ControladorBatalla controlador) {
        this.controlador = controlador;
    }

    @Override
    public void run() {
        controlador.continuarTurnos();
    }
}
