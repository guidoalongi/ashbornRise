package ashbornrise.vista.arena;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Personaje 3D hecho de vóxeles a partir de su sprite: cada píxel se vuelve un bloque.
 * Los píxeles del interior son más gruesos que los del borde, así el cuerpo queda "redondeado"
 * y el personaje respeta exactamente el dibujo del sprite visto de frente.
 */
class ModeloVoxel {

    /** Normales de las 6 caras posibles: frente, atrás, izquierda, derecha, arriba, abajo. */
    private static final double[][] NORMALES = {{0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, -1, 0}};

    private float[] vertices = new float[1024];
    private byte[] normales = new byte[86];
    private int[] colores = new int[86];
    private int caras;

    final int anchoPx;
    final int altoPx;

    private final double[] xs = new double[4], ys = new double[4], zs = new double[4];

    private ModeloVoxel(int anchoPx, int altoPx) {
        this.anchoPx = anchoPx;
        this.altoPx = altoPx;
    }

    static ModeloVoxel desdeImagen(BufferedImage original, boolean espejar) {
        BufferedImage img = preparar(original);
        int w = img.getWidth(), h = img.getHeight();
        // grilla en coordenadas del modelo: x hacia la derecha, y hacia arriba
        int[][] color = new int[w][h];
        boolean[][] solido = new boolean[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int mx = espejar ? w - 1 - x : x;
                int my = h - 1 - y;
                solido[mx][my] = (argb >>> 24) > 128;
                color[mx][my] = argb & 0xffffff;
            }
        }
        // distancia de cada píxel al borde del dibujo
        int[][] dist = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                dist[x][y] = solido[x][y] ? 99 : 0;
            }
        }
        for (int paso = 0; paso < 4; paso++) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (solido[x][y]) {
                        int vecino = Math.min(Math.min(valor(dist, x - 1, y, w, h), valor(dist, x + 1, y, w, h)),
                                Math.min(valor(dist, x, y - 1, w, h), valor(dist, x, y + 1, w, h)));
                        dist[x][y] = Math.min(dist[x][y], vecino + 1);
                    }
                }
            }
        }
        double[][] grosor = new double[w][h]; // mitad del grosor en píxeles
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                grosor[x][y] = solido[x][y] ? 0.5 + 0.5 * Math.min(dist[x][y] - 1, 3) : 0;
            }
        }

        ModeloVoxel m = new ModeloVoxel(w, h);
        double cx = w / 2.0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (!solido[x][y]) {
                    continue;
                }
                double t = grosor[x][y];
                double x0 = x - cx, x1 = x0 + 1, y0 = y, y1 = y + 1;
                int c = color[x][y];
                m.cara(0, c, x0, y0, -t, x1, y0, -t, x1, y1, -t, x0, y1, -t);
                m.cara(1, c, x0, y0, t, x0, y1, t, x1, y1, t, x1, y0, t);
                m.lateral(2, c, t, grosorEn(grosor, x - 1, y, w, h), x0, y0, y1);
                m.lateral(3, c, t, grosorEn(grosor, x + 1, y, w, h), x1, y0, y1);
                m.tapa(4, c, t, grosorEn(grosor, x, y + 1, w, h), x0, x1, y1);
                m.tapa(5, c, t, grosorEn(grosor, x, y - 1, w, h), x0, x1, y0);
            }
        }
        return m;
    }

    private static int valor(int[][] d, int x, int y, int w, int h) {
        return x < 0 || y < 0 || x >= w || y >= h ? 0 : d[x][y];
    }

    private static double grosorEn(double[][] g, int x, int y, int w, int h) {
        return x < 0 || y < 0 || x >= w || y >= h ? 0 : g[x][y];
    }

    /** Recorta los bordes vacíos y achica las imágenes grandes (por ejemplo un PNG externo). */
    private static BufferedImage preparar(BufferedImage img) {
        int minX = img.getWidth(), minY = img.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if ((img.getRGB(x, y) >>> 24) > 128) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        BufferedImage recorte = img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
        if (recorte.getHeight() <= 40) {
            return recorte;
        }
        int alto = 26;
        int ancho = Math.max(1, recorte.getWidth() * alto / recorte.getHeight());
        BufferedImage chica = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = chica.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(recorte.getScaledInstance(ancho, alto, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, null);
        g.dispose();
        return chica;
    }

    /** Caras de los costados izquierdo/derecho: solo la parte que no tapa el vecino. */
    private void lateral(int n, int c, double t, double tv, double x, double y0, double y1) {
        if (tv >= t) {
            return;
        }
        if (tv == 0) {
            cara(n, c, x, y0, -t, x, y1, -t, x, y1, t, x, y0, t);
        } else {
            cara(n, c, x, y0, -t, x, y1, -t, x, y1, -tv, x, y0, -tv);
            cara(n, c, x, y0, tv, x, y1, tv, x, y1, t, x, y0, t);
        }
    }

    /** Caras de arriba/abajo. */
    private void tapa(int n, int c, double t, double tv, double x0, double x1, double y) {
        if (tv >= t) {
            return;
        }
        if (tv == 0) {
            cara(n, c, x0, y, -t, x1, y, -t, x1, y, t, x0, y, t);
        } else {
            cara(n, c, x0, y, -t, x1, y, -t, x1, y, -tv, x0, y, -tv);
            cara(n, c, x0, y, tv, x1, y, tv, x1, y, t, x0, y, t);
        }
    }

    private void cara(int normal, int color, double... v) {
        if (caras * 12 + 12 > vertices.length) {
            vertices = java.util.Arrays.copyOf(vertices, vertices.length * 2);
        }
        if (caras >= colores.length) {
            colores = java.util.Arrays.copyOf(colores, colores.length * 2);
            normales = java.util.Arrays.copyOf(normales, normales.length * 2);
        }
        for (int i = 0; i < 12; i++) {
            vertices[caras * 12 + i] = (float) v[i];
        }
        normales[caras] = (byte) normal;
        colores[caras] = color;
        caras++;
    }

    /**
     * Dibuja el modelo.
     * @param escala      metros por píxel del sprite
     * @param estirar     >1 se estira hacia arriba, <1 se aplasta
     * @param giroZ       inclinación (radianes) alrededor del eje Z, con pivote en los pies
     * @param giroY       giro alrededor del eje vertical
     * @param tinte       color al que se mezcla (destello, selección)
     * @param fuerzaTinte 0 = sin tinte, 1 = todo del color del tinte
     * @param gris        true para dibujarlo sin color (derrotado)
     */
    void dibujar(Motor3D m, double escala, double estirar, double giroZ, double giroY,
                 double px, double py, double pz, int tinte, double fuerzaTinte, boolean gris) {
        double sxz = escala / Math.sqrt(estirar), sy = escala * estirar;
        double cz = Math.cos(giroZ), sz = Math.sin(giroZ), cyy = Math.cos(giroY), syy = Math.sin(giroY);
        double distancia = m.distancia(px, py, pz);
        // normales giradas (6 posibles)
        double[][] n = new double[6][3];
        for (int i = 0; i < 6; i++) {
            double ax = NORMALES[i][0] * cz - NORMALES[i][1] * sz;
            double ay = NORMALES[i][0] * sz + NORMALES[i][1] * cz;
            double az = NORMALES[i][2];
            n[i][0] = ax * cyy + az * syy;
            n[i][1] = ay;
            n[i][2] = -ax * syy + az * cyy;
        }
        for (int f = 0; f < caras; f++) {
            int b = f * 12;
            for (int k = 0; k < 4; k++) {
                double x = vertices[b + k * 3] * sxz, y = vertices[b + k * 3 + 1] * sy, z = vertices[b + k * 3 + 2] * sxz;
                double ax = x * cz - y * sz, ay = x * sz + y * cz;
                xs[k] = px + ax * cyy + z * syy;
                ys[k] = py + ay;
                zs[k] = pz - ax * syy + z * cyy;
            }
            double[] nn = n[normales[f]];
            if (!m.miraALaCamara(xs[0], ys[0], zs[0], nn[0], nn[1], nn[2])) {
                continue;
            }
            int base = colores[f];
            if (gris) {
                int g = (((base >> 16) & 255) + ((base >> 8) & 255) + (base & 255)) / 3;
                g = g * 2 / 3;
                base = (g << 16) | (g << 8) | g;
            }
            int color = m.iluminar(base, nn[0], nn[1], nn[2], distancia);
            if (fuerzaTinte > 0) {
                color = Motor3D.mezclar(color, tinte, fuerzaTinte);
            }
            m.poligono(xs, ys, zs, 4, color, Motor3D.NORMAL);
        }
    }
}
