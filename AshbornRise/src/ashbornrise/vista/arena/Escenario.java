package ashbornrise.vista.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * La arena: piso de arena con emblema, muro circular, gradas con público, columnas con antorchas,
 * estandartes, el portón y el cielo del atardecer. Todo es geometría simple (cajas y anillos).
 */
class Escenario {

    private static class Poli {
        double[] xs, ys, zs;
        double nx, ny, nz;
        double mx, my, mz;
        int color;
    }

    private static class Persona {
        double x, y, z, fase, velocidad;
        int ropa, piel;
    }

    static final double RADIO_MURO = 10.0;

    private final List<Poli> polis = new ArrayList<>();
    private final List<Persona> publico = new ArrayList<>();
    /** Puntos (x, y, z) donde arde el fuego de cada antorcha. */
    final List<double[]> antorchas = new ArrayList<>();

    private final Random azar = new Random(7);
    private int[] cielo;
    private int cieloAncho, cieloAlto;
    private final double[] bx = new double[4], by = new double[4], bz = new double[4];

    Escenario() {
        construirPiso();
        construirMuro();
        construirGradas();
        construirColumnas();
        construirPorton();
    }

    // ---------------------------------------------------------------- construcción

    private void construirPiso() {
        double paso = 0.8;
        for (double x = -10.4; x < 10.4; x += paso) {
            for (double z = -10.4; z < 10.4; z += paso) {
                double cx = x + paso / 2, cz = z + paso / 2;
                double r = Math.sqrt(cx * cx + cz * cz);
                if (r > RADIO_MURO + 0.3) {
                    continue;
                }
                int v = azar.nextInt(18) - 9;
                int color = rgb(196 + v, 160 + v, 110 + v);
                if (r > 2.7 && r < 3.3) {
                    color = rgb(176 + v, 120 + v, 84 + v);
                } else if (r < 0.9) {
                    color = rgb(170 + v, 112 + v, 80 + v);
                } else if (r > 6.3 && r < 6.75) {
                    color = rgb(170 + v, 134 + v, 92 + v);
                } else if (r > 9.0) {
                    color = rgb(120 + v, 110 + v, 112 + v);
                }
                quad(x, 0, z, x + paso, 0, z, x + paso, 0, z + paso, x, 0, z + paso, 0, 1, 0, color);
            }
        }
    }

    private void construirMuro() {
        int segmentos = 72;
        for (int i = 0; i < segmentos; i++) {
            double a0 = 2 * Math.PI * i / segmentos, a1 = 2 * Math.PI * (i + 1) / segmentos;
            int piedra = i % 2 == 0 ? rgb(96, 84, 102) : rgb(86, 76, 94);
            pared(RADIO_MURO, a0, a1, 0, 2.3, piedra);
            pared(RADIO_MURO, a0, a1, 0, 0.35, rgb(64, 56, 70));
            techo(RADIO_MURO, RADIO_MURO + 0.6, a0, a1, 2.3, rgb(120, 108, 122));
            if (i % 2 == 0) { // almenas
                pared(RADIO_MURO, a0, a1, 2.3, 2.75, rgb(104, 92, 110));
                techo(RADIO_MURO, RADIO_MURO + 0.6, a0, a1, 2.75, rgb(128, 116, 130));
                lado(RADIO_MURO, RADIO_MURO + 0.6, a0, 2.3, 2.75, rgb(80, 70, 88), true);
                lado(RADIO_MURO, RADIO_MURO + 0.6, a1, 2.3, 2.75, rgb(80, 70, 88), false);
            }
        }
    }

