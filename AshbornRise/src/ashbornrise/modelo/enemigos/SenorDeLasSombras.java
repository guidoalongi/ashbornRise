package ashbornrise.modelo.enemigos;

import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.acciones.AccionAtacar;
import ashbornrise.modelo.estados.Aturdimiento;

/**
 * Jefe final con dos fases. Al bajar del 50% de vida pasa a la fase 2:
 * su ataque sube un 40% y sus golpes pueden aturdir.
 */
public class SenorDeLasSombras extends Enemigo {

    private int faseActual;
    private boolean cambioDeFaseAnunciado;

    public SenorDeLasSombras(String nombre, int numeroBatalla) {
        super(nombre, 420, 26, 18, 10, 300, 200);
        this.faseActual = 1;
        this.cambioDeFaseAnunciado = false;
    }

    @Override
    public int recibirDanio(int cantidad) {
        int danio = super.recibirDanio(cantidad);
        if (faseActual == 1 && estaVivo() && vidaActual <= vidaMaximo / 2) {
            faseActual = 2;
            ataque = (int) Math.round(ataque * 1.4);
        }
        return danio;
    }

    @Override
    public Accion decidirAccion(Batalla batalla) {
        if (faseActual == 2 && !cambioDeFaseAnunciado) {
            batalla.registrar("¡" + nombre + " entra en su segunda fase! Su poder aumenta.");
            cambioDeFaseAnunciado = true;
        }
        if (faseActual == 1) {
            return new AccionAtacar(elegirAlAzar(batalla.getAliadosVivos()));
        }
        // Fase 2: va a por el más débil.
        return new AccionAtacar(elegirConMenosVida(batalla.getAliadosVivos()));
    }

    @Override
    public void alGolpear(Combatiente objetivo, Batalla batalla) {
        if (faseActual == 2 && objetivo.estaVivo() && random.nextDouble() < 0.35) {
            if (objetivo.agregarEstado(new Aturdimiento())) {
                batalla.registrar(objetivo.getNombre() + " queda aturdido.");
            }
        }
    }

    public int getFaseActual() {
        return faseActual;
    }
}
