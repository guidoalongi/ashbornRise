package ashbornrise.modelo;

import ashbornrise.modelo.estados.EstadoEfecto;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.habilidades.Habilidad;

import java.util.ArrayList;
import java.util.List;

/**
 * Base común de Personaje y Enemigo.
 * Gracias a esta clase, Batalla puede manejar una única cola de turnos
 * sin distinguir entre aliados y enemigos.
 */
public abstract class Combatiente {

    protected String nombre;
    protected int vidaActual;
    protected int vidaMaximo;
    protected int ataque;
    protected int defensa;
    protected int velocidad;
    protected List<EstadoEfecto> estados;
    protected boolean defendiendo;

    public Combatiente(String nombre, int vidaMaximo, int ataque, int defensa, int velocidad) {
        this.nombre = nombre;
        this.vidaMaximo = vidaMaximo;
        this.vidaActual = vidaMaximo;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
        this.estados = new ArrayList<>();
        this.defendiendo = false;
    }

    // ---------------------------------------------------------------- combate

    /** Ataque básico: calcula el daño (Experto) y se lo aplica al objetivo. */
    public int atacar(Combatiente objetivo) {
        int danio = calcularDanio();
        return objetivo.recibirDanio(danio);
    }

    /** Daño "bruto" de un golpe. Las subclases pueden redefinirlo (ej.: crítico del Arquero). */
    public int calcularDanio() {
        return ataque;
    }

    /**
     * Recibe un golpe: primero la defensa, después "defender" y por último los estados
     * (por ejemplo, el Escudo absorbe parte del daño).
     * Devuelve el daño que realmente se descontó de la vida.
     */
    public int recibirDanio(int cantidad) {
        int danio = cantidad - defensa / 2;
        if (danio < 1) {
            danio = 1;
        }
        if (defendiendo) {
            danio = Math.max(1, danio / 2);
        }
        for (EstadoEfecto estado : estados) {
            danio = estado.modificarDanioRecibido(danio);
        }
        quitarEstadosExpirados();
        perderVida(danio);
        return danio;
    }

    /** Pierde vida de forma directa (sin defensa). Lo usa, por ejemplo, el Veneno. */
    public void perderVida(int cantidad) {
        vidaActual = vidaActual - cantidad;
        if (vidaActual < 0) {
            vidaActual = 0;
        }
    }

    /** Recupera vida sin superar el máximo. Devuelve cuánto se curó realmente. */
    public int curar(int cantidad) {
        if (!estaVivo()) {
            return 0;
        }
        int antes = vidaActual;
        vidaActual = Math.min(vidaMaximo, vidaActual + cantidad);
        return vidaActual - antes;
    }

    public void defenderse() {
        defendiendo = true;
    }

    /**
     * Usa una habilidad. Por defecto no tiene costo (así la usan los enemigos);
     * Personaje lo redefine para descontar maná o energía primero.
     */
    public String usarHabilidad(Habilidad h, List<Combatiente> objetivos) {
        return h.ejecutar(this, objetivos);
    }

    /** Multiplicador de las curaciones que hace. Por defecto 1; el Curador lo redefine. */
    public double getPoderCuracion() {
        return 1.0;
    }

    public boolean estaVivo() {
        return vidaActual > 0;
    }

    public int obtenerVelocidad() {
        return velocidad;
    }

    // ---------------------------------------------------------------- turnos y estados

    /**
     * Se llama al comienzo del turno del combatiente.
     * Devuelve false si no puede actuar en este turno (por ejemplo, si está aturdido).
     */
    public boolean iniciarTurno() {
        defendiendo = false;
        boolean puedeActuar = true;
        for (EstadoEfecto estado : estados) {
            if (estado.impideActuar()) {
                puedeActuar = false;
            }
        }
        aplicarEstados();
        return puedeActuar && estaVivo();
    }

    /** Aplica el efecto de cada estado activo, descuenta su duración y quita los vencidos. */
    public void aplicarEstados() {
        for (EstadoEfecto estado : estados) {
            estado.aplicarEfecto(this);
            estado.tick();
        }
        quitarEstadosExpirados();
    }

    /**
     * Agrega un estado. Si ya tenía uno del mismo tipo, lo reemplaza (no se acumulan).
     * Devuelve false si el combatiente es inmune a ese estado.
     */
    public boolean agregarEstado(EstadoEfecto nuevo) {
        if (!puedeRecibirEstado(nuevo.getTipo())) {
            return false;
        }
        quitarEstado(nuevo.getTipo());
        estados.add(nuevo);
        return true;
    }

    /** Por defecto se puede recibir cualquier estado. Las subclases pueden redefinirlo. */
    public boolean puedeRecibirEstado(TipoEstado tipo) {
        return true;
    }

    /** Quita el estado del tipo indicado. Devuelve true si lo tenía. */
    public boolean quitarEstado(TipoEstado tipo) {
        for (int i = 0; i < estados.size(); i++) {
            if (estados.get(i).getTipo() == tipo) {
                estados.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean tieneEstado(TipoEstado tipo) {
        for (EstadoEfecto estado : estados) {
            if (estado.getTipo() == tipo) {
                return true;
            }
        }
        return false;
    }

    public void quitarTodosLosEstados() {
        estados.clear();
    }

    private void quitarEstadosExpirados() {
        for (int i = estados.size() - 1; i >= 0; i--) {
            if (estados.get(i).haExpirado()) {
                estados.remove(i);
            }
        }
    }

    // ---------------------------------------------------------------- "ganchos" polimórficos

    /**
     * Se llama después de que este combatiente golpea a alguien con un ataque básico.
     * Por defecto no hace nada; algunos enemigos lo redefinen (robar oro, envenenar...).
     */
    public void alGolpear(Combatiente objetivo, Batalla batalla) {
    }

    /**
     * Se llama cuando este combatiente recibe un ataque básico y sigue vivo.
     * Por defecto no hace nada; el Guerrero lo redefine para contraatacar.
     */
    public void reaccionarAAtaque(Combatiente atacante, Batalla batalla) {
    }

    /** Personaje → true (lo maneja el jugador). Enemigo → false (decide solo). */
    public abstract boolean esControladoPorJugador();

    // ---------------------------------------------------------------- getters

    public String getNombre() {
        return nombre;
    }

    public int getVidaActual() {
        return vidaActual;
    }

    public int getVidaMaximo() {
        return vidaMaximo;
    }

    public int getAtaque() {
        return ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public boolean estaDefendiendo() {
        return defendiendo;
    }

    /** Copia de la lista para que nadie de afuera modifique los estados directamente. */
    public List<EstadoEfecto> getEstados() {
        return new ArrayList<>(estados);
    }
}
