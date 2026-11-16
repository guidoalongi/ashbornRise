package ashbornrise.vista;

import ashbornrise.controlador.ControladorJuego;
import ashbornrise.dto.EquipadoDTO;
import ashbornrise.dto.ItemDTO;
import ashbornrise.dto.PartidaDTO;
import ashbornrise.dto.PersonajeDTO;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de estado del equipo e inventario (entre batallas).
 * Recibe un PartidaDTO (solo datos): no conoce las clases del modelo.
 */
public class VistaJuego extends JPanel {

    private ControladorJuego controlador;

    private JLabel lblInfo;
    private JLabel lblMensaje;
    private JPanel panelPersonajes;
    private JPanel panelItems;
    private JComboBox<String> comboItems;
    private JComboBox<String> comboPersonajes;
    private JButton btnSiguiente;

    // Listas paralelas a los combos: la posición del combo indica el ítem / personaje elegido.
    private List<ItemDTO> itemsDelCombo = new ArrayList<>();
    private List<PersonajeDTO> personajesDelCombo = new ArrayList<>();

    public VistaJuego() {
        setBackground(Estilo.FONDO);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Encabezado
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Estilo.etiqueta("Estado del Equipo", 32, Estilo.DORADO), BorderLayout.WEST);
        lblInfo = Estilo.etiqueta("", 16, Estilo.TEXTO_SUAVE);
        encabezado.add(lblInfo, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        // Centro: personajes + inventario
        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        panelPersonajes = new JPanel(new GridLayout(1, 4, 16, 0));
        panelPersonajes.setOpaque(false);
        centro.add(panelPersonajes, BorderLayout.CENTER);
        centro.add(crearPanelInventario(), BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // Abajo: botones
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        lblMensaje = Estilo.etiqueta(" ", 16, Estilo.DORADO);
        abajo.add(lblMensaje, BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botones.setOpaque(false);
        JButton btnMenu = Estilo.boton("MENÚ PRINCIPAL");
        JButton btnGuardar = Estilo.boton("GUARDAR PARTIDA");
        btnSiguiente = Estilo.boton("SIGUIENTE BATALLA");
        btnMenu.addActionListener(e -> controlador.volverAlInicio());
        btnGuardar.addActionListener(e -> controlador.guardarPartida());
        btnSiguiente.addActionListener(e -> controlador.siguienteBatalla());
        botones.add(btnMenu);
        botones.add(btnGuardar);
        botones.add(btnSiguiente);
        abajo.add(botones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
    }

    private JPanel crearPanelInventario() {
        JPanel panel = Estilo.tarjeta();
        panel.setLayout(new BorderLayout(0, 10));
        panel.add(Estilo.etiqueta("Inventario", 20, Estilo.DORADO), BorderLayout.NORTH);

        panelItems = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelItems.setOpaque(false);
        panel.add(panelItems, BorderLayout.CENTER);

        JPanel usar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        usar.setOpaque(false);
        comboItems = new JComboBox<>();
        comboPersonajes = new JComboBox<>();
        comboItems.setPreferredSize(new Dimension(220, 30));
        comboPersonajes.setPreferredSize(new Dimension(180, 30));
        JButton btnUsar = Estilo.boton("USAR / EQUIPAR");
        btnUsar.addActionListener(e -> usarItemSeleccionado());
        usar.add(Estilo.etiqueta("Usar", 16, Estilo.TEXTO));
        usar.add(comboItems);
        usar.add(Estilo.etiqueta("en", 16, Estilo.TEXTO));
        usar.add(comboPersonajes);
        usar.add(btnUsar);
        panel.add(usar, BorderLayout.SOUTH);
        return panel;
    }

    /** Vuelve a dibujar la pantalla con los datos actuales de la partida. */
    public void actualizar(PartidaDTO partida) {
        lblMensaje.setText(" ");
        int batalla = Math.min(partida.getNumeroBatallaActual(), partida.getCantidadBatallas());
        lblInfo.setText("Oro: " + partida.getOro() + "   |   Batalla "
                + batalla + " / " + partida.getCantidadBatallas());
        btnSiguiente.setEnabled(!partida.isCompleta());

        panelPersonajes.removeAll();
        personajesDelCombo.clear();
        comboPersonajes.removeAllItems();
        for (PersonajeDTO p : partida.getPersonajes()) {
            panelPersonajes.add(crearTarjetaPersonaje(p));
            personajesDelCombo.add(p);
            comboPersonajes.addItem(p.getNombre());
        }

        panelItems.removeAll();
        itemsDelCombo.clear();
        comboItems.removeAllItems();
        List<ItemDTO> distintos = partida.getItems();
        if (distintos.isEmpty()) {
            panelItems.add(Estilo.etiqueta("(vacío)", 15, Estilo.TEXTO_SUAVE));
        }
        for (ItemDTO item : distintos) {
            int cantidad = item.getCantidad();
            JLabel chip = Estilo.etiqueta(item.getNombre().toUpperCase() + " x" + cantidad, 14,
                    item.isEquipable() ? Estilo.TEXTO : Estilo.DORADO);
            chip.setToolTipText(item.getDescripcion());
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Estilo.DORADO),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
            panelItems.add(chip);
            itemsDelCombo.add(item);
            comboItems.addItem(item.getNombre() + " (x" + cantidad + ")" + (item.isEquipable() ? " · equipable" : ""));
        }
        revalidate();
        repaint();
    }

    private JPanel crearTarjetaPersonaje(PersonajeDTO p) {
        JPanel t = Estilo.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        JLabel sprite = new JLabel(Sprites.icono(p, 96));
        sprite.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.add(sprite);
        t.add(espacio(6));
        agregarCentrado(t, Estilo.etiqueta(p.getNombre(), 24, Estilo.TEXTO));
        agregarCentrado(t, Estilo.etiqueta(p.getNombreClase() + " · Nvl. " + p.getNivel(), 15, Estilo.TEXTO_SUAVE));
        t.add(espacio(14));
        agregarBarra(t, "Vida", p.getVidaActual(), p.getVidaMaxima(), Estilo.VIDA);
        agregarBarra(t, Estilo.nombreRecurso(p.isUsaMana()), p.getRecursoActual(), p.getRecursoMaximo(),
                Estilo.colorRecurso(p.isUsaMana()));
        t.add(espacio(10));
        JPanel stats = new JPanel(new GridLayout(2, 2, 6, 4));
        stats.setOpaque(false);
        stats.add(Estilo.etiqueta("ATQ " + p.getAtaque() + conBonus(p.getBonusAtaque()), 15, Estilo.TEXTO_SUAVE));
        stats.add(Estilo.etiqueta("DEF " + p.getDefensa() + conBonus(p.getBonusDefensa()), 15, Estilo.TEXTO_SUAVE));
        stats.add(Estilo.etiqueta("VEL " + p.getVelocidad() + conBonus(p.getBonusVelocidad()), 15, Estilo.TEXTO_SUAVE));
        stats.add(Estilo.etiqueta("EXP " + p.getExperiencia() + "/" + p.getExperienciaParaSubir(), 15, Estilo.TEXTO_SUAVE));
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        t.add(stats);
        t.add(espacio(10));
        JLabel habilidades = Estilo.etiqueta("", 13, Estilo.DORADO);
        String texto = "<html>";
        for (int i = 0; i < p.getHabilidades().size(); i++) {
            texto = texto + "• " + p.getHabilidades().get(i).getNombre() + "<br>";
        }
        habilidades.setText(texto + "</html>");
        habilidades.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.add(habilidades);
        t.add(espacio(8));
        agregarEquipamiento(t, p);
        return t;
    }

    /** Lo que tiene equipado, con un botón para quitar cada cosa. */
    private void agregarEquipamiento(JPanel t, PersonajeDTO p) {
        agregarCentrado(t, Estilo.etiqueta("Equipo", 14, Estilo.TEXTO));
        if (p.getEquipamiento().isEmpty()) {
            agregarCentrado(t, Estilo.etiqueta("(nada equipado)", 13, Estilo.TEXTO_SUAVE));
        }
        for (EquipadoDTO e : p.getEquipamiento()) {
            JPanel fila = new JPanel(new BorderLayout(6, 0));
            fila.setOpaque(false);
            JLabel nombre = Estilo.etiqueta(e.getNombreRanura() + ": " + e.getNombre(), 13, Estilo.TEXTO_SUAVE);
            nombre.setToolTipText(e.getDescripcion());
            fila.add(nombre, BorderLayout.CENTER);
            JButton quitar = new JButton("x");
            quitar.setToolTipText("Quitar " + e.getNombre());
            quitar.setFont(Estilo.fuente(11));
            quitar.setMargin(new java.awt.Insets(0, 4, 0, 4));
            quitar.setFocusPainted(false);
            quitar.setForeground(Estilo.DORADO);
            quitar.setBackground(Estilo.BOTON);
            quitar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Estilo.BORDE), BorderFactory.createEmptyBorder(0, 5, 1, 5)));
            quitar.addActionListener(ev -> controlador.desequipar(p.getId(), e.getRanura()));
            fila.add(quitar, BorderLayout.EAST);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            t.add(fila);
        }
    }

    private String conBonus(int bonus) {
        if (bonus == 0) {
            return "";
        }
        return bonus > 0 ? " (+" + bonus + ")" : " (" + bonus + ")";
    }

    private void agregarBarra(JPanel t, String nombre, int valor, int maximo, java.awt.Color color) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(Estilo.etiqueta(nombre, 14, Estilo.TEXTO), BorderLayout.WEST);
        fila.add(Estilo.etiqueta(valor + "/" + maximo, 14, Estilo.TEXTO), BorderLayout.EAST);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        t.add(fila);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(Estilo.barra(valor, maximo, color));
        contenedor.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        t.add(contenedor);
        t.add(espacio(8));
    }

    private void agregarCentrado(JPanel panel, JLabel l) {
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(l);
    }

    private Component espacio(int alto) {
        return javax.swing.Box.createRigidArea(new Dimension(0, alto));
    }

    private void usarItemSeleccionado() {
        int i = comboItems.getSelectedIndex();
        int j = comboPersonajes.getSelectedIndex();
        if (i >= 0 && j >= 0) {
            controlador.usarItem(itemsDelCombo.get(i).getNombre(), personajesDelCombo.get(j).getId());
        }
    }

    public void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }

    public void setControlador(ControladorJuego controlador) {
        this.controlador = controlador;
    }
}
