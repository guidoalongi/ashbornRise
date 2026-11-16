package ashbornrise.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Foto del estado de la batalla en un momento dado. La vista la recibe cada vez que tiene
 * que dibujarse o animar un turno; nunca toca el objeto Batalla del modelo.
 *
 * Es un DTO (Data Transfer Object): solo transporta datos del modelo hacia la vista.
 * No tiene lógica ni referencias a clases del modelo.
 */
public class BatallaDTO {

    private int numero;
    private int ronda;
    private boolean enCurso;
    private boolean victoria;
    private List<String> registro = new ArrayList<>();
    private List<PersonajeDTO> aliados = new ArrayList<>();
    private List<CombatienteDTO> enemigos = new ArrayList<>();
    /** Id de quien tiene el turno, o -1. */
    private int actorActualId;
    private boolean turnoDelJugador;
    /** Id de quien actuó en el último turno, o -1. */
    private int ultimoActorId;
    /** Ids de los que actúan después en esta ronda, en orden. */
    private List<Integer> proximosTurnos = new ArrayList<>();
    /** Último turno ejecutado (null al comenzar). */
    private TurnoDTO ultimoTurno;
    /** Inventario disponible. */
    private List<ItemDTO> items = new ArrayList<>();

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getRonda() {
        return ronda;
    }

    public void setRonda(int ronda) {
        this.ronda = ronda;
    }

    public boolean isEnCurso() {
        return enCurso;
    }

    public void setEnCurso(boolean enCurso) {
        this.enCurso = enCurso;
    }

    public boolean isVictoria() {
        return victoria;
    }

    public void setVictoria(boolean victoria) {
        this.victoria = victoria;
    }

    public List<String> getRegistro() {
        return registro;
    }

    public void setRegistro(List<String> registro) {
        this.registro = registro;
    }

    public List<PersonajeDTO> getAliados() {
        return aliados;
    }

    public void setAliados(List<PersonajeDTO> aliados) {
        this.aliados = aliados;
    }

    public List<CombatienteDTO> getEnemigos() {
        return enemigos;
    }

    public void setEnemigos(List<CombatienteDTO> enemigos) {
        this.enemigos = enemigos;
    }

    public int getActorActualId() {
        return actorActualId;
    }

    public void setActorActualId(int actorActualId) {
        this.actorActualId = actorActualId;
    }

    public boolean isTurnoDelJugador() {
        return turnoDelJugador;
    }

    public void setTurnoDelJugador(boolean turnoDelJugador) {
        this.turnoDelJugador = turnoDelJugador;
    }

    public int getUltimoActorId() {
        return ultimoActorId;
    }

    public void setUltimoActorId(int ultimoActorId) {
        this.ultimoActorId = ultimoActorId;
    }

    public List<Integer> getProximosTurnos() {
        return proximosTurnos;
    }

    public void setProximosTurnos(List<Integer> proximosTurnos) {
        this.proximosTurnos = proximosTurnos;
    }

    public TurnoDTO getUltimoTurno() {
        return ultimoTurno;
    }

    public void setUltimoTurno(TurnoDTO ultimoTurno) {
        this.ultimoTurno = ultimoTurno;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }


    // ---------------------------------------------------------------- ayudas de búsqueda (sin lógica de juego)

    /** Busca un combatiente por id entre aliados y enemigos; null si no está. */
    public CombatienteDTO buscar(int id) {
        for (CombatienteDTO c : todos()) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    /** El personaje que tiene el turno, o null si el turno no es del jugador. */
    public PersonajeDTO getPersonajeActual() {
        if (!turnoDelJugador) {
            return null;
        }
        for (PersonajeDTO p : aliados) {
            if (p.getId() == actorActualId) {
                return p;
            }
        }
        return null;
    }

    public List<CombatienteDTO> todos() {
        List<CombatienteDTO> lista = new ArrayList<>(aliados);
        lista.addAll(enemigos);
        return lista;
    }
}
