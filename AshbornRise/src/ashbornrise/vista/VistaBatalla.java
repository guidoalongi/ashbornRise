package ashbornrise.vista;

import ashbornrise.controlador.ControladorBatalla;
import ashbornrise.controlador.IVistaBatalla;
import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.EstadoBatalla;
import ashbornrise.modelo.Inventario;
import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.estados.EstadoEfecto;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.habilidades.TipoAlcance;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pantalla de batalla: arriba la arena 3D (donde se ven y se eligen los combatientes),
 * abajo el estado del equipo, el menú de acciones y el registro.
 * La vista solo guarda "qué está eligiendo el jugador" (por ejemplo: eligió Atacar y
 * ahora tiene que tocar un enemigo). Cuando la elección está completa, se la pasa al controlador.
 */
public class VistaBatalla extends JPanel implements IVistaBatalla {

    /** Qué está eligiendo el jugador en este momento. */
    private enum Modo {
        NINGUNO, ATACAR, ELEGIR_HABILIDAD, HABILIDAD_A_ENEMIGO, HABILIDAD_A_ALIADO, ELEGIR_ITEM, ITEM_A_ALIADO
    }

    private ControladorBatalla controlador;
    private Batalla batalla;
    private Modo modo = Modo.NINGUNO;
    private Habilidad habilidadElegida;
    private Item itemElegido;
    private boolean animando;
    private boolean finalMostrado;
    private int lineasEnRegistro;

    private JLabel lblRonda;
    private JLabel lblTurno;
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
    public void iniciarBatalla(Batalla batalla, Runnable alTerminar) {
        this.batalla = batalla;
        this.modo = Modo.NINGUNO;
        this.finalMostrado = false;
        arena.prepararBatalla(batalla);
        lineasEnRegistro = batalla.getRegistro().size();
        animando = true;
        refrescar();
        arena.animarEntrada(batalla.getNumeroBatalla(), () -> {
            animando = false;
            alTerminar.run();
        });
    }

