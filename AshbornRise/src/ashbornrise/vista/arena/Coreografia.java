package ashbornrise.vista.arena;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionAtacar;
import ashbornrise.modelo.acciones.AccionDefender;
import ashbornrise.modelo.acciones.AccionHabilidad;
import ashbornrise.modelo.acciones.AccionItem;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.habilidades.HabilidadCuracion;
import ashbornrise.modelo.habilidades.HabilidadEstado;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Arma la animación de cada turno según quién actuó y qué hizo: ataques cuerpo a cuerpo
 * o a distancia, cada habilidad con su efecto propio, ítems, defensa, veneno, derrotas,
 * la entrada a la arena y el final de la batalla.
 */
class Coreografia {

    private static final int FLECHA = 0, FLECHA_VENENO = 1, ARCANO = 2, FUEGO = 3, LUZ = 4, VERDE = 5, POCION = 6;
    private static final double PI = Math.PI;

    private final PanelArena p;
    private final Random azar = new Random();

    Coreografia(PanelArena panel) {
        this.p = panel;
    }

    // ---------------------------------------------------------------- turno

    /** Agrega a la línea la animación del turno y devuelve cuándo termina. */
    double turno(Linea l, Combatiente actorC, Accion accion, List<String> lineas) {
        Actor3D a = p.actor(actorC);
        if (a == null || accion == null || lineas.isEmpty()) {
            return 0;
        }
        double t = 0;
        if (contiene(lineas, "segunda fase")) {
            t = segundaFase(l, a);
        }
        String nombre = actorC.getNombre();
        if (accion instanceof AccionAtacar) {
            Actor3D o = p.actor(((AccionAtacar) accion).getObjetivo());
            if (o == null) {
                return t;
            }
            p.cartel(nombre + " ataca a " + o.combatiente.getNombre());
            int tipo = tipoDistancia(a);
            if (tipo >= 0) {
                List<Actor3D> objetivos = new ArrayList<>();
                objetivos.add(o);
                t = disparo(l, t, a, objetivos, tipo, 0, 0.35);
            } else {
                t = cuerpoACuerpo(l, t, a, o, clase(a).equals("AranaVenenosa") ? 0.9 : 0.35, () -> efectoGolpe(a, o, lineas));
            }
            if (contiene(lineas, "contraataca") && o.combatiente.estaVivo()) {
                t = contraataque(l, t, o, a);
            }
        } else if (accion instanceof AccionHabilidad) {
            AccionHabilidad ah = (AccionHabilidad) accion;
            p.cartel(nombre + " usa " + ah.getHabilidad().getNombre());
            t = habilidad(l, t, a, ah.getHabilidad(), actores(ah.getObjetivos()));
        } else if (accion instanceof AccionDefender) {
            p.cartel(nombre + " se pone en guardia");
            t = defensa(l, t, a);
        } else if (accion instanceof AccionItem) {
            AccionItem ai = (AccionItem) accion;
            p.cartel(nombre + " usa " + ai.getItem().getNombre());
            t = item(l, t, a, p.actor(ai.getObjetivo()), ai.getItem().getNombre());
        }
        return t;
    }

    /** Lo que pasa al comenzar el turno siguiente: daño por veneno, aturdimiento, etc. */
    void residuales(Linea l, List<String> lineas, double t0) {
        double t = t0 + 0.15;
        boolean hubo = false;
        for (Actor3D a : p.actores) {
            int d = a.vidaPendiente();
            if (d < 0 && (a.veneno || a.combatiente.tieneEstado(TipoEstado.VENENO) || contiene(lineas, a.combatiente.getNombre() + " sufre"))) {
                double ti = t;
                l.en(ti, () -> {
                    nubeVeneno(a.x(), a.alto() * 0.5, a.z(), 0.8);
                    p.impacto(a, false, new Color(150, 255, 90));
                });
                t += 0.25;
                hubo = true;
            }
        }
        for (String linea : lineas) {
            if (linea.contains("aturdido y pierde el turno")) {
                Actor3D a = p.actorPorNombre(linea);
                if (a != null) {
                    double ti = t;
                    l.en(ti, () -> {
                        chispas(a.x(), a.alto() + 0.1, a.z(), 0xffe040, 14, 2.0, 0.1, 0.6, 0, true);
                        p.texto(a, "Pierde el turno", new Color(255, 230, 90), 20);
                    });
                    t += 0.3;
                    hubo = true;
                }
            }
        }
        if (hubo) {
            l.durante(t, 0.75, k -> {
            });
        }
    }

    // ---------------------------------------------------------------- ataques básicos

    private double cuerpoACuerpo(Linea l, double t0, Actor3D a, Actor3D o, double salto, Runnable efecto) {
        double destX = o.baseX + o.frente * (0.5 + o.ancho() * 0.42);
        double destZ = o.baseZ - 0.08;
        double dx = destX - a.baseX, dz = destZ - a.baseZ;
        l.en(t0, () -> {
            p.enfocar((a.x() + o.x()) / 2, (a.z() + o.z()) / 2, 7.0);
            polvo(a.x(), a.z(), 6);
        });
        l.durante(t0, 0.16, k -> {
            a.estirar = 1 - 0.16 * Math.sin(k * PI);
            a.inclinacion = -0.18 * Math.sin(k * PI);
        });
        double ida = 0.32;
        l.durante(t0 + 0.16, ida, k -> {
            double s = Linea.suave(k);
            a.ox = dx * s;
            a.oz = dz * s;
            a.oy = Math.sin(k * PI) * salto;
        });
        double tg = t0 + 0.16 + ida;
        l.durante(tg, 0.1, k -> a.inclinacion = 0.6 * k);
        l.en(tg + 0.08, () -> {
            p.impacto(o, true);
            efecto.run();
        });
        l.durante(tg + 0.1, 0.2, k -> a.inclinacion = 0.6 * (1 - k));
        double tv = tg + 0.38;
        l.durante(tv, 0.32, k -> {
            double s = 1 - Linea.suave(k);
            a.ox = dx * s;
            a.oz = dz * s;
            a.oy = Math.sin(k * PI) * 0.25;
        });
        l.en(tv + 0.32, () -> {
            a.ox = 0;
            a.oz = 0;
            a.oy = 0;
            a.inclinacion = 0;
            a.estirar = 1;
            p.soltarCamara();
        });
        return tv + 0.4;
    }

