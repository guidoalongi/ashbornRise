package ashbornrise.vista.arena;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.enemigos.SenorDeLasSombras;
import ashbornrise.modelo.personajes.Personaje;
import ashbornrise.vista.Sprites;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Arena 3D de la batalla. Dibuja el coliseo y a los combatientes como modelos de vóxeles
 * hechos a partir de sus sprites, y reproduce las animaciones de cada turno.
 * Solo LEE el modelo: lo que cambia el juego sigue pasando por el controlador.
 */
public class PanelArena extends JPanel {

    /** Cada píxel del render 3D ocupa PIXEL x PIXEL píxeles de pantalla (estética retro). */
    private static final int PIXEL = 2;
    private static final double YAW = -0.14;
    private static final double PITCH = 0.36;
    private static final double DISTANCIA = 10.2;

    private static final Map<String, ModeloVoxel> MODELOS = new HashMap<>();

    private final Motor3D motor = new Motor3D();
    private final Escenario escenario = new Escenario();
    private final Coreografia coreografia = new Coreografia(this);
    final List<Actor3D> actores = new ArrayList<>();
    final List<Particula> particulas = new ArrayList<>();
    private final List<TextoFlotante> textos = new ArrayList<>();
    final Random azar = new Random();

    private Linea linea;
    private Runnable alTerminarLinea;
    private final Timer reloj;
    private long ultimoNano;
    private double tiempo;

    // cámara: posición actual y la deseada
    private double objX, objY = 0.9, objZ = 0.2, dist = DISTANCIA;
    private double focoX, focoY = 0.9, focoZ = 0.2, focoDist = DISTANCIA;
    private double temblor;
    private double emocion;
    private Color colorFlash = Color.WHITE;
    private double flash;
    private String cartel;
    private double cartelTiempo;

    private Combatiente actual;
    private Set<Combatiente> seleccionables = new HashSet<>();
    private Actor3D bajoMouse;
    private Consumer<Combatiente> alHacerClic;
    private final double[] proy = new double[3];
    private BufferedImage vineta;

