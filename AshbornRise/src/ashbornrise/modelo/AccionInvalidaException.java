package ashbornrise.modelo;

/**
 * La lanza el modelo cuando le piden algo que rompe las reglas del juego: atacar a un aliado,
 * elegir un objetivo derrotado, usar una habilidad sobre un objetivo incompatible, actuar fuera
 * de turno, usar un ítem que no está en el inventario, etc.
 *
 * Es unchecked (hereda de RuntimeException) porque es un error de uso, no algo externo como
 * un archivo que falta. El controlador la atrapa y muestra el mensaje en la vista.
 */
public class AccionInvalidaException extends RuntimeException {

    public AccionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