    /** Anima el último turno ejecutado; al terminar avisa al controlador. */
    @Override
    public void animarTurno(Batalla batalla, Runnable alTerminar) {
        this.batalla = batalla;
        this.modo = Modo.NINGUNO;
        animando = true;
        bloquearEntrada();
        Combatiente actor = batalla.getUltimoActor();
        lblTurno.setText(actor != null ? "Turno de: " + actor.getNombre() : "");
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
    public void actualizarVista(Batalla batalla) {
        this.batalla = batalla;
        this.modo = Modo.NINGUNO;
        lineasEnRegistro = batalla.getRegistro().size();
        refrescar();
        if (!finalMostrado && batalla.getEstado() != EstadoBatalla.EN_CURSO) {
            finalMostrado = true;
            arena.animarFinal(batalla.getEstado() == EstadoBatalla.VICTORIA);
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
        arena.setActual(null);
        arena.setSeleccionables(Collections.<Combatiente>emptyList());
        panelOpciones.revalidate();
        panelOpciones.repaint();
    }

    private void refrescar() {
        boolean enCurso = batalla.getEstado() == EstadoBatalla.EN_CURSO;
        boolean turnoJugador = !animando && batalla.esTurnoDelJugador();
        Personaje actual = animando ? null : batalla.getPersonajeActual();

        lblRonda.setText("Ronda " + batalla.getRonda() + "   ·   Batalla " + batalla.getNumeroBatalla());
        lblTurno.setText(actual != null ? "Turno de: " + actual.getNombre() + " (" + actual.getNombreClase() + ")" : "");

        dibujarAliados(actual);
        dibujarRegistro();

        btnAtacar.setEnabled(turnoJugador);
        btnDefender.setEnabled(turnoJugador);
        btnHabilidad.setEnabled(turnoJugador);
        btnItem.setEnabled(turnoJugador);

        arena.setActual(animando ? null : batalla.getActorActual());
        arena.setSeleccionables(seleccionables());

        dibujarOpciones(actual);
        if (!animando && !enCurso) {
            lblInstruccion.setText(batalla.getEstado() == EstadoBatalla.VICTORIA
                    ? "¡Victoria! Todos los enemigos fueron derrotados." : "Tu equipo fue derrotado...");
            panelOpciones.add(btnContinuar);
        }
        revalidate();
        repaint();
    }

    /** Combatientes que se pueden tocar en la arena según lo que está eligiendo el jugador. */
    private List<Combatiente> seleccionables() {
        List<Combatiente> lista = new ArrayList<>();
        if (animando) {
            return lista;
        }
        if (modo == Modo.ATACAR || modo == Modo.HABILIDAD_A_ENEMIGO) {
            lista.addAll(batalla.getEnemigosVivos());
        } else if (modo == Modo.HABILIDAD_A_ALIADO || modo == Modo.ITEM_A_ALIADO) {
            lista.addAll(batalla.getAliadosVivos());
        }
        return lista;
    }

    private void dibujarAliados(Personaje actual) {
        panelAliados.removeAll();
        boolean seleccionable = modo == Modo.HABILIDAD_A_ALIADO || modo == Modo.ITEM_A_ALIADO;
        for (Personaje p : batalla.getAliados()) {
            JPanel carta = new JPanel(new BorderLayout(0, 2));
            carta.setOpaque(false);
            JButton b = new JButton("<html>" + p.getNombre() + "  <small>Nvl " + p.getNivel()
                    + "</small><br><small>Vida " + p.getVidaActual() + "/" + p.getVidaMaximo()
                    + "  ·  " + Estilo.nombreRecurso(p.getTipoRecurso()) + " " + p.getRecursoActual()
                    + "/" + p.getRecursoMaximo() + textoEstados(p) + "</small></html>");
            b.setFont(Estilo.fuente(15));
            b.setFocusPainted(false);
            b.setForeground(p.estaVivo() ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
            b.setBackground(Estilo.PANEL);
            b.setIcon(Sprites.icono(p, 40));
            b.setIconTextGap(8);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            boolean resaltar = (seleccionable && p.estaVivo()) || p == actual;
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(resaltar ? Estilo.DORADO : Estilo.BORDE, resaltar ? 3 : 1),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
            b.addActionListener(ev -> clicEnAliado(p));
            carta.add(b, BorderLayout.CENTER);
            JPanel barras = new JPanel(new GridLayout(2, 1, 0, 2));
            barras.setOpaque(false);
            barras.add(Estilo.barra(p.getVidaActual(), p.getVidaMaximo(), Estilo.VIDA));
            barras.add(Estilo.barra(p.getRecursoActual(), p.getRecursoMaximo(), Estilo.colorRecurso(p.getTipoRecurso())));
            carta.add(barras, BorderLayout.SOUTH);
            panelAliados.add(carta);
        }
    }

    private String textoEstados(Combatiente c) {
        if (!c.estaVivo()) {
            return "  ·  DERROTADO";
        }
        String texto = "";
        for (EstadoEfecto estado : c.getEstados()) {
            texto = texto + "  ·  " + estado.getNombre().toUpperCase();
        }
        if (c.estaDefendiendo()) {
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

    /** Debajo de los botones se muestran las habilidades o ítems para elegir. */
    private void dibujarOpciones(Personaje actual) {
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
                for (Habilidad h : actual.getHabilidades()) {
                    JButton b = Estilo.boton(h.getNombre() + " · " + h.getCostoRecurso() + " "
                            + Estilo.nombreRecurso(h.getTipoRecurso()));
                    b.setFont(Estilo.fuente(14));
                    if (!actual.puedeUsar(h)) {
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
                Inventario inventario = batalla.getInventario();
                List<Item> items = inventario.obtenerItemsDistintos();
                lblInstruccion.setText(items.isEmpty() ? "No hay ítems en el inventario." : "Elegí un ítem.");
                for (Item item : items) {
                    JButton b = Estilo.boton(item.getNombre() + " x" + inventario.cantidadDe(item.getNombre()));
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

    private void elegirHabilidad(Habilidad h) {
        habilidadElegida = h;
        TipoAlcance alcance = h.getAlcance();
        if (alcance == TipoAlcance.PROPIO || alcance == TipoAlcance.VARIOS_ENEMIGOS) {
            controlador.usarHabilidad(h, null); // no hace falta elegir objetivo
        } else if (controlador.puedeUsarHabilidad(h)) {
            cambiarModo(alcance == TipoAlcance.UN_ENEMIGO ? Modo.HABILIDAD_A_ENEMIGO : Modo.HABILIDAD_A_ALIADO);
        }
    }

    private void elegirItem(Item item) {
        itemElegido = item;
        cambiarModo(Modo.ITEM_A_ALIADO);
    }

    private void clicEnCombatiente(Combatiente c) {
        if (c instanceof Enemigo) {
            clicEnEnemigo((Enemigo) c);
        } else if (c instanceof Personaje) {
            clicEnAliado((Personaje) c);
        }
    }

    private void clicEnEnemigo(Enemigo e) {
        if (animando || !e.estaVivo()) {
            return;
        }
        if (modo == Modo.ATACAR) {
            controlador.atacar(e);
        } else if (modo == Modo.HABILIDAD_A_ENEMIGO) {
            controlador.usarHabilidad(habilidadElegida, e);
        }
    }

    private void clicEnAliado(Personaje p) {
        if (animando) {
            return;
        }
        if (modo == Modo.HABILIDAD_A_ALIADO) {
            controlador.usarHabilidad(habilidadElegida, p);
        } else if (modo == Modo.ITEM_A_ALIADO) {
            controlador.usarItem(itemElegido, p);
        }
    }

    public void setControlador(ControladorBatalla controlador) {
        this.controlador = controlador;
    }
}
