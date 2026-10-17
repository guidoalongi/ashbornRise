package ashbornrise.modelo;

import ashbornrise.modelo.personajes.Personaje;

import java.util.ArrayList;
import java.util.List;

/** La party del jugador: entre 2 y 4 personajes. */
public class Equipo {

    public static final int MINIMO = 2;
    public static final int MAXIMO = 4;

    private List<Personaje> integrantes;

    public Equipo() {
        this.integrantes = new ArrayList<>();
    }

    public void agregarIntegrante(Personaje p) {
        if (integrantes.size() >= MAXIMO) {
            throw new IllegalStateException("El equipo no puede tener más de " + MAXIMO + " integrantes");
        }
        integrantes.add(p);
    }

    public boolean esValido() {
        return integrantes.size() >= MINIMO && integrantes.size() <= MAXIMO;
    }

    public List<Personaje> obtenerVivos() {
        List<Personaje> vivos = new ArrayList<>();
        for (Personaje p : integrantes) {
            if (p.estaVivo()) {
                vivos.add(p);
            }
        }
        return vivos;
    }

    public boolean estaDerrotado() {
        return obtenerVivos().isEmpty();
    }

    public void recuperarTrasBatalla() {
        for (Personaje p : integrantes) {
            p.recuperarTrasBatalla();
        }
    }

    public void restaurarCompleto() {
        for (Personaje p : integrantes) {
            p.restaurarCompleto();
        }
    }

    public List<Personaje> getIntegrantes() {
        return new ArrayList<>(integrantes);
    }
}
