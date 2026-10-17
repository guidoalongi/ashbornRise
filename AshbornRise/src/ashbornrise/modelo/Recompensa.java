package ashbornrise.modelo;

import ashbornrise.modelo.items.Item;

import java.util.ArrayList;
import java.util.List;

/** Fabricación Pura que agrupa el resultado de una batalla ganada. */
public class Recompensa {

    private int experiencia;
    private int experienciaPorPersonaje;
    private int oro;
    private List<Item> items;
    private List<String> subidasDeNivel;

    public Recompensa(int experiencia, int experienciaPorPersonaje, int oro, List<Item> items) {
        this.experiencia = experiencia;
        this.experienciaPorPersonaje = experienciaPorPersonaje;
        this.oro = oro;
        this.items = new ArrayList<>(items);
        this.subidasDeNivel = new ArrayList<>();
    }

    public void registrarSubidaDeNivel(String descripcion) {
        subidasDeNivel.add(descripcion);
    }

    public int getExperiencia() {
        return experiencia;
    }

    public int getExperienciaPorPersonaje() {
        return experienciaPorPersonaje;
    }

    public int getOro() {
        return oro;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public List<String> getSubidasDeNivel() {
        return new ArrayList<>(subidasDeNivel);
    }
}