    public PanelArena() {
        setBackground(Color.BLACK);
        setOpaque(true);
        reloj = new Timer(15, e -> paso());
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Actor3D a = buscar(e.getX(), e.getY());
                bajoMouse = a != null && seleccionables.contains(a.combatiente) ? a : null;
                setCursor(bajoMouse != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Actor3D a = buscar(e.getX(), e.getY());
                if (a != null && alHacerClic != null && seleccionables.contains(a.combatiente)) {
                    alHacerClic.accept(a.combatiente);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        ultimoNano = System.nanoTime();
        reloj.start();
    }

    // ---------------------------------------------------------------- API para la vista

    /** Arma la escena con los combatientes de la batalla. */
    public void prepararBatalla(Batalla batalla) {
        actores.clear();
        particulas.clear();
        textos.clear();
        linea = null;
        alTerminarLinea = null;
        actual = null;
        seleccionables.clear();
        List<Personaje> aliados = batalla.getAliados();
        for (int i = 0; i < aliados.size(); i++) {
            Actor3D a = crearActor(aliados.get(i));
            a.baseX = -2.6 - 0.6 * i;
            a.baseZ = (aliados.size() - 1) * 1.45 / 2 - 1.45 * i + 0.3;
            actores.add(a);
        }
        List<Enemigo> enemigos = batalla.getEnemigos();
        for (int i = 0; i < enemigos.size(); i++) {
            Actor3D a = crearActor(enemigos.get(i));
            a.baseX = 2.7 + 0.6 * i;
            a.baseZ = (enemigos.size() - 1) * 1.8 / 2 - 1.8 * i + 0.4;
            actores.add(a);
        }
        for (Actor3D a : actores) {
            a.caida = a.vivo ? 0 : 1;
        }
        objX = focoX = 0;
        objZ = focoZ = 0.2;
        focoDist = DISTANCIA;
        dist = DISTANCIA + 7;
        objY = 2.5;
        emocion = 0.3;
    }

    public void animarEntrada(int numeroBatalla, Runnable alTerminar) {
        iniciarLinea(coreografia.entrada(nuevaLinea(), numeroBatalla), alTerminar);
    }

    /** Anima lo que pasó en el último turno ejecutado. */
    public void animarTurno(Batalla batalla, List<String> lineasNuevas, Runnable alTerminar) {
        Linea l = nuevaLinea();
        double fin = coreografia.turno(l, batalla.getUltimoActor(), batalla.getUltimaAccion(), lineasNuevas);
        l.en(fin, () -> coreografia.residuales(l, lineasNuevas, fin));
        iniciarLinea(l, alTerminar);
    }

    public void animarFinal(boolean victoria) {
        iniciarLinea(victoria ? coreografia.victoria(nuevaLinea()) : coreografia.derrota(nuevaLinea()), null);
    }

    public boolean estaAnimando() {
        return linea != null;
    }

    public void setActual(Combatiente c) {
        actual = c;
    }

    public void setSeleccionables(Collection<? extends Combatiente> cs) {
        seleccionables = new HashSet<>(cs);
        if (bajoMouse != null && !seleccionables.contains(bajoMouse.combatiente)) {
            bajoMouse = null;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public void setAlHacerClic(Consumer<Combatiente> alHacerClic) {
        this.alHacerClic = alHacerClic;
    }

    // ---------------------------------------------------------------- ayudas para Coreografia

    Actor3D actor(Combatiente c) {
        for (Actor3D a : actores) {
            if (a.combatiente == c) {
                return a;
            }
        }
        return null;
    }

    Actor3D actorPorNombre(String linea) {
        Actor3D mejor = null;
        for (Actor3D a : actores) {
            String n = a.combatiente.getNombre();
            if (linea.startsWith(n) && (mejor == null || n.length() > mejor.combatiente.getNombre().length())) {
                mejor = a;
            }
        }
        return mejor;
    }

    void enfocar(double x, double z, double distancia) {
        focoX = x * 0.7;
        focoZ = 0.2 + (z - 0.2) * 0.6;
        focoDist = Math.max(8.0, distancia + 1.4);
    }

    void soltarCamara() {
        focoX = 0;
        focoZ = 0.2;
        focoDist = DISTANCIA;
    }

    void sacudir(double fuerza) {
        temblor = Math.max(temblor, fuerza);
    }

    void ovacion(double fuerza) {
        emocion = Math.max(emocion, fuerza);
    }

    void destelloPantalla(Color c, double fuerza) {
        colorFlash = c;
        flash = Math.max(flash, fuerza);
    }

    void cartel(String texto) {
        cartel = texto;
        cartelTiempo = 0;
    }

    void particula(Particula p) {
        particulas.add(p);
    }

    void texto(Actor3D a, String texto, Color color, int tamanio) {
        textos.add(new TextoFlotante(a.x(), a.alto() + 0.35, a.z() - 0.2, texto, color, tamanio, 1.3));
    }

    /**
     * Momento del impacto: muestra lo que le pasó al combatiente según el modelo
     * (daño o curación, estados nuevos, derrota) y lo deja sincronizado.
     */
    void impacto(Actor3D a, boolean golpe) {
        impacto(a, golpe, new Color(255, 236, 200));
    }

    void impacto(Actor3D a, boolean golpe, Color colorDanio) {
        int diferencia = a.vidaPendiente();
        boolean estabaVivo = a.vivo;
        boolean veneno = a.veneno, escudo = a.escudo, aturdido = a.aturdido;
        a.sincronizar();
        if (diferencia < 0) {
            texto(a, String.valueOf(-diferencia), colorDanio, 30);
            a.destello = 1;
            a.empuje = 0.35;
        } else if (diferencia > 0) {
            texto(a, "+" + diferencia, new Color(120, 255, 140), 28);
        } else if (golpe) {
            texto(a, escudo ? "Absorbido" : "0", new Color(170, 210, 255), 22);
            a.destello = 0.5;
        }
        if (!veneno && a.veneno) {
            textoRetrasado(a, "¡Envenenado!", new Color(140, 255, 90));
        } else if (veneno && !a.veneno && a.vivo) {
            textoRetrasado(a, "Purificado", new Color(240, 240, 255));
        }
        if (!escudo && a.escudo) {
            textoRetrasado(a, "¡Escudo!", new Color(130, 220, 255));
        }
        if (!aturdido && a.aturdido) {
            textoRetrasado(a, "¡Aturdido!", new Color(255, 230, 90));
        } else if (aturdido && !a.aturdido && a.vivo) {
            textoRetrasado(a, "Despierta", new Color(240, 240, 255));
        }
        if (estabaVivo && !a.vivo) {
            coreografia.derrumbe(a);
            ovacion(1);
        }
    }

    private void textoRetrasado(Actor3D a, String texto, Color color) {
        TextoFlotante t = new TextoFlotante(a.x(), a.alto() + 0.1, a.z() - 0.2, texto, color, 20, 1.6);
        t.edad = -0.25;
        textos.add(t);
    }

    // ---------------------------------------------------------------- privado

    private Actor3D crearActor(Combatiente c) {
        boolean enemigo = c instanceof Enemigo;
        String clave = c.getClass().getName() + enemigo;
        ModeloVoxel m = MODELOS.get(clave);
        if (m == null) {
            m = ModeloVoxel.desdeImagen(Sprites.imagenBase(c), enemigo);
            MODELOS.put(clave, m);
        }
        double escala = c instanceof SenorDeLasSombras ? 0.088 : 0.068;
        return new Actor3D(c, m, escala, enemigo ? -1 : 1);
    }

    private Linea nuevaLinea() {
        return new Linea();
    }

    private void iniciarLinea(Linea l, Runnable alTerminar) {
        linea = l;
        alTerminarLinea = alTerminar;
    }

    /** Un paso de la animación (unas 60 veces por segundo). */
    private void paso() {
        long ahora = System.nanoTime();
        double dt = Math.min(0.05, (ahora - ultimoNano) / 1e9);
        ultimoNano = ahora;
        if (!isShowing()) {
            return;
        }
        tiempo += dt;

        for (Iterator<Particula> it = particulas.iterator(); it.hasNext(); ) {
            if (!it.next().actualizar(dt)) {
                it.remove();
            }
        }
        textos.removeIf(t -> !t.actualizar(dt));

        if (linea != null && linea.avanzar(dt)) {
            Runnable r = alTerminarLinea;
            linea = null;
            alTerminarLinea = null;
            for (Actor3D a : actores) { // por si quedó algo sin mostrar
                if (a.vidaPendiente() != 0 || a.estadosPendientes()) {
                    impacto(a, false);
                }
            }
            if (r != null) {
                r.run();
            }
        }

        for (Actor3D a : actores) {
            a.vidaMostrada += (a.vidaSincronizada - a.vidaMostrada) * Math.min(1, dt * 6);
            a.destello = Math.max(0, a.destello - dt * 4);
            a.empuje *= Math.exp(-dt * 7);
            double objetivo = a.vivo ? 0 : 1;
            a.caida += (objetivo - a.caida) * Math.min(1, dt * 5);
            efectosDeEstado(a, dt);
        }
        for (double[] t : escenario.antorchas) {
            fuego(t[0], t[1], t[2], dt);
        }

        double k = 1 - Math.exp(-dt * 3.2);
        objX += (focoX - objX) * k;
        objY += (focoY - objY) * k;
        objZ += (focoZ - objZ) * k;
        dist += (focoDist - dist) * k;
        temblor *= Math.exp(-dt * 6);
        emocion = Math.max(0.15, emocion * Math.exp(-dt * 0.7));
        flash = Math.max(0, flash - dt * 2.5);
        cartelTiempo += dt;
        motor.intensidad = 0.97 + 0.03 * Math.sin(tiempo * 13) * Math.sin(tiempo * 7.3);
        repaint();
    }

    private void fuego(double x, double y, double z, double dt) {
        int cantidad = azar.nextDouble() < dt * 70 ? 2 : 1;
        for (int i = 0; i < cantidad; i++) {
            Particula p = new Particula(x + (azar.nextDouble() - 0.5) * 0.4, y, z + (azar.nextDouble() - 0.5) * 0.4,
                    (azar.nextDouble() - 0.5) * 0.3, 0.9 + azar.nextDouble() * 0.8, (azar.nextDouble() - 0.5) * 0.3,
                    0.16 + azar.nextDouble() * 0.1, 0.45 + azar.nextDouble() * 0.35, 0xffd060, 0xc02010, true);
            particulas.add(p);
        }
    }

    private void efectosDeEstado(Actor3D a, double dt) {
        if (!a.vivo) {
            return;
        }
        if (a.veneno && azar.nextDouble() < dt * 7) {
            Particula p = new Particula(a.x() + (azar.nextDouble() - 0.5) * 0.6, 0.2 + azar.nextDouble() * a.alto() * 0.7,
                    a.z() + (azar.nextDouble() - 0.5) * 0.4, 0, 0.5 + azar.nextDouble() * 0.4, 0,
                    0.1, 1.0, 0x80ff40, 0x205010, true);
            particulas.add(p);
        }
    }

    private Actor3D buscar(int mx, int my) {
        double x = mx / (double) PIXEL, y = my / (double) PIXEL;
        Actor3D mejor = null;
        double mejorDist = Double.MAX_VALUE;
        for (Actor3D a : actores) {
            if (!a.vivo || motor.imagen == null || !motor.proyectar(a.x(), a.y(), a.z(), proy)) {
                continue;
            }
            double sx = proy[0], pie = proy[1], d = proy[2];
            double alto = a.alto() * motor.focal / d;
            double ancho = Math.max(a.ancho() * 0.7, 0.8) * motor.focal / d;
            if (x >= sx - ancho / 2 && x <= sx + ancho / 2 && y >= pie - alto && y <= pie + 4 && d < mejorDist) {
                mejor = a;
                mejorDist = d;
            }
        }
        return mejor;
    }

    // ---------------------------------------------------------------- dibujo

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        int w = getWidth(), h = getHeight();
        motor.redimensionar(Math.max(16, w / PIXEL), Math.max(16, h / PIXEL));

        double tx = objX + (azar.nextDouble() - 0.5) * temblor * 0.25;
        double ty = objY + (azar.nextDouble() - 0.5) * temblor * 0.25;
        double tz = objZ;
        double yaw = YAW + Math.sin(tiempo * 0.23) * 0.1;
        double cx = tx + dist * Math.sin(yaw) * Math.cos(PITCH);
        double cy = ty + dist * Math.sin(PITCH);
        double cz = tz - dist * Math.cos(yaw) * Math.cos(PITCH);
        motor.mirar(cx, cy, cz, tx, ty, tz, 64, 34);

        motor.limpiar(escenario.cielo(motor.ancho, motor.alto));
        escenario.dibujar(motor, tiempo, emocion);
        for (Actor3D a : actores) {
            sombra(a);
        }
        for (Actor3D a : actores) {
            anillo(a);
        }
        for (Actor3D a : actores) {
            dibujarActor(a);
        }
        for (Actor3D a : actores) {
            adornos(a);
        }
        for (Particula p : particulas) {
            p.dibujar(motor);
        }

        g.drawImage(motor.imagen, 0, 0, motor.ancho * PIXEL, motor.alto * PIXEL, null);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        vineta(g, w, h);
        for (Actor3D a : actores) {
            barra(g, a);
        }
        flechaTurno(g);
        for (TextoFlotante t : textos) {
            dibujarTexto(g, t);
        }
        dibujarCartel(g, w);
        if (flash > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.min(0.7, flash)));
            g.setColor(colorFlash);
            g.fillRect(0, 0, w, h);
            g.setComposite(AlphaComposite.SrcOver);
        }
    }

    private void dibujarActor(Actor3D a) {
        double respiro = a.vivo ? Math.sin(tiempo * 2.4 + a.fase) * 0.025 : 0;
        double estirar = a.estirar * (1 + respiro);
        double angCaida = a.caida * Math.PI / 2;
        double giroZ = a.frente * angCaida - a.frente * a.inclinacion;
        double elevar = Math.sin(angCaida) * a.ancho() * 0.32;
        int tinte = 0xffffff;
        double fuerza = a.destello * 0.85;
        if (a == bajoMouse) {
            tinte = 0xffe080;
            fuerza = Math.max(fuerza, 0.25 + 0.1 * Math.sin(tiempo * 8));
        } else if (a.aturdido && a.vivo) {
            tinte = 0xfff0a0;
            fuerza = Math.max(fuerza, 0.12);
        }
        a.modelo.dibujar(motor, a.escala, estirar, giroZ, 0, a.x(), a.y() + elevar, a.z(), tinte, fuerza, !a.vivo);
    }

    private final double[] px = new double[16], py = new double[16], pz = new double[16];

    private void sombra(Actor3D a) {
        if (a.y() < -0.05) {
            return;
        }
        double r = Math.max(0.35, a.ancho() * 0.36) * (1 + a.caida * 0.4) / (1 + Math.max(0, a.y()) * 0.6);
        int n = 12;
        for (int i = 0; i < n; i++) {
            double ang = 2 * Math.PI * i / n;
            px[i] = a.x() + Math.cos(ang) * r * (1 + a.caida * 0.6);
            py[i] = 0.012;
            pz[i] = a.z() + Math.sin(ang) * r * 0.6;
        }
        motor.poligono(px, py, pz, n, 0, Motor3D.SOMBRA);
    }

    /** Aro en el piso: dorado para quien tiene el turno, titilante para los que se pueden elegir. */
    private void anillo(Actor3D a) {
        if (!a.vivo) {
            return;
        }
        int color;
        double r;
        if (a == bajoMouse) {
            color = 0xffd860;
            r = 0.72;
        } else if (seleccionables.contains(a.combatiente)) {
            double p = 0.55 + 0.45 * Math.sin(tiempo * 6);
            color = Motor3D.escalarColor(0xd0b060, p);
            r = 0.66;
        } else if (a.combatiente == actual) {
            color = Motor3D.escalarColor(0xe0b050, 0.8 + 0.2 * Math.sin(tiempo * 3));
            r = 0.66;
        } else {
            return;
        }
        int n = 20;
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n, a1 = 2 * Math.PI * (i + 1) / n;
            double r0 = r - 0.09;
            px[0] = a.baseX + Math.cos(a0) * r0;
            pz[0] = a.baseZ + Math.sin(a0) * r0 * 0.75;
            px[1] = a.baseX + Math.cos(a1) * r0;
            pz[1] = a.baseZ + Math.sin(a1) * r0 * 0.75;
            px[2] = a.baseX + Math.cos(a1) * r;
            pz[2] = a.baseZ + Math.sin(a1) * r * 0.75;
            px[3] = a.baseX + Math.cos(a0) * r;
            pz[3] = a.baseZ + Math.sin(a0) * r * 0.75;
            py[0] = py[1] = py[2] = py[3] = 0.02;
            motor.poligono(px, py, pz, 4, color, Motor3D.BRILLO);
        }
    }

