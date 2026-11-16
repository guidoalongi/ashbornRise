package ashbornrise.dto;

/**
 * Un ítem que un personaje tiene equipado.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class EquipadoDTO {

    /** Identificador de la ranura ("ARMA", "ARMADURA", "ACCESORIO"); se devuelve al controlador para desequipar. */
    private String ranura;
    private String nombreRanura;
    private String nombre;
    private String descripcion;

    public String getRanura() {
        return ranura;
    }

    public void setRanura(String ranura) {
        this.ranura = ranura;
    }

    public String getNombreRanura() {
        return nombreRanura;
    }

    public void setNombreRanura(String nombreRanura) {
        this.nombreRanura = nombreRanura;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
