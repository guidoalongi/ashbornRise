package ashbornrise.vista;

import ashbornrise.controlador.ControladorJuego;
import ashbornrise.modelo.Partida;
import ashbornrise.modelo.Recompensa;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de resultados: experiencia, subidas de nivel y botín (o la derrota). */
public class VistaResultados extends JPanel {

    private ControladorJuego controlador;
    private JPanel contenido;

    public VistaResultados() {
        setBackground(Estilo.FONDO);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        add(contenido, BorderLayout.CENTER);
    }

    public void mostrarVictoria(Recompensa recompensa, Partida partida, int rondas) {
        contenido.removeAll();
        agregarCentrado(Estilo.etiqueta("¡ VICTORIA !", 56, Estilo.DORADO));
        agregarCentrado(Estilo.etiqueta("El equipo derrotó a todos los enemigos en la Ronda " + rondas,
                18, Estilo.TEXTO_SUAVE));
        contenido.add(Box.createRigidArea(new Dimension(0, 24)));

        // Experiencia y progresión
        JPanel exp = Estilo.tarjeta();
        exp.setLayout(new BoxLayout(exp, BoxLayout.Y_AXIS));
        exp.add(Estilo.etiqueta("EXPERIENCIA Y PROGRESIÓN  —  +" + recompensa.getExperienciaPorPersonaje()
                + " EXP para cada personaje en pie", 14, Estilo.DORADO));
        exp.add(Box.createRigidArea(new Dimension(0, 10)));
        for (Personaje p : partida.getEquipo().getIntegrantes()) {
            JPanel fila = new JPanel(new BorderLayout());
            fila.setOpaque(false);
            fila.add(Estilo.etiqueta(p.getNombre() + " (" + p.getNombreClase() + ")  ·  Nvl. " + p.getNivel(),
                    18, Estilo.TEXTO), BorderLayout.WEST);
            fila.add(Estilo.etiqueta(p.getExperiencia() + " / " + p.experienciaParaSubir() + " EXP",
                    14, Estilo.DORADO), BorderLayout.EAST);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            exp.add(fila);
            JPanel barra = new JPanel(new BorderLayout());
            barra.setOpaque(false);
            barra.add(Estilo.barra(p.getExperiencia(), p.experienciaParaSubir(), Estilo.EXPERIENCIA));
            barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
            barra.setAlignmentX(Component.LEFT_ALIGNMENT);
            exp.add(barra);
            exp.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        if (!recompensa.getSubidasDeNivel().isEmpty()) {
            exp.add(filaDeChips(recompensa.getSubidasDeNivel()));
        }
        agregarTarjeta(exp);
        contenido.add(Box.createRigidArea(new Dimension(0, 18)));

        // Botín
        JPanel botin = Estilo.tarjeta();
        botin.setLayout(new BoxLayout(botin, BoxLayout.Y_AXIS));
        botin.add(Estilo.etiqueta("BOTÍN OBTENIDO", 14, Estilo.DORADO));
        botin.add(Box.createRigidArea(new Dimension(0, 8)));
        List<String> chips = new ArrayList<>();
        chips.add("Oro +" + recompensa.getOro());
        for (Item item : recompensa.getItems()) {
            chips.add(item.getNombre());
        }
        botin.add(filaDeChips(chips));
        agregarTarjeta(botin);
        contenido.add(Box.createRigidArea(new Dimension(0, 24)));

        JButton continuar = Estilo.boton("CONTINUAR");
        continuar.addActionListener(e -> controlador.continuarTrasResultados());
        agregarBotones(continuar);
        refrescar();
    }

    public void mostrarDerrota(int numeroBatalla) {
        contenido.removeAll();
        contenido.add(Box.createRigidArea(new Dimension(0, 120)));
        agregarCentrado(Estilo.etiqueta("DERROTA", 56, Estilo.VIDA));
        agregarCentrado(Estilo.etiqueta("Tu equipo cayó en la batalla " + numeroBatalla + ".", 20, Estilo.TEXTO_SUAVE));
        contenido.add(Box.createRigidArea(new Dimension(0, 60)));
        JButton reintentar = Estilo.boton("REINTENTAR");
        JButton cargar = Estilo.boton("CARGAR ÚLTIMA PARTIDA");
        JButton menu = Estilo.boton("MENÚ PRINCIPAL");
        reintentar.addActionListener(e -> controlador.reintentarBatalla());
        cargar.addActionListener(e -> controlador.cargarPartida());
        menu.addActionListener(e -> controlador.volverAlInicio());
        agregarBotones(reintentar, cargar, menu);
        refrescar();
    }

    private JPanel filaDeChips(List<String> textos) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String t : textos) {
            JLabel chip = Estilo.etiqueta(t.toUpperCase(), 13, Estilo.DORADO);
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Estilo.DORADO),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
            fila.add(chip);
        }
        return fila;
    }

    private void agregarCentrado(JLabel l) {
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(l);
    }

    private void agregarTarjeta(JPanel tarjeta) {
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getPreferredSize().height));
        contenido.add(tarjeta);
    }

    private void agregarBotones(JButton... botones) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        fila.setOpaque(false);
        for (JButton b : botones) {
            fila.add(b);
        }
        fila.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(fila);
    }

    private void refrescar() {
        revalidate();
        repaint();
    }

    public void setControlador(ControladorJuego controlador) {
        this.controlador = controlador;
    }
}
