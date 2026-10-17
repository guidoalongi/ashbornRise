package ashbornrise.vista;

import ashbornrise.controlador.ControladorJuego;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;

/** Pantalla inicial: Nueva partida, Cargar partida y Salir. */
public class VistaInicio extends JPanel {

    private ControladorJuego controlador;

    public VistaInicio() {
        setBackground(Estilo.FONDO);
        setLayout(new GridBagLayout());

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JLabel titulo = Estilo.etiqueta("ASHBORN  RISE", 72, Estilo.DORADO);
        JLabel subtitulo = Estilo.etiqueta("R P G   P O R   T U R N O S", 20, Estilo.TEXTO_SUAVE);
        JButton btnNueva = crearBoton("NUEVA PARTIDA");
        JButton btnCargar = crearBoton("CARGAR PARTIDA");
        JButton btnSalir = crearBoton("SALIR");

        btnNueva.addActionListener(e -> controlador.nuevaPartida());
        btnCargar.addActionListener(e -> controlador.cargarPartida());
        btnSalir.addActionListener(e -> controlador.salir());

        centrar(titulo);
        centrar(subtitulo);
        centro.add(titulo);
        centro.add(subtitulo);
        centro.add(Box.createRigidArea(new Dimension(0, 90)));
        centro.add(btnNueva);
        centro.add(Box.createRigidArea(new Dimension(0, 16)));
        centro.add(btnCargar);
        centro.add(Box.createRigidArea(new Dimension(0, 16)));
        centro.add(btnSalir);
        add(centro);
    }

    private JButton crearBoton(String texto) {
        JButton b = Estilo.boton(texto);
        b.setMaximumSize(new Dimension(400, 46));
        b.setPreferredSize(new Dimension(400, 46));
        centrar(b);
        return b;
    }

    private void centrar(javax.swing.JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    public void setControlador(ControladorJuego controlador) {
        this.controlador = controlador;
    }
}
