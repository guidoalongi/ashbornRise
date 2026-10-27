package ashbornrise.controlador;

import ashbornrise.modelo.Batalla;

/**
 * Lo que ControladorBatalla necesita de "algo que muestre la batalla".
 *
 * El controlador depende de esta interfaz y no de la clase VistaBatalla (un JPanel de Swing).
 * Así el controlador no sabe si la batalla se muestra con la arena 3D, en 2D o por consola:
 * se puede cambiar la vista sin tocar el controlador (Bajo Acoplamiento y Variación Protegida).
 */
public interface IVistaBatalla {

    /** Prepara la pantalla para una batalla nueva. Cuando termina la presentación, llama a alTerminar.run(). */
    void iniciarBatalla(Batalla batalla, Runnable alTerminar);

    /** Muestra lo que pasó en el último turno. Cuando termina, llama a alTerminar.run(). */
    void animarTurno(Batalla batalla, Runnable alTerminar);

    /** Redibuja la pantalla con el estado actual de la batalla. */
    void actualizarVista(Batalla batalla);

    void mostrarError(String mensaje);
}