    private void efectoGolpe(Actor3D a, Actor3D o, List<String> lineas) {
        double x = o.x(), y = o.alto() * 0.55, z = o.z() - 0.15;
        switch (clase(a)) {
            case "Guerrero":
                tajo(x, y, z, a.frente, 0xe8f0ff, 0.7);
                chispas(x, y, z, 0xffe080, 12, 3.2, 0.07, 0.35, 6, true);
                p.sacudir(0.5);
                break;
            case "GoblinMerodeador":
                tajo(x, y - 0.1, z, a.frente, 0xd0d8e0, 0.45);
                chispas(x, y, z, 0xffffff, 8, 2.5, 0.06, 0.3, 6, true);
                if (contiene(lineas, "roba")) {
                    monedas(o, a);
                }
                p.sacudir(0.35);
                break;
            case "EsqueletoGuerrero":
                tajo(x, y, z, a.frente, 0xe8e4d4, 0.6);
                chispas(x, y, z, 0xffe0a0, 10, 3, 0.06, 0.35, 6, true);
                p.sacudir(0.45);
                break;
            case "AranaVenenosa":
                nubeVeneno(x, y, z, 0.6);
                chispas(x, y, z, 0x90ff50, 10, 2.5, 0.07, 0.4, 5, true);
                p.sacudir(0.35);
                break;
            case "SenorDeLasSombras":
                tajo(x, y, z, a.frente, 0xc070ff, 1.0);
                chispas(x, y, z, 0xb050ff, 30, 4.5, 0.1, 0.6, 2, true);
                chispas(x, y, z, 0x401050, 16, 2, 0.14, 0.8, -0.5, false);
                p.sacudir(1.0);
                p.destelloPantalla(new Color(90, 0, 120), 0.3);
                p.ovacion(0.8);
                break;
            default:
                chispas(x, y, z, 0xffffff, 10, 3, 0.07, 0.3, 6, true);
                p.sacudir(0.4);
        }
    }

    private double contraataque(Linea l, double t0, Actor3D g, Actor3D e) {
        l.en(t0, () -> p.texto(g, "¡Contraataque!", new Color(255, 200, 90), 20));
        return cuerpoACuerpo(l, t0 + 0.15, g, e, 0.2, () -> {
            tajo(e.x(), e.alto() * 0.55, e.z() - 0.15, g.frente, 0xfff0c0, 0.7);
            chispas(e.x(), e.alto() * 0.55, e.z() - 0.15, 0xffd060, 12, 3, 0.07, 0.35, 6, true);
            p.sacudir(0.5);
        });
    }

    /**
     * Proyectiles hacia uno o varios objetivos (flechas, magia...).
     * @param separacion tiempo entre un disparo y el siguiente
     * @param arco       altura de la parábola
     */
    private double disparo(Linea l, double t0, Actor3D a, List<Actor3D> objetivos, int tipo, double separacion, double arco) {
        if (objetivos.isEmpty()) {
            return t0;
        }
        double mx = 0, mz = 0;
        for (Actor3D o : objetivos) {
            mx += o.x();
            mz += o.z();
        }
        double fmx = mx / objetivos.size(), fmz = mz / objetivos.size();
        l.en(t0, () -> p.enfocar((a.x() + fmx) / 2, (a.z() + fmz) / 2, 7.8));
        l.durante(t0, 0.3, k -> {
            a.inclinacion = -0.22 * Math.sin(k * PI);
            a.estirar = 1 + 0.07 * Math.sin(k * PI);
        });
        if (tipo == ARCANO || tipo == LUZ || tipo == VERDE) {
            int color = tipo == ARCANO ? 0xff50e0 : tipo == LUZ ? 0xfff0a0 : 0x60ff80;
            l.durante(t0, 0.3, k -> {
                double[] m = mano(a);
                converger(m[0], m[1], m[2], color, 2, 0.6);
            });
        }
        double fin = t0;
        for (int i = 0; i < objetivos.size(); i++) {
            Actor3D o = objetivos.get(i);
            double salida = t0 + 0.3 + i * separacion;
            double[] m = mano(a);
            double distancia = Math.abs(o.x() - m[0]) + Math.abs(o.z() - m[2]);
            double vuelo = Math.max(0.25, distancia / (tipo == FLECHA || tipo == FLECHA_VENENO ? 15.0 : 9.0));
            l.durante(salida, vuelo, k -> {
                double[] mm = mano(a);
                proyectil(tipo, mm[0], mm[1], mm[2], o.x(), o.alto() * 0.55, o.z() - 0.1, k, arco, 0);
            });
            l.en(salida + vuelo, () -> {
                p.impacto(o, true);
                impactoTipo(tipo, o);
            });
            fin = salida + vuelo;
        }
        l.en(fin + 0.45, p::soltarCamara);
        return fin + 0.55;
    }

