package ashbornrise.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos de la partida para la pantalla de estado del equipo.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class PartidaDTO {

    private int oro;
    private int numeroBatallaActual;
    private int cantidadBatallas;
    private boolean completa;
    private String fechaGuardado;
    private List<PersonajeDTO> personajes = new ArrayList<>();
    private List<ItemDTO> items = new ArrayList<>();

    public int getOro() {
        return oro;
    }

    public void setOro(int oro) {
        this.oro = oro;
    }

    public int getNumeroBatallaActual() {
        return numeroBatallaActual;
    }

    public void setNumeroBatallaActual(int numeroBatallaActual) {
        this.numeroBatallaActual = numeroBatallaActual;
    }

    public int getCantidadBatallas() {
        return cantidadBatallas;
    }

    public void setCantidadBatallas(int cantidadBatallas) {
        this.cantidadBatallas = cantidadBatallas;
    }

    public boolean isCompleta() {
        return completa;
    }

    public void setCompleta(boolean completa) {
        this.completa = completa;
    }

    public String getFechaGuardado() {
        return fechaGuardado;
    }

    public void setFechaGuardado(String fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
    }

    public List<PersonajeDTO> getPersonajes() {
        return personajes;
    }

    public void setPersonajes(List<PersonajeDTO> personajes) {
        this.personajes = personajes;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }
}
