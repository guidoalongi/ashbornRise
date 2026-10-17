package ashbornrise;

import ashbornrise.controlador.ControladorBatalla;
import ashbornrise.controlador.ControladorJuego;
import ashbornrise.persistencia.PersistenciaArchivoJSON;
import ashbornrise.vista.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada: arma la Vista, los Controladores y la Persistencia, y los conecta (MVC). */
public class Main {

    public static void main(String[] args) {
        // En algunas PCs con Windows, el dibujado con Direct3D hace que Java se cierre
        // sin mensaje (código 0xC0000005). Con esto Swing dibuja por software.
        System.setProperty("sun.java2d.d3d", "false");
        try {
            // Look & feel multiplataforma: así los colores se ven igual en Windows, Mac y Linux.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // si falla, se usa el look & feel por defecto
        }
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ControladorJuego controladorJuego = new ControladorJuego(new PersistenciaArchivoJSON(), ventana);
            ControladorBatalla controladorBatalla = new ControladorBatalla(controladorJuego, ventana.getVistaBatalla());
            controladorJuego.setControladorBatalla(controladorBatalla);
            ventana.conectarControladores(controladorJuego, controladorBatalla);
            ventana.mostrarInicio();
            ventana.setVisible(true);
        });
    }
}
