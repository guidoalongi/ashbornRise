package ashbornrise.vista.arena;

/** Un cuadradito que se mueve, cae y se desvanece (chispas, fuego, humo, magia...). */
class Particula {

    double x, y, z;
    double vx, vy, vz;
    double gravedad;
    double friccion = 1.0;
    double tamanio;
    double duracion;
    double edad;
    int color;
    int colorFinal;
    boolean brillo;

    Particula(double x, double y, double z, double vx, double vy, double vz,
              double tamanio, double duracion, int color, int colorFinal, boolean brillo) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.tamanio = tamanio;
        this.duracion = duracion;
        this.color = color;
        this.colorFinal = colorFinal;
        this.brillo = brillo;
    }

    /** Devuelve false cuando la partícula ya terminó. */
    boolean actualizar(double dt) {
        edad += dt;
        vy -= gravedad * dt;
        double f = Math.pow(friccion, dt * 60);
        vx *= f;
        vy *= f;
        vz *= f;
        x += vx * dt;
        y += vy * dt;
        z += vz * dt;
        if (y < 0.02 && gravedad > 0) {
            y = 0.02;
            vy = -vy * 0.3;
            vx *= 0.6;
            vz *= 0.6;
        }
        return edad < duracion;
    }

    void dibujar(Motor3D m) {
        double t = duracion <= 0 ? 0 : Math.min(1, edad / duracion);
        int c = Motor3D.mezclar(color, colorFinal, t);
        double tam = tamanio * (1 - 0.6 * t);
        if (brillo) {
            double apagado = 1 - t * t;
            m.punto(x, y, z, tam * 2.4, Motor3D.escalarColor(c, 0.22 * apagado), true);
            m.punto(x, y, z, tam, Motor3D.escalarColor(c, apagado), true);
        } else {
            m.punto(x, y, z, tam, c, false);
        }
    }
}