    private void impactoTipo(int tipo, Actor3D o) {
        double x = o.x(), y = o.alto() * 0.55, z = o.z() - 0.15;
        switch (tipo) {
            case FLECHA:
                chispas(x, y, z, 0xfff0d0, 8, 2.5, 0.06, 0.3, 6, true);
                p.sacudir(0.25);
                break;
            case FLECHA_VENENO:
                nubeVeneno(x, y, z, 1.0);
                p.sacudir(0.25);
                break;
            case ARCANO:
                chispas(x, y, z, 0xff50e0, 18, 3, 0.09, 0.45, 0, true);
                p.sacudir(0.35);
                break;
            case FUEGO:
                explosionFuego(x, y, z, 1.0);
                break;
            case LUZ:
                chispas(x, y, z, 0xfff0a0, 16, 2.8, 0.08, 0.45, 0, true);
                p.sacudir(0.25);
                break;
            default:
                chispas(x, y, z, 0x60ff80, 16, 2.8, 0.08, 0.45, 0, true);
                p.sacudir(0.3);
        }
    }

    // ---------------------------------------------------------------- habilidades

    private double habilidad(Linea l, double t0, Actor3D a, Habilidad h, List<Actor3D> objetivos) {
        switch (h.getNombre()) {
            case "Golpe Devastador":
                return golpeDevastador(l, t0, a, objetivos);
            case "Postura Defensiva":
                return aura(l, t0, a, objetivos, 0xffd060, 0x60c0ff);
            case "Bola de Fuego":
                return bolaDeFuego(l, t0, a, objetivos);
            case "Escudo Arcano":
                return escudoArcano(l, t0, a, objetivos);
            case "Disparo Múltiple":
                return disparo(l, t0 + 0.15, a, objetivos, FLECHA, 0.13, 0.4);
            case "Flecha Envenenada":
                return disparo(l, t0 + 0.15, a, objetivos, FLECHA_VENENO, 0, 0.5);
            case "Curación":
                return curacion(l, t0, a, objetivos, 0xfff0a0, 0x70ff90);
            case "Bendición":
                return curacion(l, t0, a, objetivos, 0xffffff, 0xffe080);
            case "Curación Oscura":
                return curacion(l, t0, a, objetivos, 0x70ff80, 0xa040d0);
            default:
                if (h instanceof HabilidadCuracion) {
                    return curacion(l, t0, a, objetivos, 0xfff0a0, 0x70ff90);
                }
                if (h instanceof HabilidadEstado) {
                    TipoEstado e = ((HabilidadEstado) h).getEfectoAplicado();
                    if (e == TipoEstado.ESCUDO) {
                        return aura(l, t0, a, objetivos, 0x80c0ff, 0x60c0ff);
                    }
                    return disparo(l, t0, a, objetivos, e == TipoEstado.VENENO ? VERDE : LUZ, 0.12, 0.6);
                }
                return disparo(l, t0, a, objetivos, ARCANO, 0.15, 0.8);
        }
    }

    /** Guerrero: corre, salta muy alto y cae con un golpe que sacude la arena. */
    private double golpeDevastador(Linea l, double t0, Actor3D a, List<Actor3D> objetivos) {
        if (objetivos.isEmpty()) {
            return t0;
        }
        Actor3D o = objetivos.get(0);
        double destX = o.baseX + o.frente * (0.5 + o.ancho() * 0.42), destZ = o.baseZ - 0.08;
        double medioX = a.baseX + (destX - a.baseX) * 0.45, medioZ = a.baseZ + (destZ - a.baseZ) * 0.45;
        l.en(t0, () -> p.enfocar((a.x() + o.x()) / 2, (a.z() + o.z()) / 2, 7.2));
        l.durante(t0, 0.3, k -> {
            a.estirar = 1 - 0.22 * Math.sin(k * PI / 2);
            chispas(a.x(), 0.3, a.z(), 0xffa040, 1, 1.0, 0.07, 0.4, -1, true);
        });
        l.durante(t0 + 0.3, 0.25, k -> {
            double s = Linea.suave(k);
            a.estirar = 0.78 + 0.22 * k;
            a.ox = (medioX - a.baseX) * s;
            a.oz = (medioZ - a.baseZ) * s;
        });
        double ts = t0 + 0.55;
        l.en(ts, () -> polvo(a.x(), a.z(), 10));
        l.durante(ts, 0.55, k -> {
            a.ox = (medioX - a.baseX) + (destX - medioX) * k;
            a.oz = (medioZ - a.baseZ) + (destZ - medioZ) * k;
            a.oy = Math.sin(k * PI) * 1.5;
            a.inclinacion = k < 0.6 ? -0.4 * k : 0.9 * (k - 0.6) / 0.4;
            a.estirar = 1.1;
            chispas(a.x(), a.oy + a.alto() * 0.5, a.z(), 0xffc060, 1, 0.5, 0.08, 0.3, 0, true);
        });
        double tc = ts + 0.55;
        l.en(tc, () -> {
            a.oy = 0;
            for (Actor3D ob : objetivos) {
                p.impacto(ob, true);
            }
            onda(o.x(), o.z(), 0xffa040, 3.0);
            onda(o.x(), o.z(), 0xffe0a0, 1.8);
            polvo(o.x(), o.z(), 20);
            chispas(o.x(), 0.2, o.z(), 0x8a6a4a, 18, 4, 0.1, 1.0, 9, false);
            chispas(o.x(), o.alto() * 0.5, o.z(), 0xffe080, 20, 4, 0.08, 0.4, 4, true);
            p.sacudir(1.4);
            p.destelloPantalla(Color.WHITE, 0.35);
            p.ovacion(1);
        });
        l.durante(tc, 0.15, k -> a.estirar = 0.8 + 0.2 * k);
        l.durante(tc + 0.15, 0.25, k -> a.inclinacion = 0.9 * (1 - k));
        double tv = tc + 0.5;
        double fx = destX - a.baseX, fz = destZ - a.baseZ;
        l.durante(tv, 0.35, k -> {
            double s = 1 - Linea.suave(k);
            a.ox = fx * s;
            a.oz = fz * s;
            a.oy = Math.sin(k * PI) * 0.3;
        });
        l.en(tv + 0.35, () -> {
            a.ox = a.oz = a.oy = 0;
            a.estirar = 1;
            a.inclinacion = 0;
            p.soltarCamara();
        });
        return tv + 0.45;
    }

