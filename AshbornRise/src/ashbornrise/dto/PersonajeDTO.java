package ashbornrise.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos de un personaje del jugador: lo de cualquier combatiente más nivel, recurso,
 * habilidades y equipamiento.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class PersonajeDTO extends CombatienteDTO {

    private String nombreClase;
    private int nivel;
    private int experiencia;
    private int experienciaParaSubir;
    /** true = Maná, false = Energía. */
    private boolean usaMana;
    private int recursoActual;
    private int recursoMaximo;
    private int bonusAtaque;
    private int bonusDefensa;
    private int bonusVelocidad;
    private List<HabilidadDTO> habilidades = new ArrayList<>();
    private List<EquipadoDTO> equipamiento = new ArrayList<>();

    public String getNombreClase() {
        return nombreClase;
    }

    public void setNombreClase(String nombreClase) {
        this.nombreClase = nombreClase;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(int experiencia) {
        this.experiencia = experiencia;
    }

    public int getExperienciaParaSubir() {
        return experienciaParaSubir;
    }

    public void setExperienciaParaSubir(int experienciaParaSubir) {
        this.experienciaParaSubir = experienciaParaSubir;
    }

    public boolean isUsaMana() {
        return usaMana;
    }

    public void setUsaMana(boolean usaMana) {
        this.usaMana = usaMana;
    }

    public int getRecursoActual() {
        return recursoActual;
    }

    public void setRecursoActual(int recursoActual) {
        this.recursoActual = recursoActual;
    }

    public int getRecursoMaximo() {
        return recursoMaximo;
    }

    public void setRecursoMaximo(int recursoMaximo) {
        this.recursoMaximo = recursoMaximo;
    }

    public int getBonusAtaque() {
        return bonusAtaque;
    }

    public void setBonusAtaque(int bonusAtaque) {
        this.bonusAtaque = bonusAtaque;
    }

    public int getBonusDefensa() {
        return bonusDefensa;
    }

    public void setBonusDefensa(int bonusDefensa) {
        this.bonusDefensa = bonusDefensa;
    }

    public int getBonusVelocidad() {
        return bonusVelocidad;
    }

    public void setBonusVelocidad(int bonusVelocidad) {
        this.bonusVelocidad = bonusVelocidad;
    }

    public List<HabilidadDTO> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<HabilidadDTO> habilidades) {
        this.habilidades = habilidades;
    }

    public List<EquipadoDTO> getEquipamiento() {
        return equipamiento;
    }

    public void setEquipamiento(List<EquipadoDTO> equipamiento) {
        this.equipamiento = equipamiento;
    }
}
