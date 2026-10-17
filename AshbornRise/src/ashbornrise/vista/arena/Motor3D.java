package ashbornrise.vista.arena;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * Motor 3D por software (sin librerías): cámara en perspectiva, recorte contra el plano cercano,
 * z-buffer y polígonos de color plano. Dibuja en una imagen chica que después se agranda
 * sin suavizar, para que combine con el pixel art de los sprites.
 */
class Motor3D {

    static final int NORMAL = 0;
    static final int SOMBRA = 1;
    static final int BRILLO = 2;

    private static final double CERCA = 0.2;

    int ancho;
    int alto;
    BufferedImage imagen;
    private int[] pixeles;
    private float[] profundidad; // guarda 1/z: más grande = más cerca
    private int[] marca;         // evita pintar dos veces el mismo píxel en sombras y brillos
    private int marcaActual;

    // cámara
    double camX, camY, camZ;
    private double rx, ry, rz, ux, uy, uz, fx, fy, fz;
    double focal;

    // luz
    private double luzX = -0.35, luzY = 0.8, luzZ = -0.48;
    double intensidad = 1.0;
    private static final int NIEBLA_R = 30, NIEBLA_G = 20, NIEBLA_B = 48;

    // buffers de trabajo (para no crear objetos en cada polígono)
    private final double[] cx = new double[24], cy = new double[24], cz = new double[24];
    private final double[] tx = new double[24], ty = new double[24], tz = new double[24];

    Motor3D() {
        double n = Math.sqrt(luzX * luzX + luzY * luzY + luzZ * luzZ);
        luzX /= n;
        luzY /= n;
        luzZ /= n;
    }

    void redimensionar(int w, int h) {
        w = Math.max(w, 16);
        h = Math.max(h, 16);
        if (w == ancho && h == alto) {
            return;
        }
        ancho = w;
        alto = h;
        imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        pixeles = ((DataBufferInt) imagen.getRaster().getDataBuffer()).getData();
        profundidad = new float[ancho * alto];
        marca = new int[ancho * alto];
    }

    void limpiar(int[] fondo) {
        System.arraycopy(fondo, 0, pixeles, 0, pixeles.length);
        Arrays.fill(profundidad, 0f);
    }

    /** Ubica la cámara en (px,py,pz) mirando a (ox,oy,oz). */
    void mirar(double px, double py, double pz, double ox, double oy, double oz, double fovHorizontal, double fovVerticalMinimo) {
        camX = px;
        camY = py;
        camZ = pz;
        fx = ox - px;
        fy = oy - py;
        fz = oz - pz;
        double n = Math.sqrt(fx * fx + fy * fy + fz * fz);
        fx /= n;
        fy /= n;
        fz /= n;
        rx = fz;
        ry = 0;
        rz = -fx;
        n = Math.sqrt(rx * rx + rz * rz);
        rx /= n;
        rz /= n;
        ux = fy * rz - fz * ry;
        uy = fz * rx - fx * rz;
        uz = fx * ry - fy * rx;
        double f1 = (ancho / 2.0) / Math.tan(Math.toRadians(fovHorizontal) / 2);
        double f2 = (alto / 2.0) / Math.tan(Math.toRadians(fovVerticalMinimo) / 2);
        focal = Math.min(f1, f2);
    }

    /** Proyecta un punto del mundo a la pantalla chica: out = {x, y, distancia}. False si queda detrás. */
    boolean proyectar(double x, double y, double z, double[] out) {
        double dx = x - camX, dy = y - camY, dz = z - camZ;
        double zc = dx * fx + dy * fy + dz * fz;
        if (zc < CERCA) {
            return false;
        }
        double xc = dx * rx + dy * ry + dz * rz;
        double yc = dx * ux + dy * uy + dz * uz;
        out[0] = ancho / 2.0 + xc * focal / zc;
        out[1] = alto / 2.0 - yc * focal / zc;
        out[2] = zc;
        return true;
    }

    /** Luz direccional + ambiente, con un tono cálido y niebla según la distancia. */
    int iluminar(int rgb, double nx, double ny, double nz, double distancia) {
        double d = nx * luzX + ny * luzY + nz * luzZ;
        double k = (0.5 + 0.62 * Math.max(0, d)) * intensidad;
        double r = ((rgb >> 16) & 255) * k * 1.05;
        double g = ((rgb >> 8) & 255) * k * 0.97;
        double b = (rgb & 255) * k * 0.9;
        double f = Math.max(0, Math.min(0.7, (distancia - 10) / 20));
        r = r * (1 - f) + NIEBLA_R * f;
        g = g * (1 - f) + NIEBLA_G * f;
        b = b * (1 - f) + NIEBLA_B * f;
        return (limitar(r) << 16) | (limitar(g) << 8) | limitar(b);
    }

