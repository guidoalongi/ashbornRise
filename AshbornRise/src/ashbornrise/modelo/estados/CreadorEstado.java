package ashbornrise.modelo.estados;

/**
 * Sabe crear un estado nuevo, ya configurado (por ejemplo "Escudo de 25 puntos por 2 turnos").
 *
 * HabilidadEstado recibe uno de estos en lugar de un TipoEstado: así no necesita un switch
 * para decidir qué clase concreta crear. Si mañana se agrega un estado nuevo, se crea su clase
 * y se le pasa un creador a la habilidad, sin tocar HabilidadEstado (Polimorfismo).
 *
 * Es una interfaz con un solo método, como Runnable: se puede implementar con una lambda,
 * por ejemplo  () -> new Escudo(25, 2).
 */
public interface CreadorEstado {

    EstadoEfecto crear();
}