    /** Mago: junta fuego en la punta del bastón y lanza una bola de fuego a cada enemigo. */
    private double bolaDeFuego(Linea l, double t0, Actor3D a, List<Actor3D> objetivos) {
        l.en(t0, () -> p.enfocar(0.3, 0.3, 8.2));
        l.durante(t0, 0.25, k -> {
            a.oy = Math.sin(k * PI) * 0.3;
            a.inclinacion = -0.25 * k;
        });
        l.durante(t0 + 0.2, 0.75, k -> {
            double[] m = mano(a);
            double y = m[1] + 0.5;
            converger(m[0], y, m[2], k < 0.5 ? 0xff8020 : 0xffd040, 3, 0.9);
            instante(m[0], y, m[2], 0.12 + 0.35 * k, 0xff9020, true);
            instante(m[0], y, m[2], 0.06 + 0.15 * k, 0xffffc0, true);
            p.ovacion(0.4 + 0.4 * k);
        });
        double fin = t0 + 0.95;
        l.durante(fin, 0.15, k -> a.inclinacion = -0.25 + 0.6 * k);
        for (int i = 0; i < objetivos.size(); i++) {
            Actor3D o = objetivos.get(i);
            double salida = fin + i * 0.18;
            double vuelo = 0.55;
            l.durante(salida, vuelo, k -> {
                double[] m = mano(a);
                proyectil(FUEGO, m[0], m[1] + 0.5, m[2], o.x(), o.alto() * 0.5, o.z() - 0.1, k, 1.3, 0);
            });
            l.en(salida + vuelo, () -> {
                p.impacto(o, true);
                explosionFuego(o.x(), o.alto() * 0.5, o.z() - 0.1, 1.3);
                p.destelloPantalla(new Color(255, 140, 40), 0.18);
            });
        }
        double ultimo = fin + (objetivos.size() - 1) * 0.18 + 0.55;
        l.durante(ultimo, 0.2, k -> a.inclinacion = 0.35 * (1 - k));
        l.en(ultimo + 0.55, p::soltarCamara);
        return ultimo + 0.7;
    }

    /** Mago: runas arcanas giran a su alrededor y un orbe forma una burbuja protectora en el aliado. */
    private double escudoArcano(Linea l, double t0, Actor3D a, List<Actor3D> objetivos) {
        l.en(t0, () -> p.enfocar(a.x() + 0.6, a.z(), 6.8));
        l.durante(t0, 0.7, k -> {
            a.oy = Math.sin(k * PI) * 0.15;
            for (int i = 0; i < 2; i++) {
                double ang = k * 10 + i * PI;
                instante(a.x() + Math.cos(ang) * 0.7, 0.1 + k * a.alto(), a.z() + Math.sin(ang) * 0.5, 0.12, 0xa060ff, true);
                chispas(a.x() + Math.cos(ang) * 0.7, 0.1 + k * a.alto(), a.z() + Math.sin(ang) * 0.5, 0x7040e0, 1, 0.3, 0.07, 0.5, -0.5, true);
            }
        });
        double t = t0 + 0.7;
        for (Actor3D o : objetivos) {
            double vuelo = o == a ? 0 : 0.45;
            double salida = t;
            if (vuelo > 0) {
                l.durante(salida, vuelo, k -> {
                    double[] m = mano(a);
                    proyectil(ARCANO, m[0], m[1] + 0.3, m[2], o.x(), o.alto() * 0.6, o.z(), k, 0.9, 0);
                });
            }
            l.durante(salida + vuelo, 0.4, k -> burbuja(o, 0x70a0ff, 4));
            l.en(salida + vuelo + 0.35, () -> {
                p.impacto(o, false);
                chispas(o.x(), o.alto() * 0.5, o.z(), 0xa0d0ff, 18, 2.5, 0.08, 0.5, 0, true);
            });
            t = salida + vuelo + 0.2;
        }
        l.en(t + 0.6, p::soltarCamara);
        return t + 0.75;
    }