    /** Escudo, aturdimiento y guardia se dibujan alrededor del combatiente. */
    private void adornos(Actor3D a) {
        if (!a.vivo) {
            return;
        }
        double cx = a.x(), cz = a.z(), alto = a.alto();
        if (a.escudo) {
            int n = 26;
            for (int i = 0; i < n; i++) {
                double ang = tiempo * 1.3 + i * 2 * Math.PI / n;
                double alt = (i % 3) / 2.0;
                double rr = 0.75 * Math.sqrt(1 - (alt - 0.5) * (alt - 0.5) * 2.5);
                motor.punto(cx + Math.cos(ang) * rr, 0.15 + alt * alto * 0.9, cz + Math.sin(ang) * rr * 0.7,
                        0.07, 0x3070a0, true);
            }
        }
        if (a.guardia) {
            int n = 9;
            for (int i = 0; i < n; i++) {
                double ang = -0.9 + 1.8 * i / (n - 1);
                motor.punto(cx + a.frente * 0.55 * Math.cos(ang), alto * 0.5 + Math.sin(ang) * 0.5, cz - 0.1,
                        0.08, 0x406090, true);
            }
        }
        if (a.aturdido) {
            for (int i = 0; i < 3; i++) {
                double ang = tiempo * 4 + i * 2.094;
                motor.punto(cx + Math.cos(ang) * 0.35, alto + 0.15, cz + Math.sin(ang) * 0.25, 0.11, 0xffe040, true);
            }
        }
    }

