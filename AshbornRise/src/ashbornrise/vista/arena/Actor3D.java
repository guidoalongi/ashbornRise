package ashbornrise.vista.arena;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.estados.TipoEstado;

/**
 * Representación 3D de un combatiente en la arena. Guarda la posición, los desplazamientos
 * de las animaciones y lo que se está mostrando (vida, estados), que se sincroniza con el
 * modelo recién cuando la animación llega al momento del impacto.
 */
class Actor3D {

    final Combatiente combatiente;
    final ModeloVoxel modelo;
    final double escala;   // metros por píxel del sprite
    final int frente;      // +1 mira hacia +x (personajes), -1 hacia -x (enemigos)

    double baseX, baseZ;
    double ox, oy, oz;           // desplazamiento por animación
    double inclinacion;          // radianes, positivo = hacia adelante
    double estirar = 1;          // 1 normal, <1 aplastado
    double caida;                // 0 de pie, 1 tirado
    double destello;             // 0..1 se pinta de blanco (golpe)
    double empuje;               // retroceso al recibir un golpe (se va solo)
    int colorTinte = 0xffffff;
    final double fase;

    // lo que se muestra
    double vidaMostrada;
    int vidaSincronizada;
    boolean vivo;
    boolean veneno, escudo, aturdido, guardia;

    Actor3D(Combatiente c, ModeloVoxel modelo, double escala, int frente) {
        this.combatiente = c;
        this.modelo = modelo;
        this.escala = escala;
        this.frente = frente;
        this.fase = Math.random() * 6.28;
        sincronizar();
        vidaMostrada = vidaSincronizada;
    }

    void sincronizar() {
        vidaSincronizada = combatiente.getVidaActual();
        vivo = combatiente.estaVivo();
        veneno = combatiente.tieneEstado(TipoEstado.VENENO);
        escudo = combatiente.tieneEstado(TipoEstado.ESCUDO);
        aturdido = combatiente.tieneEstado(TipoEstado.ATURDIMIENTO);
        guardia = combatiente.estaDefendiendo();
    }

    /** Diferencia de vida que todavía no se mostró. */
    int vidaPendiente() {
        return combatiente.getVidaActual() - vidaSincronizada;
    }

    boolean estadosPendientes() {
        return vivo != combatiente.estaVivo()
                || veneno != combatiente.tieneEstado(TipoEstado.VENENO)
                || escudo != combatiente.tieneEstado(TipoEstado.ESCUDO)
                || aturdido != combatiente.tieneEstado(TipoEstado.ATURDIMIENTO)
                || guardia != combatiente.estaDefendiendo();
    }

    double x() {
        return baseX + ox - frente * empuje;
    }

    double y() {
        return oy;
    }

    double z() {
        return baseZ + oz;
    }

    double alto() {
        return modelo.altoPx * escala;
    }

    double ancho() {
        return modelo.anchoPx * escala;
    }
}
