package ashbornrise.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Qué pasó en el último turno, para que la vista lo anime.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class TurnoDTO {

    private TipoAccionDTO tipo;
    private int actorId;
    /** Ids de los combatientes afectados. */
    private List<Integer> objetivos = new ArrayList<>();
    /** Nombre de la habilidad o del ítem usado (null si no corresponde). */
    private String nombreUsado;
    /** Efecto de la habilidad usada (null si no fue una habilidad). */
    private EfectoDTO efecto;

    public TipoAccionDTO getTipo() {
        return tipo;
    }

    public void setTipo(TipoAccionDTO tipo) {
        this.tipo = tipo;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public List<Integer> getObjetivos() {
        return objetivos;
    }

    public void setObjetivos(List<Integer> objetivos) {
        this.objetivos = objetivos;
    }

    public String getNombreUsado() {
        return nombreUsado;
    }

    public void setNombreUsado(String nombreUsado) {
        this.nombreUsado = nombreUsado;
    }

    public EfectoDTO getEfecto() {
        return efecto;
    }

    public void setEfecto(EfectoDTO efecto) {
        this.efecto = efecto;
    }
}