    private void barra(Graphics2D g, Actor3D a) {
        if ((!a.vivo && a.caida > 0.9) || a.y() < -0.3) {
            return;
        }
        if (!motor.proyectar(a.x(), a.alto() + 0.18 + a.y(), a.z(), proy)) {
            return;
        }
        int x = (int) (proy[0] * PIXEL), y = (int) (proy[1] * PIXEL);
        int ancho = a.combatiente instanceof SenorDeLasSombras ? 120 : 76;
        int alto = 7;
        double frac = Math.max(0, a.vidaMostrada / a.combatiente.getVidaMaximo());
        g.setFont(new Font(Font.SERIF, Font.BOLD, 12));
        String nombre = a.combatiente.getNombre();
        FontMetrics fm = g.getFontMetrics();
        int tx = x - fm.stringWidth(nombre) / 2;
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(nombre, tx + 1, y - 5);
        g.setColor(a.combatiente == actual ? new Color(255, 220, 120) : new Color(236, 228, 210));
        g.drawString(nombre, tx, y - 6);
        g.setColor(new Color(10, 6, 14, 200));
        g.fillRect(x - ancho / 2 - 1, y - 1, ancho + 2, alto + 2);
        Color c = frac > 0.5 ? new Color(90, 200, 80) : frac > 0.25 ? new Color(230, 180, 50) : new Color(220, 60, 50);
        g.setColor(c);
        g.fillRect(x - ancho / 2, y, (int) (ancho * frac), alto);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(x - ancho / 2, y, (int) (ancho * frac), 2);
    }

