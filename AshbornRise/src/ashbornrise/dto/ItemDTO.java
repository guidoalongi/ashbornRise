package ashbornrise.dto;

/**
 * Un tipo de ítem del inventario con su cantidad ("Poción de Vida x3").
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class ItemDTO {

    private String nombre;
    private String descripcion;
    private int cantidad;
    private boolean equipable;
    private boolean usableEnBatalla;

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

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public boolean isEquipable() {
        return equipable;
    }

    public void setEquipable(boolean equipable) {
        this.equipable = equipable;
    }

    public boolean isUsableEnBatalla() {
        return usableEnBatalla;
    }

    public void setUsableEnBatalla(boolean usableEnBatalla) {
        this.usableEnBatalla = usableEnBatalla;
    }
}