    /** Aura de protección propia (Postura Defensiva u otras de escudo). */
    private double aura(Linea l, double t0, Actor3D a, List<Actor3D> objetivos, int color, int burbuja) {
        l.en(t0, () -> p.enfocar(a.x() + 0.5, a.z(), 6.6));
        l.durante(t0, 0.35, k -> a.estirar = 1 - 0.18 * Math.sin(k * PI / 2));
        l.durante(t0 + 0.1, 0.6, k -> {
            for (Actor3D o : objetivos) {
                double ang = azar.nextDouble() * 2 * PI;
                Particula pa = new Particula(o.x() + Math.cos(ang) * 0.6, 0.05, o.z() + Math.sin(ang) * 0.45,
                        0, 2.2 + azar.nextDouble(), 0, 0.09, 0.6, color, 0x804010, true);
                p.particula(pa);
                burbuja(o, burbuja, 2);
            }
        });
        l.en(t0 + 0.35, () -> {
            a.estirar = 1;
            for (Actor3D o : objetivos) {
                onda(o.x(), o.z(), color, 1.6);
            }
        });
        l.en(t0 + 0.6, () -> {
            for (Actor3D o : objetivos) {
                p.impacto(o, false);
            }
        });
        l.en(t0 + 1.2, p::soltarCamara);
        return t0 + 1.3;
    }

    /** Curaciones: el lanzador brilla y una columna de luz cae sobre cada objetivo. */
    private double curacion(Linea l, double t0, Actor3D a, List<Actor3D> objetivos, int color, int color2) {
        l.en(t0, () -> {
            double mx = a.x(), mz = a.z();
            for (Actor3D o : objetivos) {
                mx += o.x();
                mz += o.z();
            }
            p.enfocar(mx / (objetivos.size() + 1), mz / (objetivos.size() + 1), 7.0);
        });
        l.durante(t0, 0.45, k -> {
            a.oy = Math.sin(k * PI) * 0.2;
            double[] m = mano(a);
            instante(m[0], m[1] + 0.55, m[2], 0.1 + 0.2 * Math.sin(k * PI), color, true);
            chispas(m[0], m[1] + 0.55, m[2], color, 1, 1.2, 0.07, 0.4, 0, true);
        });
        double t = t0 + 0.45;
        for (Actor3D o : objetivos) {
            double ti = t;
            l.durante(ti, 0.75, k -> {
                for (int i = 0; i < 2; i++) {
                    double ang = azar.nextDouble() * 2 * PI, r = azar.nextDouble() * 0.5;
                    p.particula(new Particula(o.x() + Math.cos(ang) * r, 3.5, o.z() + Math.sin(ang) * r * 0.7,
                            0, -6, 0, 0.09, 0.5, color, color2, true));
                    p.particula(new Particula(o.x() + Math.cos(ang) * 0.55, 0.1, o.z() + Math.sin(ang) * 0.4,
                            0, 1.6 + azar.nextDouble(), 0, 0.08, 0.8, color2, color, true));
                }
            });
            l.en(ti + 0.4, () -> {
                p.impacto(o, false);
                onda(o.x(), o.z(), color2, 1.3);
            });
            t += 0.2;
        }
        l.en(t + 0.8, p::soltarCamara);
        return t + 0.95;
    }

    // ---------------------------------------------------------------- defensa e ítems

    private double defensa(Linea l, double t0, Actor3D a) {
        l.en(t0, () -> p.enfocar(a.x() + a.frente * 0.8, a.z(), 6.8));
        l.durante(t0, 0.3, k -> a.estirar = 1 - 0.14 * Math.sin(k * PI / 2));
        l.durante(t0 + 0.15, 0.35, k -> {
            for (int i = 0; i < 3; i++) {
                double ang = -1 + 2 * azar.nextDouble();
                instante(a.x() + a.frente * 0.6 * Math.cos(ang), a.alto() * 0.5 + Math.sin(ang) * 0.6, a.z() - 0.1,
                        0.1, 0x80c0ff, true);
            }
        });
        l.en(t0 + 0.4, () -> {
            p.impacto(a, false);
            p.texto(a, "En guardia", new Color(150, 200, 255), 20);
        });
        l.durante(t0 + 0.5, 0.25, k -> a.estirar = 0.86 + 0.14 * k);
        l.en(t0 + 0.9, p::soltarCamara);
        return t0 + 1.0;
    }

    private double item(Linea l, double t0, Actor3D a, Actor3D o, String nombreItem) {
        if (o == null) {
            return t0;
        }
        int color = colorItem(nombreItem);
        l.en(t0, () -> p.enfocar((a.x() + o.x()) / 2, (a.z() + o.z()) / 2, 6.8));
        double vuelo = o == a ? 0.45 : 0.55;
        l.durante(t0 + 0.1, vuelo, k -> {
            if (o == a) {
                instante(a.x() + a.frente * 0.2, a.alto() + 0.2 + k * 0.3, a.z() - 0.2, 0.14, color, false);
                instante(a.x() + a.frente * 0.2, a.alto() + 0.33 + k * 0.3, a.z() - 0.2, 0.06, 0xe0e0f0, false);
            } else {
                double[] m = mano(a);
                proyectil(POCION, m[0], m[1], m[2], o.x(), o.alto() * 0.7, o.z(), k, 1.2, color);
            }
        });
        l.en(t0 + 0.1 + vuelo, () -> {
            chispas(o.x(), o.alto() * 0.7, o.z(), color, 14, 2.5, 0.08, 0.6, 7, false);
            chispas(o.x(), o.alto() * 0.7, o.z(), 0xe0f0ff, 6, 2, 0.05, 0.3, 0, true);
        });
        l.durante(t0 + 0.1 + vuelo, 0.6, k -> {
            double ang = azar.nextDouble() * 2 * PI;
            p.particula(new Particula(o.x() + Math.cos(ang) * 0.5, 0.1, o.z() + Math.sin(ang) * 0.35,
                    0, 1.8, 0, 0.08, 0.7, color, 0xffffff, true));
        });
        l.en(t0 + 0.4 + vuelo, () -> p.impacto(o, false));
        l.en(t0 + 1.0 + vuelo, p::soltarCamara);
        return t0 + 1.1 + vuelo;
    }

