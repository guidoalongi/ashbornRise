package ashbornrise.modelo.items;

import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.personajes.Personaje;

public class RestaurarMana implements EfectoConsumible {

    private int cantidad;

    public RestaurarMana(int cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String aplicar(Personaje objetivo) {
        if (objetivo.getTipoRecurso() != TipoRecurso.MANA) {
            return "No tiene efecto: " + objetivo.getNombre() + " usa Energía.";
        }
        return objetivo.getNombre() + " recupera " + objetivo.recuperarRecurso(cantidad) + " de maná.";
    }
}
