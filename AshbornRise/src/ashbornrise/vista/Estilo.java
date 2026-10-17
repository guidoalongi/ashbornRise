package ashbornrise.vista;

import ashbornrise.modelo.TipoRecurso;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

/** Colores, fuentes y componentes con la estética del juego (fantasía oscura). */
public class Estilo {

    public static final Color FONDO = new Color(18, 14, 28);
    public static final Color PANEL = new Color(40, 31, 58);
    public static final Color PANEL_OSCURO = new Color(28, 22, 40);
    public static final Color BORDE = new Color(122, 98, 56);
    public static final Color DORADO = new Color(230, 192, 101);
    public static final Color TEXTO = new Color(232, 226, 208);
    public static final Color TEXTO_SUAVE = new Color(169, 159, 192);
    public static final Color VIDA = new Color(192, 57, 59);
    public static final Color MANA = new Color(58, 143, 208);
    public static final Color ENERGIA = new Color(212, 137, 42);
    public static final Color EXPERIENCIA = new Color(106, 191, 58);
    public static final Color ENEMIGO = new Color(70, 32, 36);
    public static final Color BOTON = new Color(64, 50, 30);

    public static Font fuente(int tamanio) {
        return new Font(Font.SERIF, Font.PLAIN, tamanio);
    }

    public static Font fuenteNegrita(int tamanio) {
        return new Font(Font.SERIF, Font.BOLD, tamanio);
    }

    public static JLabel etiqueta(String texto, int tamanio, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(tamanio));
        l.setForeground(color);
        return l;
    }

    public static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fuente(17));
        b.setForeground(DORADO);
        b.setBackground(BOTON);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DORADO),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JProgressBar barra(int valor, int maximo, Color color) {
        JProgressBar barra = new JProgressBar(0, Math.max(1, maximo));
        barra.setValue(valor);
        barra.setUI(new BasicProgressBarUI()); // para que el color se respete en todos los sistemas
        barra.setForeground(color);
        barra.setBackground(PANEL_OSCURO);
        barra.setBorder(BorderFactory.createLineBorder(new Color(80, 70, 100)));
        barra.setPreferredSize(new Dimension(150, 10));
        return barra;
    }

    public static Color colorRecurso(TipoRecurso tipo) {
        if (tipo == TipoRecurso.MANA) {
            return MANA;
        }
        return ENERGIA;
    }

    public static String nombreRecurso(TipoRecurso tipo) {
        if (tipo == TipoRecurso.MANA) {
            return "Maná";
        }
        return "Energía";
    }

    public static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        return p;
    }
}