    private int colorItem(String nombre) {
        String n = nombre.toLowerCase();
        if (n.contains("man")) {
            return 0x4090ff;
        }
        if (n.contains("ant")) {
            return 0x70e040;
        }
        if (n.contains("tenac")) {
            return 0xffd040;
        }
        return 0xff4050;
    }

    // ---------------------------------------------------------------- entrada, final, derrotas

    Linea entrada(Linea l, int numeroBatalla) {
        boolean jefe = false;
        int indiceAliado = 0, indiceEnemigo = 0;
        for (Actor3D a : p.actores) {
            if (!a.vivo) {
                continue;
            }
            if (a.frente > 0) {
                double inicio = 0.15 + indiceAliado++ * 0.13;
                a.ox = -5;
                l.durante(inicio, 0.75, k -> {
                    a.ox = -5 * (1 - Linea.suave(k));
                    a.oy = Math.abs(Math.sin(k * PI * 3)) * 0.18;
                });
                l.en(inicio + 0.75, () -> polvo(a.x(), a.z(), 5));
            } else {
                double inicio = 0.55 + indiceEnemigo++ * 0.22;
                double bajo = -a.alto() - 0.2;
                a.oy = bajo;
                l.en(inicio, () -> {
                    chispas(a.x(), 0.2, a.z(), 0x9040c0, 24, 2.2, 0.14, 0.9, -0.6, true);
                    chispas(a.x(), 0.1, a.z(), 0x302030, 14, 1.5, 0.16, 1.0, -0.3, false);
                });
                l.durante(inicio, 0.65, k -> a.oy = bajo * (1 - Linea.suave(k)));
                if (a.combatiente instanceof ashbornrise.modelo.enemigos.SenorDeLasSombras) {
                    jefe = true;
                }
            }
        }
        boolean esJefe = jefe;
        l.en(0, () -> {
            p.cartel(esJefe ? "¡Batalla final: el Señor de las Sombras!" : "Batalla " + numeroBatalla);
            p.ovacion(0.9);
        });
        if (esJefe) {
            l.en(1.2, () -> {
                p.sacudir(1.5);
                p.destelloPantalla(new Color(120, 0, 30), 0.45);
                p.ovacion(1);
            });
        }
        l.durante(1.0, 0.8, k -> {
        });
        return l;
    }

    Linea victoria(Linea l) {
        l.en(0, () -> {
            p.cartel("¡VICTORIA!");
            p.enfocar(-3.3, 0.2, 7.0);
        });
        int i = 0;
        for (Actor3D a : p.actores) {
            if (a.frente > 0 && a.vivo) {
                double inicio = 0.2 + i++ * 0.12;
                l.durante(inicio, 2.0, k -> a.oy = Math.abs(Math.sin(k * PI * 4)) * 0.45);
            }
        }
        l.durante(0, 3.0, k -> {
            p.ovacion(1);
            int[] colores = {0xff4060, 0xffd040, 0x40c0ff, 0x60ff80, 0xff80ff, 0xffffff};
            for (int j = 0; j < 3; j++) {
                Particula c = new Particula(-6 + azar.nextDouble() * 9, 5.5, -2 + azar.nextDouble() * 5,
                        (azar.nextDouble() - 0.5) * 0.8, -0.5, (azar.nextDouble() - 0.5) * 0.8,
                        0.07, 3.0, colores[azar.nextInt(colores.length)], 0x404040, false);
                c.gravedad = 0.6;
                c.friccion = 0.985;
                p.particula(c);
            }
        });
        return l;
    }

    Linea derrota(Linea l) {
        l.en(0, () -> {
            p.cartel("Tu equipo fue derrotado...");
            p.enfocar(2.5, 0.3, 7.5);
            p.destelloPantalla(new Color(90, 0, 0), 0.5);
        });
        int i = 0;
        for (Actor3D a : p.actores) {
            if (a.frente < 0 && a.vivo) {
                double inicio = 0.3 + i++ * 0.15;
                l.durante(inicio, 1.6, k -> a.oy = Math.abs(Math.sin(k * PI * 3)) * 0.3);
            }
        }
        l.durante(0, 2.0, k -> p.ovacion(0.8));
        return l;
    }

    /** Polvo y humo cuando alguien cae derrotado. */
    void derrumbe(Actor3D a) {
        polvo(a.x(), a.z(), 16);
        if (a.frente < 0) {
            chispas(a.x(), a.alto() * 0.5, a.z(), 0x8030b0, 20, 1.5, 0.12, 1.1, -1.2, true);
        } else {
            chispas(a.x(), a.alto() * 0.5, a.z(), 0xc0c0d0, 10, 1.2, 0.09, 0.9, -1, true);
        }
        p.texto(a, "Derrotado", new Color(200, 190, 210), 18);
    }

    private double segundaFase(Linea l, Actor3D jefe) {
        l.en(0, () -> {
            p.cartel("¡El Señor de las Sombras entra en su segunda fase!");
            p.enfocar(jefe.x(), jefe.z(), 6.5);
        });
        l.durante(0, 1.2, k -> {
            jefe.estirar = 1 + 0.12 * Math.sin(k * PI);
            jefe.destello = 0.3 * Math.sin(k * PI);
            for (int i = 0; i < 3; i++) {
                double ang = azar.nextDouble() * 2 * PI;
                p.particula(new Particula(jefe.x() + Math.cos(ang) * 1.1, 0.1, jefe.z() + Math.sin(ang) * 0.8,
                        -Math.cos(ang) * 0.5, 2.5 + azar.nextDouble() * 2, -Math.sin(ang) * 0.4,
                        0.12, 0.8, 0xc040ff, 0x400060, true));
            }
            p.sacudir(0.4);
        });
        l.en(1.0, () -> {
            onda(jefe.x(), jefe.z(), 0xc040ff, 4.0);
            p.destelloPantalla(new Color(140, 0, 180), 0.4);
            p.sacudir(1.5);
            p.ovacion(1);
        });
        return 1.4;
    }

