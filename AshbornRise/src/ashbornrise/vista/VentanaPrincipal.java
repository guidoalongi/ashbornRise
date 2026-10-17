package ashbornrise.vista;

import ashbornrise.controlador.ControladorBatalla;
import ashbornrise.controlador.ControladorJuego;
import ashbornrise.modelo.Partida;
import ashbornrise.modelo.Recompensa;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

/** Ventana única del juego. Cambia entre las 4 pantallas con un CardLayout. */
public class VentanaPrincipal extends JFrame {

    private static final String INICIO = "inicio";
    private static final String JUEGO = "juego";
    private static final String BATALLA = "batalla";
    private static final String RESULTADOS = "resultados";

    private CardLayout cartas;
    private JPanel contenedor;
    private VistaInicio vistaInicio;
    private VistaJuego vistaJuego;
    private VistaBatalla vistaBatalla;
    private VistaResultados vistaResultados;

    public VentanaPrincipal() {
        super("Ashborn Rise");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 760));

        vistaInicio = new VistaInicio();
        vistaJuego = new VistaJuego();
        vistaBatalla = new VistaBatalla();
        vistaResultados = new VistaResultados();

        cartas = new CardLayout();
        contenedor = new JPanel(cartas);
        contenedor.add(vistaInicio, INICIO);
        contenedor.add(vistaJuego, JUEGO);
        contenedor.add(vistaBatalla, BATALLA);
        contenedor.add(vistaResultados, RESULTADOS);
        setContentPane(contenedor);
        pack();
        setLocationRelativeTo(null);
    }

    public void conectarControladores(ControladorJuego cj, ControladorBatalla cb) {
        vistaInicio.setControlador(cj);
        vistaJuego.setControlador(cj);
        vistaResultados.setControlador(cj);
        vistaBatalla.setControlador(cb);
    }

    public void mostrarInicio() {
        cartas.show(contenedor, INICIO);
    }

    public void mostrarJuego(Partida partida) {
        vistaJuego.actualizar(partida);
        cartas.show(contenedor, JUEGO);
    }

    public void mostrarBatalla() {
        cartas.show(contenedor, BATALLA);
    }

    public void mostrarResultadosVictoria(Recompensa recompensa, Partida partida, int rondas) {
        vistaResultados.mostrarVictoria(recompensa, partida, rondas);
        cartas.show(contenedor, RESULTADOS);
    }

    public void mostrarResultadosDerrota(int numeroBatalla) {
        vistaResultados.mostrarDerrota(numeroBatalla);
        cartas.show(contenedor, RESULTADOS);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Ashborn Rise", JOptionPane.ERROR_MESSAGE);
    }

    public VistaJuego getVistaJuego() {
        return vistaJuego;
    }

    public VistaBatalla getVistaBatalla() {
        return vistaBatalla;
    }
}