    double distancia(double x, double y, double z) {
        double dx = x - camX, dy = y - camY, dz = z - camZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** True si una cara con esa normal mira hacia la cámara. */
    boolean miraALaCamara(double x, double y, double z, double nx, double ny, double nz) {
        return (x - camX) * nx + (y - camY) * ny + (z - camZ) * nz < 0;
    }

    /** Dibuja un polígono convexo (en coordenadas del mundo). */
    void poligono(double[] xs, double[] ys, double[] zs, int n, int color, int modo) {
        for (int i = 0; i < n; i++) {
            double dx = xs[i] - camX, dy = ys[i] - camY, dz = zs[i] - camZ;
            cx[i] = dx * rx + dy * ry + dz * rz;
            cy[i] = dx * ux + dy * uy + dz * uz;
            cz[i] = dx * fx + dy * fy + dz * fz;
        }
        // recorte contra el plano cercano
        int m = 0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            boolean dentroI = cz[i] >= CERCA, dentroJ = cz[j] >= CERCA;
            if (dentroI) {
                tx[m] = cx[i];
                ty[m] = cy[i];
                tz[m] = cz[i];
                m++;
            }
            if (dentroI != dentroJ) {
                double t = (CERCA - cz[i]) / (cz[j] - cz[i]);
                tx[m] = cx[i] + t * (cx[j] - cx[i]);
                ty[m] = cy[i] + t * (cy[j] - cy[i]);
                tz[m] = CERCA;
                m++;
            }
        }
        if (m < 3) {
            return;
        }
        for (int i = 0; i < m; i++) {
            cx[i] = ancho / 2.0 + tx[i] * focal / tz[i];
            cy[i] = alto / 2.0 - ty[i] * focal / tz[i];
            cz[i] = 1.0 / tz[i];
        }
        marcaActual++;
        for (int i = 1; i < m - 1; i++) {
            triangulo(0, i, i + 1, color, modo);
        }
    }

    private void triangulo(int a, int b, int c, int color, int modo) {
        double x0 = cx[a], y0 = cy[a], x1 = cx[b], y1 = cy[b], x2 = cx[c], y2 = cy[c];
        double area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
        if (Math.abs(area) < 1e-7) {
            return;
        }
        int minX = Math.max(0, (int) Math.floor(Math.min(x0, Math.min(x1, x2))));
        int maxX = Math.min(ancho - 1, (int) Math.ceil(Math.max(x0, Math.max(x1, x2))));
        int minY = Math.max(0, (int) Math.floor(Math.min(y0, Math.min(y1, y2))));
        int maxY = Math.min(alto - 1, (int) Math.ceil(Math.max(y0, Math.max(y1, y2))));
        if (minX > maxX || minY > maxY) {
            return;
        }
        double inv = 1.0 / area;
        double z0 = cz[a], z1 = cz[b], z2 = cz[c];
        // las coordenadas baricéntricas cambian linealmente: se avanzan sumando un paso por píxel
        double pasoW0 = (y1 - y2) * inv, pasoW1 = (y2 - y0) * inv;
        double pasoZ = pasoW0 * (z0 - z2) + pasoW1 * (z1 - z2);
        double px0 = minX + 0.5;
        for (int y = minY; y <= maxY; y++) {
            double py = y + 0.5;
            int fila = y * ancho;
            double w0 = ((x1 - px0) * (y2 - py) - (x2 - px0) * (y1 - py)) * inv;
            double w1 = ((x2 - px0) * (y0 - py) - (x0 - px0) * (y2 - py)) * inv;
            double iz0 = z2 + w0 * (z0 - z2) + w1 * (z1 - z2);
            for (int x = minX; x <= maxX; x++, w0 += pasoW0, w1 += pasoW1, iz0 += pasoZ) {
                if (w0 < -1e-9 || w1 < -1e-9 || 1 - w0 - w1 < -1e-9) {
                    continue;
                }
                int i = fila + x;
                float iz = (float) iz0;
                if (iz <= profundidad[i]) {
                    continue;
                }
                if (modo == NORMAL) {
                    profundidad[i] = iz;
                    pixeles[i] = color;
                } else if (marca[i] != marcaActual) {
                    marca[i] = marcaActual;
                    pixeles[i] = modo == SOMBRA ? oscurecer(pixeles[i]) : sumar(pixeles[i], color);
                }
            }
        }
    }

    /** Un cuadradito (partícula) en el mundo, del tamaño indicado en metros. */
    void punto(double x, double y, double z, double tamanio, int color, boolean brillo) {
        double dx = x - camX, dy = y - camY, dz = z - camZ;
        double zc = dx * fx + dy * fy + dz * fz;
        if (zc < CERCA) {
            return;
        }
        double sx = ancho / 2.0 + (dx * rx + dy * ry + dz * rz) * focal / zc;
        double sy = alto / 2.0 - (dx * ux + dy * uy + dz * uz) * focal / zc;
        double lado = Math.max(1, tamanio * focal / zc);
        int x0 = (int) Math.round(sx - lado / 2), y0 = (int) Math.round(sy - lado / 2);
        int x1 = x0 + (int) Math.round(lado), y1 = y0 + (int) Math.round(lado);
        float iz = (float) (1.0 / zc);
        for (int yy = Math.max(0, y0); yy < Math.min(alto, y1); yy++) {
            for (int xx = Math.max(0, x0); xx < Math.min(ancho, x1); xx++) {
                int i = yy * ancho + xx;
                if (iz > profundidad[i]) {
                    if (brillo) {
                        pixeles[i] = sumar(pixeles[i], color);
                    } else {
                        pixeles[i] = color;
                        profundidad[i] = iz;
                    }
                }
            }
        }
    }

    private static int oscurecer(int c) {
        return ((((c >> 16) & 255) * 130 >> 8) << 16) | ((((c >> 8) & 255) * 130 >> 8) << 8) | ((c & 255) * 140 >> 8);
    }

    static int sumar(int a, int b) {
        int r = Math.min(255, ((a >> 16) & 255) + ((b >> 16) & 255));
        int g = Math.min(255, ((a >> 8) & 255) + ((b >> 8) & 255));
        int bl = Math.min(255, (a & 255) + (b & 255));
        return (r << 16) | (g << 8) | bl;
    }

    static int mezclar(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    static int escalarColor(int c, double k) {
        return (limitar(((c >> 16) & 255) * k) << 16) | (limitar(((c >> 8) & 255) * k) << 8) | limitar((c & 255) * k);
    }

    private static int limitar(double v) {
        return v < 0 ? 0 : (v > 255 ? 255 : (int) v);
    }
}
