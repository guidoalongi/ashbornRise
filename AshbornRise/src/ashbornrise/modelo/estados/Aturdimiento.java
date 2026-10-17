package ashbornrise.modelo.estados;

/** El afectado pierde su próximo turno. */
public class Aturdimiento extends EstadoEfecto {

    public Aturdimiento() {
        super("Aturdimiento", 1);
    }

    @Override
    public boolean impideActuar() {
        return true;
    }

    @Override
    public TipoEstado getTipo() {
        return TipoEstado.ATURDIMIENTO;
    }
}
