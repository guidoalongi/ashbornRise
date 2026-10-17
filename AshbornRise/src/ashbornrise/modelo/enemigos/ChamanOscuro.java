package ashbornrise.modelo.enemigos;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionAtacar;
import ashbornrise.modelo.acciones.AccionHabilidad;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.habilidades.HabilidadCuracion;
import ashbornrise.modelo.habilidades.TipoAlcance;

import java.util.ArrayList;
import java.util.List;

/**
 * Soporte: si algún enemigo está herido, lo cura con su habilidad "Curación Oscura";
 * si no, ataca. Usa la misma clase HabilidadCuracion que el Curador (los enemigos no pagan recurso).
 */
public class ChamanOscuro extends Enemigo {

    private int curacionOtorgada;
    private Habilidad curacionOscura;

    public ChamanOscuro(String nombre, int numeroBatalla) {
        super(nombre, escalar(70, numeroBatalla), escalar(12, numeroBatalla), escalar(8, numeroBatalla),
                10, escalar(45, numeroBatalla), escalar(25, numeroBatalla));
        this.curacionOtorgada = escalar(15, numeroBatalla);
        this.curacionOscura = new HabilidadCuracion("Curación Oscura", 0, TipoRecurso.MANA,
                TipoAlcance.UN_ALIADO, curacionOtorgada, 1);
    }

    @Override
    public Accion decidirAccion(Batalla batalla) {
        Enemigo masHerido = null;
        for (Enemigo e : batalla.getEnemigosVivos()) {
            boolean herido = e.getVidaActual() < e.getVidaMaximo() * 0.6;
            if (herido && (masHerido == null || e.getVidaActual() < masHerido.getVidaActual())) {
                masHerido = e;
            }
        }
        if (masHerido != null) {
            List<Combatiente> objetivos = new ArrayList<>();
            objetivos.add(masHerido);
            return new AccionHabilidad(curacionOscura, objetivos);
        }
        return new AccionAtacar(elegirAlAzar(batalla.getAliadosVivos()));
    }

    public int getCuracionOtorgada() {
        return curacionOtorgada;
    }
}