    private void flechaTurno(Graphics2D g) {
        Actor3D a = actor(actual);
        if (a == null || !a.vivo || linea != null) {
            return;
        }
        if (!motor.proyectar(a.x(), a.alto() + 0.75 + Math.abs(Math.sin(tiempo * 4)) * 0.15, a.z(), proy)) {
            return;
        }
        int x = (int) (proy[0] * PIXEL), y = (int) (proy[1] * PIXEL);
        Polygon p = new Polygon(new int[]{x - 9, x + 9, x}, new int[]{y - 10, y - 10, y + 2}, 3);
        g.setColor(new Color(255, 214, 90));
        g.fillPolygon(p);
        g.setColor(new Color(90, 50, 10));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(p);
    }

    private void dibujarTexto(Graphics2D g, TextoFlotante t) {
        if (t.edad < 0 || !motor.proyectar(t.x, t.y, t.z, proy)) {
            return;
        }
        double vida = t.edad / t.duracion;
        float alfa = (float) Math.max(0, Math.min(1, vida < 0.7 ? 1 : (1 - vida) / 0.3));
        double pop = t.edad < 0.12 ? 1.5 - t.edad / 0.12 * 0.5 : 1;
        Font f = new Font(Font.SERIF, Font.BOLD, (int) (t.tamanio * pop));
        GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), t.texto);
        int ancho = (int) gv.getVisualBounds().getWidth();
        int x = (int) (proy[0] * PIXEL) - ancho / 2, y = (int) (proy[1] * PIXEL);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
        java.awt.Shape forma = gv.getOutline(x, y);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(20, 8, 16));
        g.draw(forma);
        g.setColor(t.color);
        g.fill(forma);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void dibujarCartel(Graphics2D g, int w) {
        if (cartel == null || cartelTiempo > 2.2) {
            return;
        }
        float alfa = (float) Math.min(1, Math.min(cartelTiempo / 0.15, (2.2 - cartelTiempo) / 0.4));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alfa)));
        g.setFont(new Font(Font.SERIF, Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int ancho = fm.stringWidth(cartel) + 50;
        int x = (w - ancho) / 2, y = 14;
        g.setColor(new Color(16, 10, 24, 210));
        g.fillRoundRect(x, y, ancho, 38, 12, 12);
        g.setColor(new Color(230, 192, 101));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, ancho, 38, 12, 12);
        g.drawString(cartel, x + 25, y + 26);
        g.setComposite(AlphaComposite.SrcOver);
    }

    /** Oscurece los bordes (se calcula una sola vez por tamaño de panel). */
    private void vineta(Graphics2D g, int w, int h) {
        if (vineta == null || vineta.getWidth() != w || vineta.getHeight() != h) {
            vineta = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gv = vineta.createGraphics();
            float r = (float) Math.hypot(w, h) / 1.6f;
            gv.setPaint(new RadialGradientPaint(w / 2f, h / 2f, r, new float[]{0.55f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)}));
            gv.fillRect(0, 0, w, h);
            gv.dispose();
        }
        g.drawImage(vineta, 0, 0, null);
    }
}
