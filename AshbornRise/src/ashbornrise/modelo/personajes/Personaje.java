package ashbornrise.modelo.personajes;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.Inventario;
import ashbornrise.modelo.items.Equipable;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.items.TipoRanura;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Personaje jugable. Agrega a Combatiente: nivel, experiencia, recurso (maná o energía)
 * habilidades y equipamiento. Cada subclase define sus estadísticas, sus habilidades y cómo mejora al subir de nivel.
 */
public abstract class Personaje extends Combatiente {

    private static final int ENERGIA_POR_ATAQUE = 8;
    private static final int ENERGIA_POR_GOLPE_RECIBIDO = 5;
    private static final int ENERGIA_POR_DEFENDER = 10;
    private static final int MANA_POR_DEFENDER = 5;

    private int nivel;
    private int experiencia;
    private int recursoActual;
    private int recursoMaximo;
    private TipoRecurso tipoRecurso;
    private int regeneracionMana;
    private List<Habilidad> habilidades;
    private Map<TipoRanura, Equipable> equipamiento;   // como máximo un ítem por ranura

    public Personaje(String nombre, int vida, int ataque, int defensa, int velocidad,
                     TipoRecurso tipoRecurso, int recursoMaximo, int regeneracionMana) {
        super(nombre, vida, ataque, defensa, velocidad);
        this.nivel = 1;
        this.experiencia = 0;
        this.tipoRecurso = tipoRecurso;
        this.recursoMaximo = recursoMaximo;
        this.regeneracionMana = regeneracionMana;
        this.habilidades = new ArrayList<>();
        this.equipamiento = new EnumMap<>(TipoRanura.class);
        // El maná arranca lleno; la energía arranca a la mitad y se carga peleando.
        if (tipoRecurso == TipoRecurso.MANA) {
            this.recursoActual = recursoMaximo;
        } else {
            this.recursoActual = recursoMaximo / 2;
        }
    }

    // ---------------------------------------------------------------- acciones

    @Override
    public int atacar(Combatiente objetivo) {
        int danio = super.atacar(objetivo);
        ganarEnergia(ENERGIA_POR_ATAQUE);
        return danio;
    }

    @Override
    public int recibirDanio(int cantidad) {
        int danio = super.recibirDanio(cantidad);
        if (estaVivo()) {
            ganarEnergia(ENERGIA_POR_GOLPE_RECIBIDO);
        }
        return danio;
    }

    @Override
    public void defenderse() {
        super.defenderse();
        if (tipoRecurso == TipoRecurso.ENERGIA) {
            recuperarRecurso(ENERGIA_POR_DEFENDER);
        } else {
            recuperarRecurso(MANA_POR_DEFENDER);
        }
    }

    @Override
    public boolean iniciarTurno() {
        boolean puedeActuar = super.iniciarTurno();
        if (tipoRecurso == TipoRecurso.MANA && estaVivo()) {
            recuperarRecurso(regeneracionMana);
        }
        return puedeActuar;
    }

    /** El jugador decide qué hace un Personaje. */
    @Override
    public boolean esControladoPorJugador() {
        return true;
    }

    /** Valida si tiene la habilidad, el tipo de recurso correcto y la cantidad suficiente. */
    @Override
    public boolean puedeUsar(Habilidad h) {
        return estaVivo()
                && habilidades.contains(h)
                && h.getTipoRecurso() == tipoRecurso
                && recursoActual >= h.getCostoRecurso();
    }

    /** Descuenta el costo y después delega el efecto en la propia habilidad. */
    @Override
    public String usarHabilidad(Habilidad h, List<Combatiente> objetivos) {
        consumirRecurso(h.getCostoRecurso());
        return super.usarHabilidad(h, objetivos);
    }

    /** Usa un ítem del inventario sobre un aliado (puede ser él mismo). */
    public String usarItem(Item item, Personaje objetivo, Inventario inventario) {
        return item.usar(objetivo, inventario);
    }

    // ---------------------------------------------------------------- equipamiento

    /** Equipa el ítem en su ranura. Devuelve el que estaba antes en esa ranura (o null). */
    public Equipable equipar(Equipable item) {
        return equipamiento.put(item.getRanura(), item);
    }

    /** Saca el ítem de esa ranura. Devuelve el ítem quitado (o null si estaba vacía). */
    public Equipable desequipar(TipoRanura ranura) {
        return equipamiento.remove(ranura);
    }

    /** Copia de lo que tiene equipado, en el orden de las ranuras. */
    public List<Equipable> getEquipamiento() {
        return new ArrayList<>(equipamiento.values());
    }

    public int getBonusAtaque() {
        int total = 0;
        for (Equipable e : equipamiento.values()) {
            total += e.getBonusAtaque();
        }
        return total;
    }

    public int getBonusDefensa() {
        int total = 0;
        for (Equipable e : equipamiento.values()) {
            total += e.getBonusDefensa();
        }
        return total;
    }

