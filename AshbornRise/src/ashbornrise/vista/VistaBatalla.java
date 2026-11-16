package ashbornrise.vista;

import ashbornrise.controlador.ControladorBatalla;
import ashbornrise.controlador.IVistaBatalla;
import ashbornrise.dto.AlcanceDTO;
import ashbornrise.dto.BatallaDTO;
import ashbornrise.dto.CombatienteDTO;
import ashbornrise.dto.HabilidadDTO;
import ashbornrise.dto.ItemDTO;
import ashbornrise.dto.PersonajeDTO;
import ashbornrise.vista.arena.PanelArena;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pantalla de batalla: arriba la arena 3D (donde se ven y se eligen los combatientes),
 * abajo el estado del equipo, el menú de acciones y el registro.
 *
 * Recibe una "foto" de la batalla (BatallaDTO) y nunca toca el modelo. Solo guarda "qué está
 * eligiendo el jugador" (por ejemplo: eligió Atacar y ahora tiene que tocar un enemigo). Cuando
 * la elección está completa, le pasa al controlador los ids / índices / nombres elegidos.
 * Las reglas (a quién se puede atacar, qué objetivos admite una habilidad) las valida el modelo:
 * acá solo se resaltan los objetivos razonables para que la interfaz sea cómoda.
 */
public class VistaBatalla extends JPanel implements IVistaBatalla {

    /** Qué está eligiendo el jugador en este momento. */
    private enum Modo {
        NINGUNO, ATACAR, ELEGIR_HABILIDAD, HABILIDAD_A_ENEMIGO, HABILIDAD_A_ALIADO, ELEGIR_ITEM, ITEM_A_ALIADO
    }

    private ControladorBatalla controlador;
    private BatallaDTO batalla;
    private Modo modo = Modo.NINGUNO;
    private HabilidadDTO habilidadElegida;
    private ItemDTO itemElegido;
    private boolean animando;
    private boolean finalMostrado;
    private int lineasEnRegistro;

    private JLabel lblRonda;
    private JLabel lblTurno;
    private JPanel panelOrden;
    private JLabel lblInstruccion;
    private PanelArena arena;
    private JPanel panelAliados;
    private JPanel panelOpciones;
    private JTextArea txtRegistro;
    private JButton btnAtacar;
    private JButton btnDefender;
    private JButton btnHabilidad;
    private JButton btnItem;
    private JButton btnContinuar;

