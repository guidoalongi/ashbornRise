package ashbornrise.dto;

/**
 * Datos de una habilidad de un personaje. El índice es su posición en la lista de habilidades
 * del personaje: la vista lo devuelve al controlador para indicar cuál eligió el jugador.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class HabilidadDTO {

    private int indice;
    private String nombre;
    private int costo;
    private boolean usaMana;
    private AlcanceDTO alcance;
    /** true si al personaje le alcanza el recurso para usarla ahora. */
    private boolean usable;

    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCosto() {
        return costo;
    }

    public void setCosto(int costo) {
        this.costo = costo;
    }

    public boolean isUsaMana() {
        return usaMana;
    }

    public void setUsaMana(boolean usaMana) {
        this.usaMana = usaMana;
    }

    public AlcanceDTO getAlcance() {
        return alcance;
    }

    public void setAlcance(AlcanceDTO alcance) {
        this.alcance = alcance;
    }

    public boolean isUsable() {
        return usable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }
}