    public int getBonusVelocidad() {
        int total = 0;
        for (Equipable e : equipamiento.values()) {
            total += e.getBonusVelocidad();
        }
        return total;
    }

    /** Estadísticas efectivas = base + equipamiento. Como Combatiente usa estos getters para
     *  calcular daño, defensa y orden de turnos, el equipo influye en todo el combate. */
    @Override
    public int getAtaque() {
        return super.getAtaque() + getBonusAtaque();
    }

    @Override
    public int getDefensa() {
        return Math.max(0, super.getDefensa() + getBonusDefensa());
    }

    @Override
    public int getVelocidad() {
        return Math.max(1, super.getVelocidad() + getBonusVelocidad());
    }

    // ---------------------------------------------------------------- recurso

    public void consumirRecurso(int cantidad) {
        recursoActual = Math.max(0, recursoActual - cantidad);
    }

    /** Suma recurso sin pasarse del máximo. Devuelve cuánto se recuperó realmente. */
    public int recuperarRecurso(int cantidad) {
        int antes = recursoActual;
        recursoActual = Math.min(recursoMaximo, recursoActual + cantidad);
        return recursoActual - antes;
    }

    private void ganarEnergia(int cantidad) {
        if (tipoRecurso == TipoRecurso.ENERGIA) {
            recuperarRecurso(cantidad);
        }
    }

    // ---------------------------------------------------------------- progresión

    /** Experiencia necesaria para pasar del nivel actual al siguiente. */
    public int experienciaParaSubir() {
        return 50 * nivel;
    }

    /** Suma experiencia y sube de nivel las veces que haga falta. Devuelve cuántos niveles subió. */
    public int ganarExperiencia(int cantidad) {
        experiencia = experiencia + cantidad;
        int nivelesSubidos = 0;
        while (experiencia >= experienciaParaSubir()) {
            experiencia = experiencia - experienciaParaSubir();
            subirNivel();
            nivelesSubidos++;
        }
        return nivelesSubidos;
    }

    /** Sube un nivel. Lo que mejora depende de la clase (método abstracto, Polimorfismo). */
    public void subirNivel() {
        int vidaAntes = vidaMaximo;
        int recursoAntes = recursoMaximo;
        nivel++;
        aplicarMejorasDeNivel();
        // Lo que aumentó el máximo también se suma al valor actual.
        vidaActual = vidaActual + (vidaMaximo - vidaAntes);
        recursoActual = recursoActual + (recursoMaximo - recursoAntes);
    }

    /** Cada clase decide qué estadísticas mejora al subir de nivel. */
    protected abstract void aplicarMejorasDeNivel();

    /** Ayuda para las subclases: suma a cada estadística. */
    protected void mejorarEstadisticas(int vida, int recurso, int ataque, int defensa) {
        this.vidaMaximo += vida;
        this.recursoMaximo += recurso;
        this.ataque += ataque;
        this.defensa += defensa;
    }

    // ---------------------------------------------------------------- entre batallas

    /** Después de ganar: se quitan los estados, el maná se llena y los caídos vuelven con poca vida. */
    public void recuperarTrasBatalla() {
        quitarTodosLosEstados();
        defendiendo = false;
        if (!estaVivo()) {
            vidaActual = Math.max(1, vidaMaximo / 4);
        }
        if (tipoRecurso == TipoRecurso.MANA) {
            recursoActual = recursoMaximo;
        }
    }

    /** Recuperación total (se usa al reintentar una batalla perdida). */
    public void restaurarCompleto() {
        quitarTodosLosEstados();
        defendiendo = false;
        vidaActual = vidaMaximo;
        if (tipoRecurso == TipoRecurso.MANA) {
            recursoActual = recursoMaximo;
        } else {
            recursoActual = recursoMaximo / 2;
        }
    }

    /**
     * Reconstruye un personaje guardado: se sube de nivel desde el nivel 1 la misma
     * cantidad de veces (así las estadísticas quedan iguales) y se restauran los valores actuales.
     */
    public void restaurarProgreso(int nivelGuardado, int experienciaGuardada, int vida, int recurso) {
        while (nivel < nivelGuardado) {
            subirNivel();
        }
        this.experiencia = experienciaGuardada;
        this.vidaActual = Math.max(0, Math.min(vida, vidaMaximo));
        this.recursoActual = Math.max(0, Math.min(recurso, recursoMaximo));
    }

    // ---------------------------------------------------------------- consultas

    /** Nombre de la clase para mostrar y para guardar ("Guerrero", "Mago", ...). */
    public abstract String getNombreClase();

    protected void aprenderHabilidad(Habilidad h) {
        habilidades.add(h);
    }

    public List<Habilidad> getHabilidades() {
        return new ArrayList<>(habilidades);
    }

    public int getNivel() {
        return nivel;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public int getRecursoActual() {
        return recursoActual;
    }

    public int getRecursoMaximo() {
        return recursoMaximo;
    }

    public TipoRecurso getTipoRecurso() {
        return tipoRecurso;
    }
}
