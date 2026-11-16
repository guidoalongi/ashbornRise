package ashbornrise.modelo.items;

/** Lugar donde se equipa un ítem. Un personaje lleva como máximo uno por ranura. */
public enum TipoRanura {
    ARMA("Arma"),
    ARMADURA("Armadura"),
    ACCESORIO("Accesorio");

    private String nombre;

    TipoRanura(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