    // ---------------------------------------------------------------- efectos

    private void proyectil(int tipo, double x0, double y0, double z0, double x1, double y1, double z1,
                           double k, double arco, int color) {
        double x = x0 + (x1 - x0) * k;
        double y = y0 + (y1 - y0) * k + Math.sin(k * PI) * arco;
        double z = z0 + (z1 - z0) * k;
        double dx = x1 - x0, dy = (y1 - y0) + Math.cos(k * PI) * PI * arco, dz = z1 - z0;
        double n = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= n;
        dy /= n;
        dz /= n;
        switch (tipo) {
            case FLECHA:
            case FLECHA_VENENO:
                for (int i = 0; i < 7; i++) {
                    int c = i == 0 ? 0xd8e0e8 : (i >= 5 ? 0xf0e8d8 : 0x8a5a2a);
                    instante(x - dx * i * 0.07, y - dy * i * 0.07, z - dz * i * 0.07, 0.065, c, false);
                }
                p.particula(new Particula(x, y, z, 0, 0, 0, 0.06, 0.2, tipo == FLECHA ? 0x808080 : 0x60ff40, 0x101010, true));
                break;
            case FUEGO:
                instante(x, y, z, 0.42, 0xff8020, true);
                instante(x, y, z, 0.2, 0xffffa0, true);
                for (int i = 0; i < 3; i++) {
                    p.particula(new Particula(x, y, z, (azar.nextDouble() - 0.5) * 0.8, 0.4 + azar.nextDouble() * 0.6,
                            (azar.nextDouble() - 0.5) * 0.8, 0.16, 0.4, 0xffb030, 0x901010, true));
                }
                if (azar.nextDouble() < 0.5) {
                    p.particula(new Particula(x, y, z, 0, 0.6, 0, 0.14, 0.6, 0x4a3a3a, 0x201818, false));
                }
                break;
            case POCION:
                instante(x, y, z, 0.13, color, false);
                instante(x, y + 0.11, z, 0.06, 0xe0e0f0, false);
                p.particula(new Particula(x, y, z, 0, 0, 0, 0.05, 0.3, 0xffffff, color, true));
                break;
            default:
                int c = tipo == ARCANO ? 0xff50e0 : tipo == LUZ ? 0xfff0a0 : 0x60ff80;
                instante(x, y, z, 0.26, c, true);
                instante(x, y, z, 0.1, 0xffffff, true);
                for (int i = 0; i < 2; i++) {
                    p.particula(new Particula(x, y, z, (azar.nextDouble() - 0.5) * 0.6, (azar.nextDouble() - 0.5) * 0.6,
                            (azar.nextDouble() - 0.5) * 0.6, 0.1, 0.35, c, 0x200020, true));
                }
        }
    }

    /** Partícula que se ve un solo cuadro (para dibujar algo que se mueve, como una flecha). */
    private void instante(double x, double y, double z, double tam, int color, boolean brillo) {
        p.particula(new Particula(x, y, z, 0, 0, 0, tam, 0.0001, color, color, brillo));
    }

    private void chispas(double x, double y, double z, int color, int n, double velocidad, double tam,
                         double duracion, double gravedad, boolean brillo) {
        for (int i = 0; i < n; i++) {
            double th = azar.nextDouble() * 2 * PI, ph = Math.acos(2 * azar.nextDouble() - 1);
            double v = velocidad * (0.35 + 0.65 * azar.nextDouble());
            Particula pa = new Particula(x, y, z, Math.sin(ph) * Math.cos(th) * v, Math.cos(ph) * v + gravedad * 0.08,
                    Math.sin(ph) * Math.sin(th) * v, tam, duracion * (0.6 + 0.4 * azar.nextDouble()), color,
                    brillo ? Motor3D.escalarColor(color, 0.3) : color, brillo);
            pa.gravedad = gravedad;
            pa.friccion = 0.95;
            p.particula(pa);
        }
    }

    /** Arco de luz que dibuja el filo de un arma. */
    private void tajo(double x, double y, double z, int frente, int color, double radio) {
        for (int i = 0; i < 16; i++) {
            double ang = Math.toRadians(120 - i * 13);
            double px = x - frente * Math.cos(ang) * radio * 0.6, py = y + Math.sin(ang) * radio;
            Particula pa = new Particula(px, py, z, -frente * 0.6, -0.3, 0, 0.1, 0.18 + i * 0.012, color, 0x404060, true);
            p.particula(pa);
        }
    }

    private void explosionFuego(double x, double y, double z, double escala) {
        chispas(x, y, z, 0xffc040, (int) (34 * escala), 4.2 * escala, 0.16, 0.6, -1.5, true);
        chispas(x, y, z, 0xff5010, (int) (22 * escala), 3 * escala, 0.2, 0.7, -1, true);
        chispas(x, y, z, 0xffffe0, 8, 2, 0.12, 0.25, 0, true);
        chispas(x, y + 0.2, z, 0x3a2e2e, (int) (12 * escala), 1.2, 0.2, 1.3, -0.8, false);
        onda(x, z, 0xff7020, 1.6 * escala);
        p.sacudir(0.8 * escala);
        p.ovacion(0.9);
    }

