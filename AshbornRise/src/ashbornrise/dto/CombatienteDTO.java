package ashbornrise.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos de un combatiente (aliado o enemigo) para mostrar en pantalla.
 * El id identifica al combatiente: la vista lo devuelve al controlador cuando el jugador lo elige.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class CombatienteDTO {

    private int id;
    private String nombre;
    /** Nombre de la clase ("Guerrero", "GoblinMerodeador"...), para elegir el sprite. */
    private String clase;
    private boolean enemigo;
    private boolean jefe;
    private boolean vivo;
    private int vidaActual;
    private int vidaMaxima;
    private int ataque;
    private int defensa;
    private int velocidad;
    private boolean envenenado;
    private boolean conEscudo;
    private boolean aturdido;
    private boolean defendiendo;
    /** Nombres de los estados activos ("Veneno", "Escudo"...), solo para mostrar. */
    private List<String> estados = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public boolean isEnemigo() {
        return enemigo;
    }

    public void setEnemigo(boolean enemigo) {
        this.enemigo = enemigo;
    }

    public boolean isJefe() {
        return jefe;
    }

    public void setJefe(boolean jefe) {
        this.jefe = jefe;
    }

    public boolean isVivo() {
        return vivo;
    }

    public void setVivo(boolean vivo) {
        this.vivo = vivo;
    }

    public int getVidaActual() {
        return vidaActual;
    }

    public void setVidaActual(int vidaActual) {
        this.vidaActual = vidaActual;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public void setVidaMaxima(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
    }

    public int getAtaque() {
        return ataque;
    }

    public void setAtaque(int ataque) {
        this.ataque = ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public void setDefensa(int defensa) {
        this.defensa = defensa;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    public boolean isEnvenenado() {
        return envenenado;
    }

    public void setEnvenenado(boolean envenenado) {
        this.envenenado = envenenado;
    }

    public boolean isConEscudo() {
        return conEscudo;
    }

    public void setConEscudo(boolean conEscudo) {
        this.conEscudo = conEscudo;
    }

    public boolean isAturdido() {
        return aturdido;
    }

    public void setAturdido(boolean aturdido) {
        this.aturdido = aturdido;
    }

    public boolean isDefendiendo() {
        return defendiendo;
    }

    public void setDefendiendo(boolean defendiendo) {
        this.defendiendo = defendiendo;
    }

    public List<String> getEstados() {
        return estados;
    }

    public void setEstados(List<String> estados) {
        this.estados = estados;
    }
}