    private void construirGradas() {
        int segmentos = 72;
        for (int fila = 0; fila < 4; fila++) {
            double r0 = RADIO_MURO + 0.6 + fila * 0.8, r1 = r0 + 0.8;
            double y = 2.6 + fila * 0.6;
            for (int i = 0; i < segmentos; i++) {
                double a0 = 2 * Math.PI * i / segmentos, a1 = 2 * Math.PI * (i + 1) / segmentos;
                pared(r0, a0, a1, y - 0.6, y, rgb(70 - fila * 4, 58 - fila * 3, 74 - fila * 3));
                techo(r0, r1, a0, a1, y, rgb(110 - fila * 6, 94 - fila * 5, 104 - fila * 5));
            }
            // público solo en la mitad del fondo (la que ve la cámara)
            for (double a = Math.toRadians(5); a < Math.toRadians(175); a += 0.62 / (r0 + 0.4)) {
                if (azar.nextDouble() < 0.18 || Math.abs(a - Math.PI / 2) < 0.12) {
                    continue;
                }
                Persona p = new Persona();
                double r = r0 + 0.4;
                p.x = r * Math.cos(a);
                p.z = r * Math.sin(a);
                p.y = y;
                p.fase = azar.nextDouble() * 6.28;
                p.velocidad = 5 + azar.nextDouble() * 4;
                int[] ropas = {rgb(170, 50, 50), rgb(60, 90, 170), rgb(200, 160, 60), rgb(70, 140, 80),
                        rgb(130, 70, 150), rgb(210, 200, 180), rgb(90, 70, 50), rgb(200, 110, 50)};
                int[] pieles = {rgb(236, 190, 150), rgb(200, 150, 110), rgb(150, 100, 70), rgb(110, 75, 55)};
                p.ropa = ropas[azar.nextInt(ropas.length)];
                p.piel = pieles[azar.nextInt(pieles.length)];
                publico.add(p);
            }
        }
    }

    private void construirColumnas() {
        double[] angulos = {-25, 18, 52, 90 - 22, 90 + 22, 128, 162, 205};
        for (int i = 0; i < angulos.length; i++) {
            double a = Math.toRadians(angulos[i]);
            double r = RADIO_MURO - 0.45;
            double x = r * Math.cos(a), z = r * Math.sin(a);
            caja(x - 0.38, 0, z - 0.38, x + 0.38, 3.0, z + 0.38, rgb(112, 100, 116));
            caja(x - 0.48, 0, z - 0.48, x + 0.48, 0.35, z + 0.48, rgb(90, 80, 96));
            caja(x - 0.5, 3.0, z - 0.5, x + 0.5, 3.35, z + 0.5, rgb(52, 44, 48));
            antorchas.add(new double[]{x, 3.45, z});
            // estandarte entre esta columna y la siguiente
            if (i < angulos.length - 1) {
                double am = Math.toRadians((angulos[i] + angulos[i + 1]) / 2);
                if (Math.abs(Math.toDegrees(am) - 90) > 15) {
                    estandarte(am, i % 2 == 0 ? rgb(150, 32, 44) : rgb(70, 40, 130));
                }
            }
        }
    }

    private void estandarte(double a, int color) {
        double r = RADIO_MURO - 0.04;
        double ancho = 0.45 / r; // medio ancho en radianes
        double rr = r - 0.03;
        paredHaciaAdentro(r, a - ancho - 0.012, a + ancho + 0.012, 0.75, 2.2, rgb(214, 170, 72));
        paredHaciaAdentro(rr, a - ancho, a + ancho, 0.9, 2.1, color);
        paredHaciaAdentro(rr - 0.02, a - ancho * 0.35, a + ancho * 0.35, 1.3, 1.75, rgb(230, 190, 90));
    }

    private void construirPorton() {
        double a = Math.PI / 2;
        double r = RADIO_MURO - 0.02;
        double ancho = 1.3 / r;
        paredHaciaAdentro(r, a - ancho - 0.03, a + ancho + 0.03, 0, 2.15, rgb(70, 60, 74));
        paredHaciaAdentro(r - 0.02, a - ancho, a + ancho, 0, 1.95, rgb(18, 12, 18));
        for (int i = -4; i <= 4; i++) {
            double ab = a + ancho * i / 4.6;
            paredHaciaAdentro(r - 0.06, ab - 0.006, ab + 0.006, 0, 1.95, rgb(120, 104, 80));
        }
        for (int j = 1; j <= 3; j++) {
            paredHaciaAdentro(r - 0.07, a - ancho, a + ancho, 0.5 * j - 0.03, 0.5 * j + 0.03, rgb(120, 104, 80));
        }
    }

