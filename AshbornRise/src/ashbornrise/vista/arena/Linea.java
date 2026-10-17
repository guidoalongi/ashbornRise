package ashbornrise.vista.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

/**
 * Línea de tiempo de una animación: cosas que pasan en un instante (en) y cosas
 * que duran un rato y reciben el progreso de 0 a 1 (durante).
 */
class Linea {

    private static class Evento {
        double inicio;
        double duracion;
        Runnable instante;
        DoubleConsumer progreso;
        boolean terminado;
    }

    private final List<Evento> eventos = new ArrayList<>();
    private double tiempo;

    void en(double inicio, Runnable r) {
        Evento e = new Evento();
        e.inicio = inicio;
        e.instante = r;
        eventos.add(e);
    }

    void durante(double inicio, double duracion, DoubleConsumer f) {
        Evento e = new Evento();
        e.inicio = inicio;
        e.duracion = duracion;
        e.progreso = f;
        eventos.add(e);
    }

    double fin() {
        double fin = 0;
        for (Evento e : eventos) {
            fin = Math.max(fin, e.inicio + e.duracion);
        }
        return fin;
    }

    /** Avanza el reloj. Devuelve true cuando ya pasó todo. */
    boolean avanzar(double dt) {
        tiempo += dt;
        boolean quedan = false;
        for (int i = 0; i < eventos.size(); i++) { // con índice: un evento puede agregar otros
            Evento e = eventos.get(i);
            if (e.terminado) {
                continue;
            }
            if (tiempo < e.inicio) {
                quedan = true;
                continue;
            }
            if (e.instante != null) {
                e.terminado = true;
                e.instante.run();
            } else {
                double p = e.duracion <= 0 ? 1 : Math.min(1, (tiempo - e.inicio) / e.duracion);
                e.progreso.accept(p);
                if (p >= 1) {
                    e.terminado = true;
                } else {
                    quedan = true;
                }
            }
        }
        return !quedan;
    }

    /** Suavizado: arranca y frena despacio. */
    static double suave(double t) {
        return t * t * (3 - 2 * t);
    }
}
