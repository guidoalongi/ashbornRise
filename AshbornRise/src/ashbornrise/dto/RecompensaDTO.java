package ashbornrise.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de una batalla ganada, para la pantalla de resultados.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class RecompensaDTO {

    private int experienciaPorPersonaje;
    private int oro;
    private List<String> items = new ArrayList<>();
    private List<String> subidasDeNivel = new ArrayList<>();

    public int getExperienciaPorPersonaje() {
        return experienciaPorPersonaje;
    }

    public void setExperienciaPorPersonaje(int experienciaPorPersonaje) {
        this.experienciaPorPersonaje = experienciaPorPersonaje;
    }

    public int getOro() {
        return oro;
    }

    public void setOro(int oro) {
        this.oro = oro;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<String> getSubidasDeNivel() {
        return subidasDeNivel;
    }

    public void setSubidasDeNivel(List<String> subidasDeNivel) {
        this.subidasDeNivel = subidasDeNivel;
    }
}