    // ---------------------------------------------------------------- piezas

    private static int rgb(int r, int g, int b) {
        return (Math.max(0, Math.min(255, r)) << 16) | (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b));
    }

    private void quad(double x0, double y0, double z0, double x1, double y1, double z1,
                      double x2, double y2, double z2, double x3, double y3, double z3,
                      double nx, double ny, double nz, int color) {
        Poli p = new Poli();
        p.xs = new double[]{x0, x1, x2, x3};
        p.ys = new double[]{y0, y1, y2, y3};
        p.zs = new double[]{z0, z1, z2, z3};
        p.nx = nx;
        p.ny = ny;
        p.nz = nz;
        p.mx = (x0 + x1 + x2 + x3) / 4;
        p.my = (y0 + y1 + y2 + y3) / 4;
        p.mz = (z0 + z1 + z2 + z3) / 4;
        p.color = color;
        polis.add(p);
    }

    /** Pared vertical de un anillo, mirando hacia el centro. */
    private void pared(double r, double a0, double a1, double y0, double y1, int color) {
        paredHaciaAdentro(r, a0, a1, y0, y1, color);
    }

    private void paredHaciaAdentro(double r, double a0, double a1, double y0, double y1, int color) {
        double am = (a0 + a1) / 2;
        quad(r * Math.cos(a0), y0, r * Math.sin(a0), r * Math.cos(a1), y0, r * Math.sin(a1),
                r * Math.cos(a1), y1, r * Math.sin(a1), r * Math.cos(a0), y1, r * Math.sin(a0),
                -Math.cos(am), 0, -Math.sin(am), color);
    }

    private void techo(double r0, double r1, double a0, double a1, double y, int color) {
        quad(r0 * Math.cos(a0), y, r0 * Math.sin(a0), r0 * Math.cos(a1), y, r0 * Math.sin(a1),
                r1 * Math.cos(a1), y, r1 * Math.sin(a1), r1 * Math.cos(a0), y, r1 * Math.sin(a0), 0, 1, 0, color);
    }

    private void lado(double r0, double r1, double a, double y0, double y1, int color, boolean antes) {
        double s = antes ? 1 : -1;
        double nx = -Math.sin(a) * -s, nz = Math.cos(a) * -s;
        quad(r0 * Math.cos(a), y0, r0 * Math.sin(a), r1 * Math.cos(a), y0, r1 * Math.sin(a),
                r1 * Math.cos(a), y1, r1 * Math.sin(a), r0 * Math.cos(a), y1, r0 * Math.sin(a), nx, 0, nz, color);
    }

    private void caja(double x0, double y0, double z0, double x1, double y1, double z1, int color) {
        quad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0, 0, -1, color);
        quad(x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, 0, 0, 1, color);
        quad(x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, -1, 0, 0, color);
        quad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 1, 0, 0, color);
        quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0, 1, 0, color);
    }

    // ---------------------------------------------------------------- dibujo

    /** @param emocion 0 = público tranquilo, 1 = público eufórico */
    void dibujar(Motor3D m, double tiempo, double emocion) {
        for (Poli p : polis) {
            if (!m.miraALaCamara(p.mx, p.my, p.mz, p.nx, p.ny, p.nz)) {
                continue;
            }
            int color = m.iluminar(p.color, p.nx, p.ny, p.nz, m.distancia(p.mx, p.my, p.mz));
            m.poligono(p.xs, p.ys, p.zs, 4, color, Motor3D.NORMAL);
        }
        for (Persona p : publico) {
            double salto = Math.abs(Math.sin(tiempo * p.velocidad * (0.5 + emocion) + p.fase)) * (0.03 + 0.2 * emocion);
            double y = p.y + salto;
            cajaDinamica(m, p.x, y, p.z, 0.17, 0.42, 0.13, p.ropa);
            cajaDinamica(m, p.x, y + 0.42, p.z, 0.11, 0.2, 0.1, p.piel);
            if (emocion > 0.35) {
                double brazo = 0.25 + 0.1 * Math.sin(tiempo * 12 + p.fase);
                cajaDinamica(m, p.x - 0.2, y + 0.3, p.z, 0.04, brazo, 0.04, p.piel);
                cajaDinamica(m, p.x + 0.2, y + 0.3, p.z, 0.04, brazo, 0.04, p.piel);
            }
        }
    }

    /** Caja centrada en (x, z), apoyada en y, con medio ancho/medio fondo hx/hz y alto h. */
    private void cajaDinamica(Motor3D m, double x, double y, double z, double hx, double h, double hz, int color) {
        double d = m.distancia(x, y, z);
        caraDinamica(m, d, color, 0, 0, -1, x - hx, y, z - hz, x + hx, y, z - hz, x + hx, y + h, z - hz, x - hx, y + h, z - hz);
        caraDinamica(m, d, color, 0, 0, 1, x - hx, y, z + hz, x - hx, y + h, z + hz, x + hx, y + h, z + hz, x + hx, y, z + hz);
        caraDinamica(m, d, color, -1, 0, 0, x - hx, y, z - hz, x - hx, y + h, z - hz, x - hx, y + h, z + hz, x - hx, y, z + hz);
        caraDinamica(m, d, color, 1, 0, 0, x + hx, y, z - hz, x + hx, y, z + hz, x + hx, y + h, z + hz, x + hx, y + h, z - hz);
        caraDinamica(m, d, color, 0, 1, 0, x - hx, y + h, z - hz, x + hx, y + h, z - hz, x + hx, y + h, z + hz, x - hx, y + h, z + hz);
    }

    private void caraDinamica(Motor3D m, double d, int color, double nx, double ny, double nz, double... v) {
        if (!m.miraALaCamara(v[0], v[1], v[2], nx, ny, nz)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            bx[i] = v[i * 3];
            by[i] = v[i * 3 + 1];
            bz[i] = v[i * 3 + 2];
        }
        m.poligono(bx, by, bz, 4, m.iluminar(color, nx, ny, nz, d), Motor3D.NORMAL);
    }

    /** Fondo: cielo de atardecer con estrellas y luna. */
    int[] cielo(int w, int h) {
        if (cielo != null && w == cieloAncho && h == cieloAlto) {
            return cielo;
        }
        cieloAncho = w;
        cieloAlto = h;
        cielo = new int[w * h];
        Random r = new Random(3);
        for (int y = 0; y < h; y++) {
            double t = y / (h * 0.7);
            int c;
            if (t < 0.6) {
                c = Motor3D.mezclar(rgb(8, 6, 24), rgb(44, 24, 70), t / 0.6);
            } else if (t < 1) {
                c = Motor3D.mezclar(rgb(44, 24, 70), rgb(150, 70, 74), (t - 0.6) / 0.4);
            } else {
                c = rgb(40, 26, 40);
            }
            for (int x = 0; x < w; x++) {
                cielo[y * w + x] = c;
            }
        }
        for (int i = 0; i < w * h / 260; i++) {
            int x = r.nextInt(w), y = r.nextInt(Math.max(1, h / 2));
            int b = 120 + r.nextInt(135);
            cielo[y * w + x] = rgb(b, b, Math.min(255, b + 20));
        }
        int lx = (int) (w * 0.84), ly = (int) (h * 0.13), lr = Math.max(3, h / 22);
        for (int y = ly - lr * 3; y <= ly + lr * 3; y++) {
            for (int x = lx - lr * 3; x <= lx + lr * 3; x++) {
                if (x < 0 || y < 0 || x >= w || y >= h) {
                    continue;
                }
                double d = Math.sqrt((x - lx) * (x - lx) + (y - ly) * (y - ly));
                int i = y * w + x;
                if (d <= lr) {
                    cielo[i] = (x - lx + y - ly > lr * 0.6) ? rgb(206, 196, 176) : rgb(240, 232, 210);
                } else if (d < lr * 3) {
                    int glow = (int) (40 * (1 - (d - lr) / (lr * 2)));
                    cielo[i] = Motor3D.sumar(cielo[i], rgb(glow, glow, glow / 2 + glow / 3));
                }
            }
        }
        return cielo;
    }
}
