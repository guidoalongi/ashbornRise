package ashbornrise.modelo.items;

import ashbornrise.modelo.personajes.Personaje;

public class RestaurarVida implements EfectoConsumible {

    private int cantidad;

    public RestaurarVida(int cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String aplicar(Personaje objetivo) {
        return objetivo.getNombre() + " recupera " + objetivo.curar(cantidad) + " de vida.";
    }
}
