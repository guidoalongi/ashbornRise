package ashbornrise.vista.arena;

import java.awt.Color;

/** Números de daño o curación y avisos que suben y se desvanecen sobre un combatiente. */
class TextoFlotante {

    double x, y, z;
    final String texto;
    final Color color;
    final int tamanio;
    double edad;
    final double duracion;

    TextoFlotante(double x, double y, double z, String texto, Color color, int tamanio, double duracion) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.texto = texto;
        this.color = color;
        this.tamanio = tamanio;
        this.duracion = duracion;
    }

    boolean actualizar(double dt) {
        edad += dt;
        y += dt * (edad < 0.25 ? 2.2 : 0.35);
        return edad < duracion;
    }
}