    private void nubeVeneno(double x, double y, double z, double escala) {
        for (int i = 0; i < (int) (16 * escala); i++) {
            double th = azar.nextDouble() * 2 * PI;
            double v = 0.4 + azar.nextDouble() * 0.9;
            Particula pa = new Particula(x, y, z, Math.cos(th) * v, (azar.nextDouble() - 0.3) * 0.6, Math.sin(th) * v * 0.6,
                    0.2, 1.0 + azar.nextDouble() * 0.5, 0x60c030, 0x203010, false);
            pa.friccion = 0.93;
            p.particula(pa);
        }
        chispas(x, y, z, 0xa0ff60, (int) (10 * escala), 1.5, 0.08, 0.8, -0.5, true);
    }

    private void polvo(double x, double z, int n) {
        for (int i = 0; i < n; i++) {
            double th = azar.nextDouble() * 2 * PI;
            double v = 0.6 + azar.nextDouble() * 1.4;
            Particula pa = new Particula(x + Math.cos(th) * 0.2, 0.08, z + Math.sin(th) * 0.2,
                    Math.cos(th) * v, 0.3 + azar.nextDouble() * 0.5, Math.sin(th) * v,
                    0.16, 0.7 + azar.nextDouble() * 0.4, 0xc8a878, 0x8a7458, false);
            pa.friccion = 0.92;
            p.particula(pa);
        }
    }

    /** Onda expansiva en el piso. */
    private void onda(double x, double z, int color, double radio) {
        int n = 36;
        for (int i = 0; i < n; i++) {
            double th = 2 * PI * i / n;
            double v = radio / 0.45;
            Particula pa = new Particula(x, 0.08, z, Math.cos(th) * v, 0, Math.sin(th) * v * 0.8,
                    0.13, 0.5, color, Motor3D.escalarColor(color, 0.2), true);
            pa.friccion = 0.93;
            p.particula(pa);
        }
    }

    /** Partículas que se juntan hacia un punto (cargando un hechizo). */
    private void converger(double x, double y, double z, int color, int n, double radio) {
        for (int i = 0; i < n; i++) {
            double th = azar.nextDouble() * 2 * PI, ph = Math.acos(2 * azar.nextDouble() - 1);
            double sx = x + Math.sin(ph) * Math.cos(th) * radio, sy = y + Math.cos(ph) * radio, sz = z + Math.sin(ph) * Math.sin(th) * radio;
            double dur = 0.3;
            p.particula(new Particula(sx, sy, sz, (x - sx) / dur, (y - sy) / dur, (z - sz) / dur, 0.08, dur,
                    Motor3D.escalarColor(color, 0.6), color, true));
        }
    }

    /** Burbuja que se forma alrededor de un combatiente. */
    private void burbuja(Actor3D o, int color, int n) {
        double cy = o.alto() * 0.5;
        for (int i = 0; i < n; i++) {
            double th = azar.nextDouble() * 2 * PI, ph = Math.acos(2 * azar.nextDouble() - 1);
            double r = 1.3, dur = 0.35;
            double sx = Math.sin(ph) * Math.cos(th) * r, sy = Math.cos(ph) * r, sz = Math.sin(ph) * Math.sin(th) * r * 0.8;
            p.particula(new Particula(o.x() + sx, cy + sy, o.z() + sz, -sx * 0.45 / dur, -sy * 0.45 / dur, -sz * 0.45 / dur,
                    0.09, dur, Motor3D.escalarColor(color, 0.5), color, true));
        }
    }

    private void monedas(Actor3D desde, Actor3D hacia) {
        for (int i = 0; i < 7; i++) {
            Particula m = new Particula(desde.x(), desde.alto() * 0.6, desde.z() - 0.1,
                    (hacia.x() - desde.x()) * (0.4 + azar.nextDouble() * 0.3) + (azar.nextDouble() - 0.5),
                    3 + azar.nextDouble() * 2, (azar.nextDouble() - 0.5) * 1.2,
                    0.09, 1.1, 0xffd040, 0xc09020, false);
            m.gravedad = 9;
            p.particula(m);
        }
        p.texto(hacia, "¡Robo!", new Color(255, 210, 80), 18);
    }

    // ---------------------------------------------------------------- ayudas

    private double[] mano(Actor3D a) {
        return new double[]{a.x() + a.frente * a.ancho() * 0.35, a.y() + a.alto() * 0.6, a.z() - 0.1};
    }

    private List<Actor3D> actores(List<Combatiente> cs) {
        List<Actor3D> lista = new ArrayList<>();
        for (Combatiente c : cs) {
            Actor3D a = p.actor(c);
            if (a != null) {
                lista.add(a);
            }
        }
        return lista;
    }

    private static String clase(Actor3D a) {
        return a.combatiente.getClass().getSimpleName();
    }

    /** Tipo de proyectil del ataque básico, o -1 si pelea cuerpo a cuerpo. */
    private static int tipoDistancia(Actor3D a) {
        switch (clase(a)) {
            case "Arquero":
                return FLECHA;
            case "Mago":
                return ARCANO;
            case "Curador":
                return LUZ;
            case "ChamanOscuro":
                return VERDE;
            default:
                return -1;
        }
    }

    private static boolean contiene(List<String> lineas, String texto) {
        for (String l : lineas) {
            if (l.contains(texto)) {
                return true;
            }
        }
        return false;
    }
}