    public VistaBatalla() {
        setBackground(Estilo.FONDO);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 12, 20));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        lblRonda = Estilo.etiqueta("Ronda 1", 26, Estilo.DORADO);
        lblTurno = Estilo.etiqueta("", 17, Estilo.TEXTO_SUAVE);
        encabezado.add(lblRonda, BorderLayout.WEST);
        encabezado.add(lblTurno, BorderLayout.EAST);
        // Debajo de la ronda: la fila con el orden de turnos (quién actúa y quién viene después).
        panelOrden = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelOrden.setOpaque(false);
        encabezado.add(panelOrden, BorderLayout.SOUTH);
        add(encabezado, BorderLayout.NORTH);

        arena = new PanelArena();
        arena.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        arena.setAlHacerClic(this::clicEnCombatiente);
        add(arena, BorderLayout.CENTER);

        add(crearPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelInferior() {
        JPanel sur = new JPanel(new BorderLayout(0, 8));
        sur.setOpaque(false);

        panelAliados = new JPanel(new GridLayout(1, 4, 10, 0));
        panelAliados.setOpaque(false);
        sur.add(panelAliados, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(1, 4, 10, 0));
        botones.setOpaque(false);
        btnAtacar = Estilo.boton("ATACAR");
        btnDefender = Estilo.boton("DEFENDER");
        btnHabilidad = Estilo.boton("HABILIDAD");
        btnItem = Estilo.boton("ÍTEM");
        btnAtacar.addActionListener(e -> cambiarModo(Modo.ATACAR));
        btnDefender.addActionListener(e -> controlador.defender());
        btnHabilidad.addActionListener(e -> cambiarModo(Modo.ELEGIR_HABILIDAD));
        btnItem.addActionListener(e -> cambiarModo(Modo.ELEGIR_ITEM));
        botones.add(btnAtacar);
        botones.add(btnDefender);
        botones.add(btnHabilidad);
        botones.add(btnItem);
        sur.add(botones, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(14, 0));
        abajo.setOpaque(false);
        JPanel izquierda = new JPanel(new BorderLayout(0, 4));
        izquierda.setOpaque(false);
        lblInstruccion = Estilo.etiqueta(" ", 16, Estilo.DORADO);
        izquierda.add(lblInstruccion, BorderLayout.NORTH);
        panelOpciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelOpciones.setOpaque(false);
        izquierda.add(panelOpciones, BorderLayout.CENTER);
        abajo.add(izquierda, BorderLayout.CENTER);

        txtRegistro = new JTextArea(3, 36);
        txtRegistro.setEditable(false);
        txtRegistro.setLineWrap(true);
        txtRegistro.setWrapStyleWord(true);
        txtRegistro.setFont(Estilo.fuente(13));
        txtRegistro.setBackground(Estilo.PANEL);
        txtRegistro.setForeground(Estilo.TEXTO);
        txtRegistro.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JScrollPane scroll = new JScrollPane(txtRegistro);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scroll.setPreferredSize(new Dimension(440, 74));
        abajo.add(scroll, BorderLayout.EAST);
        sur.add(abajo, BorderLayout.SOUTH);

        btnContinuar = Estilo.boton("CONTINUAR");
        btnContinuar.addActionListener(e -> controlador.continuar());
        return sur;
    }

    // ---------------------------------------------------------------- lo que llama el controlador

    /** Arma la arena con los combatientes y reproduce la entrada. */
    @Override
    public void iniciarBatalla(BatallaDTO batalla, Runnable alTerminar) {
        this.batalla = batalla;
        this.modo = Modo.NINGUNO;
        this.finalMostrado = false;
        arena.prepararBatalla(batalla);
        lineasEnRegistro = batalla.getRegistro().size();
        animando = true;
        refrescar();
        arena.animarEntrada(batalla.getNumero(), () -> {
            animando = false;
            alTerminar.run();
        });
    }

    /** Anima el último turno ejecutado; al terminar avisa al controlador. */
    @Override
    public void animarTurno(BatallaDTO batalla, Runnable alTerminar) {
        this.batalla = batalla;
        this.modo = Modo.NINGUNO;
        animando = true;
        bloquearEntrada();
        CombatienteDTO actor = batalla.buscar(batalla.getUltimoActorId());
        lblTurno.setText(actor != null ? "Turno de: " + actor.getNombre() : "");
        dibujarOrdenTurnos();
        List<String> registro = batalla.getRegistro();
        int hasta = registro.size();
        List<String> nuevas = new ArrayList<>(registro.subList(Math.min(lineasEnRegistro, hasta), hasta));
        arena.animarTurno(batalla, nuevas, () -> {
            animando = false;
            lineasEnRegistro = hasta;
            dibujarRegistro();
            dibujarAliados(null);
            panelAliados.revalidate();
            panelAliados.repaint();
            alTerminar.run();
        });
    }

    /** Refresca la pantalla cuando le toca elegir al jugador o terminó la batalla. */
    @Override
    public void actualizarVista(BatallaDTO batalla) {
        this.batalla = batalla;
        this.modo = Modo.NINGUNO;
        lineasEnRegistro = batalla.getRegistro().size();
        arena.actualizarCombatientes(batalla);
        refrescar();
        if (!finalMostrado && !batalla.isEnCurso()) {
            finalMostrado = true;
            arena.animarFinal(batalla.isVictoria());
        }
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Acción no válida", JOptionPane.WARNING_MESSAGE);
    }

    // ---------------------------------------------------------------- dibujo

    private void cambiarModo(Modo nuevo) {
        if (animando) {
            return;
        }
        modo = nuevo;
        refrescar();
    }

    private void bloquearEntrada() {
        btnAtacar.setEnabled(false);
        btnDefender.setEnabled(false);
        btnHabilidad.setEnabled(false);
        btnItem.setEnabled(false);
        panelOpciones.removeAll();
        lblInstruccion.setText(" ");
        arena.setActual(-1);
        arena.setSeleccionables(Collections.<CombatienteDTO>emptyList());
        panelOpciones.revalidate();
        panelOpciones.repaint();
    }

    private void refrescar() {
        boolean enCurso = batalla.isEnCurso();
        boolean turnoJugador = !animando && batalla.isTurnoDelJugador();
        PersonajeDTO actual = animando ? null : batalla.getPersonajeActual();

        lblRonda.setText("Ronda " + batalla.getRonda() + "   ·   Batalla " + batalla.getNumero());
        lblTurno.setText(actual != null ? "Turno de: " + actual.getNombre() + " (" + actual.getNombreClase() + ")" : "");

        dibujarAliados(actual);
        dibujarRegistro();
        dibujarOrdenTurnos();

        btnAtacar.setEnabled(turnoJugador);
        btnDefender.setEnabled(turnoJugador);
        btnHabilidad.setEnabled(turnoJugador);
        btnItem.setEnabled(turnoJugador);

        arena.setActual(animando ? -1 : batalla.getActorActualId());
        arena.setSeleccionables(seleccionables());

        dibujarOpciones(actual);
        if (!animando && !enCurso) {
            lblInstruccion.setText(batalla.isVictoria()
                    ? "¡Victoria! Todos los enemigos fueron derrotados." : "Tu equipo fue derrotado...");
            panelOpciones.add(btnContinuar);
        }
        revalidate();
        repaint();
    }

    /** Combatientes que se pueden tocar en la arena según lo que está eligiendo el jugador (solo ayuda visual). */
    private List<CombatienteDTO> seleccionables() {
        List<CombatienteDTO> lista = new ArrayList<>();
        if (animando) {
            return lista;
        }
        if (modo == Modo.ATACAR || modo == Modo.HABILIDAD_A_ENEMIGO) {
            for (CombatienteDTO e : batalla.getEnemigos()) {
                if (e.isVivo()) {
                    lista.add(e);
                }
            }
        } else if (modo == Modo.HABILIDAD_A_ALIADO || modo == Modo.ITEM_A_ALIADO) {
            for (PersonajeDTO p : batalla.getAliados()) {
                if (p.isVivo()) {
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    private void dibujarAliados(PersonajeDTO actual) {
        panelAliados.removeAll();
        boolean seleccionable = modo == Modo.HABILIDAD_A_ALIADO || modo == Modo.ITEM_A_ALIADO;
        for (PersonajeDTO p : batalla.getAliados()) {
            JPanel carta = new JPanel(new BorderLayout(0, 2));
            carta.setOpaque(false);
            JButton b = new JButton("<html>" + p.getNombre() + "  <small>Nvl " + p.getNivel()
                    + "</small><br><small>Vida " + p.getVidaActual() + "/" + p.getVidaMaxima()
                    + "  ·  " + Estilo.nombreRecurso(p.isUsaMana()) + " " + p.getRecursoActual()
                    + "/" + p.getRecursoMaximo() + textoEstados(p) + "</small></html>");
            b.setFont(Estilo.fuente(15));
            b.setFocusPainted(false);
            b.setForeground(p.isVivo() ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
            b.setBackground(Estilo.PANEL);
            b.setIcon(Sprites.icono(p, 40));
            b.setIconTextGap(8);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            boolean resaltar = (seleccionable && p.isVivo()) || (actual != null && p.getId() == actual.getId());
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(resaltar ? Estilo.DORADO : Estilo.BORDE, resaltar ? 3 : 1),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
            b.addActionListener(ev -> clicEnAliado(p));
            carta.add(b, BorderLayout.CENTER);
            JPanel barras = new JPanel(new GridLayout(2, 1, 0, 2));
            barras.setOpaque(false);
            barras.add(Estilo.barra(p.getVidaActual(), p.getVidaMaxima(), Estilo.VIDA));
            barras.add(Estilo.barra(p.getRecursoActual(), p.getRecursoMaximo(), Estilo.colorRecurso(p.isUsaMana())));
            carta.add(barras, BorderLayout.SOUTH);
            panelAliados.add(carta);
        }
    }

    private String textoEstados(CombatienteDTO c) {
        if (!c.isVivo()) {
            return "  ·  DERROTADO";
        }
        String texto = "";
        for (String estado : c.getEstados()) {
            texto = texto + "  ·  " + estado.toUpperCase();
        }
        if (c.isDefendiendo()) {
            texto = texto + "  ·  EN GUARDIA";
        }
        return texto;
    }

    private void dibujarRegistro() {
        List<String> registro = batalla.getRegistro();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lineasEnRegistro, registro.size()); i++) {
            sb.append(registro.get(i)).append("\n");
        }
        txtRegistro.setText(sb.toString());
        txtRegistro.setCaretPosition(txtRegistro.getDocument().getLength());
    }

    /**
     * Fila con el orden de turnos: primero quien está actuando (resaltado en dorado)
     * y después los que vienen en esta ronda. Si se está animando un turno, el que
     * actúa es el último actor; si no, el que tiene el turno ahora.
     * Al terminar la ronda la cola se vuelve a armar, por eso solo se muestra la ronda actual.
     */
    private void dibujarOrdenTurnos() {
        panelOrden.removeAll();
        List<CombatienteDTO> orden = new ArrayList<>();
        if (batalla.isEnCurso()) {
            if (animando) {
                agregarSiExiste(orden, batalla.getUltimoActorId());
            }
            agregarSiExiste(orden, batalla.getActorActualId());
            for (int id : batalla.getProximosTurnos()) {
                agregarSiExiste(orden, id);
            }
        }
        if (!orden.isEmpty()) {
            panelOrden.add(Estilo.etiqueta("Orden de turnos:", 14, Estilo.TEXTO_SUAVE));
        }
        for (int i = 0; i < orden.size(); i++) {
            if (i > 0) {
                panelOrden.add(Estilo.etiqueta("›", 18, Estilo.TEXTO_SUAVE));
            }
            panelOrden.add(fichaDeTurno(orden.get(i), i == 0));
        }
        panelOrden.revalidate();
        panelOrden.repaint();
    }

    private void agregarSiExiste(List<CombatienteDTO> orden, int id) {
        CombatienteDTO c = batalla.buscar(id);
        if (c != null) {
            orden.add(c);
        }
    }

    /** Ficha chica con el ícono y el nombre. Los enemigos tienen fondo rojizo; el que actúa, borde dorado. */
    private JLabel fichaDeTurno(CombatienteDTO c, boolean actuando) {
        JLabel ficha = new JLabel(c.getNombre(), Sprites.icono(c, 24), SwingConstants.LEFT);
        ficha.setFont(actuando ? Estilo.fuenteNegrita(13) : Estilo.fuente(13));
        ficha.setForeground(actuando ? Estilo.DORADO : Estilo.TEXTO);
        ficha.setOpaque(true);
        ficha.setBackground(c.isEnemigo() ? Estilo.ENEMIGO : Estilo.PANEL);
        ficha.setIconTextGap(5);
        ficha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(actuando ? Estilo.DORADO : Estilo.BORDE, actuando ? 2 : 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 8)));
        return ficha;
    }

    /** Debajo de los botones se muestran las habilidades o ítems para elegir. */
    private void dibujarOpciones(PersonajeDTO actual) {
        panelOpciones.removeAll();
        lblInstruccion.setText(" ");
        if (actual == null) {
            return;
        }
        switch (modo) {
            case ATACAR:
                lblInstruccion.setText("Elegí un enemigo en la arena para atacar.");
                break;
            case ELEGIR_HABILIDAD:
                lblInstruccion.setText("Elegí una habilidad.");
                for (HabilidadDTO h : actual.getHabilidades()) {
                    JButton b = Estilo.boton(h.getNombre() + " · " + h.getCosto() + " "
                            + Estilo.nombreRecurso(h.isUsaMana()));
                    b.setFont(Estilo.fuente(14));
                    if (!h.isUsable()) {
                        b.setForeground(Estilo.TEXTO_SUAVE);
                    }
                    b.addActionListener(e -> elegirHabilidad(h));
                    panelOpciones.add(b);
                }
                break;
            case HABILIDAD_A_ENEMIGO:
                lblInstruccion.setText(habilidadElegida.getNombre() + ": elegí un enemigo en la arena.");
                break;
            case HABILIDAD_A_ALIADO:
                lblInstruccion.setText(habilidadElegida.getNombre() + ": elegí un aliado (en la arena o en su tarjeta).");
                break;
            case ELEGIR_ITEM:
                List<ItemDTO> items = new ArrayList<>();
                for (ItemDTO item : batalla.getItems()) {
                    if (item.isUsableEnBatalla()) {
                        items.add(item);
                    }
                }
                lblInstruccion.setText(items.isEmpty() ? "No hay ítems para usar en batalla." : "Elegí un ítem.");
                for (ItemDTO item : items) {
                    JButton b = Estilo.boton(item.getNombre() + " x" + item.getCantidad());
                    b.setFont(Estilo.fuente(14));
                    b.setToolTipText(item.getDescripcion());
                    b.addActionListener(e -> elegirItem(item));
                    panelOpciones.add(b);
                }
                break;
            case ITEM_A_ALIADO:
                lblInstruccion.setText(itemElegido.getNombre() + ": elegí un aliado (en la arena o en su tarjeta).");
                break;
            default:
                lblInstruccion.setText("¿Qué hace " + actual.getNombre() + "?");
                return;
        }
        JButton cancelar = Estilo.boton("Cancelar");
        cancelar.setFont(Estilo.fuente(14));
        cancelar.addActionListener(e -> cambiarModo(Modo.NINGUNO));
        panelOpciones.add(cancelar);
    }

    // ---------------------------------------------------------------- clics

    private void elegirHabilidad(HabilidadDTO h) {
        habilidadElegida = h;
        AlcanceDTO alcance = h.getAlcance();
        if (alcance == AlcanceDTO.PROPIO || alcance == AlcanceDTO.VARIOS_ENEMIGOS) {
            controlador.usarHabilidad(h.getIndice(), -1); // no hace falta elegir objetivo
        } else if (controlador.puedeUsarHabilidad(h.getIndice())) {
            cambiarModo(alcance == AlcanceDTO.UN_ENEMIGO ? Modo.HABILIDAD_A_ENEMIGO : Modo.HABILIDAD_A_ALIADO);
        }
    }

    private void elegirItem(ItemDTO item) {
        itemElegido = item;
        cambiarModo(Modo.ITEM_A_ALIADO);
    }

    private void clicEnCombatiente(CombatienteDTO c) {
        if (animando) {
            return;
        }
        if (modo == Modo.ATACAR) {
            controlador.atacar(c.getId());
        } else if (modo == Modo.HABILIDAD_A_ENEMIGO || modo == Modo.HABILIDAD_A_ALIADO) {
            controlador.usarHabilidad(habilidadElegida.getIndice(), c.getId());
        } else if (modo == Modo.ITEM_A_ALIADO) {
            controlador.usarItem(itemElegido.getNombre(), c.getId());
        }
    }

    private void clicEnAliado(PersonajeDTO p) {
        if (modo == Modo.HABILIDAD_A_ALIADO || modo == Modo.ITEM_A_ALIADO) {
            clicEnCombatiente(p);
        }
    }

    public void setControlador(ControladorBatalla controlador) {
        this.controlador = controlador;
    }
}
