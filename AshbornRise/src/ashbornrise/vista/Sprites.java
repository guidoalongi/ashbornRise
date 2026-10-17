package ashbornrise.vista;

import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.enemigos.SenorDeLasSombras;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Sprites pixel-art de los combatientes. Están dibujados en código: cada fila es un texto
 * donde cada letra es un píxel de un color de la paleta ('.' es transparente).
 * Los personajes miran a la derecha; a los enemigos se los espeja para que miren al equipo.
 *
 * Si existe un archivo sprites/NombreDeLaClase.png (por ejemplo sprites/Mago.png) en la carpeta
 * desde donde se ejecuta el juego o en el classpath, se usa esa imagen en lugar del dibujo.
 */
public class Sprites {

    private static final Map<String, BufferedImage> BASES = new HashMap<>();
    private static final Map<String, ImageIcon> ICONOS = new HashMap<>();

    /** Ícono del combatiente con el alto pedido (en píxeles). El jefe final se ve más grande
     *  y los derrotados se ven grises. */
    public static ImageIcon icono(Combatiente c, int alto) {
        if (c instanceof SenorDeLasSombras) {
            alto = alto * 3 / 2;
        }
        String clase = c.getClass().getSimpleName();
        boolean espejar = c instanceof Enemigo;
        String clave = clase + "|" + alto + "|" + c.estaVivo();
        ImageIcon icono = ICONOS.get(clave);
        if (icono == null) {
            BufferedImage img = escalar(base(clase), alto, espejar);
            if (!c.estaVivo()) {
                img = apagar(img);
            }
            icono = new ImageIcon(img);
            ICONOS.put(clave, icono);
        }
        return icono;
    }

    /** Imagen original del sprite (sin escalar ni espejar), por ejemplo para armar el modelo 3D. */
    public static BufferedImage imagenBase(Combatiente c) {
        return base(c.getClass().getSimpleName());
    }

    private static BufferedImage base(String clase) {
        BufferedImage img = BASES.get(clase);
        if (img == null) {
            img = cargarArchivo(clase);
            if (img == null) {
                img = dibujar(clase);
            }
            BASES.put(clase, img);
        }
        return img;
    }

    private static BufferedImage cargarArchivo(String clase) {
        try {
            File archivo = new File("sprites", clase + ".png");
            if (archivo.isFile()) {
                return ImageIO.read(archivo);
            }
            InputStream in = Sprites.class.getResourceAsStream("/sprites/" + clase + ".png");
            if (in != null) {
                try (InputStream entrada = in) {
                    return ImageIO.read(entrada);
                }
            }
        } catch (Exception e) {
            // si la imagen no se puede leer, se usa el dibujo del código
        }
        return null;
    }

    /** Escala sin suavizar (para que el pixel art se vea nítido) y opcionalmente espeja. */
    private static BufferedImage escalar(BufferedImage img, int alto, boolean espejar) {
        int ancho = Math.max(1, img.getWidth() * alto / img.getHeight());
        BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = salida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if (espejar) {
            g.drawImage(img, ancho, 0, -ancho, alto, null);
        } else {
            g.drawImage(img, 0, 0, ancho, alto, null);
        }
        g.dispose();
        return salida;
    }

    /** Versión gris y semitransparente para los combatientes derrotados. */
    private static BufferedImage apagar(BufferedImage img) {
        BufferedImage salida = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) / 3;
                int gris = (((argb >> 16) & 255) + ((argb >> 8) & 255) + (argb & 255)) / 3;
                salida.setRGB(x, y, (a << 24) | (gris << 16) | (gris << 8) | gris);
            }
        }
        return salida;
    }

    /** Arma la imagen a partir de las filas de texto y la paleta. */
    private static BufferedImage pixeles(String[] filas, Object... paleta) {
        Map<Character, Color> colores = new HashMap<>();
        for (int i = 0; i < paleta.length; i += 2) {
            colores.put((Character) paleta[i], new Color((Integer) paleta[i + 1]));
        }
        int ancho = 0;
        for (String fila : filas) {
            ancho = Math.max(ancho, fila.length());
        }
        BufferedImage img = new BufferedImage(ancho, filas.length, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < filas.length; y++) {
            for (int x = 0; x < filas[y].length(); x++) {
                Color color = colores.get(filas[y].charAt(x));
                if (color != null) {
                    img.setRGB(x, y, color.getRGB());
                }
            }
        }
        return img;
    }

    /** Para sprites simétricos: se escribe la mitad izquierda y se completa espejándola. */
    private static String[] simetrico(String... mitades) {
        String[] filas = new String[mitades.length];
        for (int i = 0; i < mitades.length; i++) {
            filas[i] = mitades[i] + new StringBuilder(mitades[i]).reverse();
        }
        return filas;
    }

    private static BufferedImage dibujar(String clase) {
        switch (clase) {
            case "Guerrero":
                return guerrero();
            case "Mago":
                return mago();
            case "Arquero":
                return arquero();
            case "Curador":
                return curador();
            case "GoblinMerodeador":
                return goblin();
            case "EsqueletoGuerrero":
                return esqueleto();
            case "ChamanOscuro":
                return chaman();
            case "AranaVenenosa":
                return arana();
            case "SenorDeLasSombras":
                return senorDeLasSombras();
            default:
                return desconocido();
        }
    }

    // ---------------------------------------------------------------- personajes

    private static BufferedImage guerrero() {
        return pixeles(new String[]{
                "...........RR...........",
                "..........RRrR..........",
                "..........RrrK..........",
                "........KKKKKKK.........",
                ".......KWSSSSSsK........",
                "......KWSSSSSSSsK.......",
                "......KSSSSSSSSsK.......",
                "......KSSEEEEEEsK.......",
                "......KSSSSGSSSsK.....K.",
                "......KsSSSGSSssK....KWK",
                ".......KsssssssK....KWK.",
                "..KKKKKKGGGGGGKK...KWK..",
                ".KGGGGGKSWSSSSsK..KWK...",
                ".KGSSSGKSSSSSSsK.KWK....",
                ".KGSRSGKSSGGSSsKGGK.....",
                ".KGRRRGKSSSSSSKKBGK.....",
                ".KGSRSGKRRRRRRKBBK......",
                ".KGSSSGKrrrrrrKKKGK.....",
                "..KGSGKKsSSKSSsK.KK.....",
                "...KGK.KsSSKSSsK........",
                "....K..KsSSKSSsK........",
                ".......KBBK.KBBK........",
                "......KBBBK.KBBBK.......",
                "......KKKKK.KKKKK.......",
        },
                'K', 0x1a1220, 'S', 0xa9b3c4, 's', 0x6c7488, 'W', 0xe8ecf4, 'E', 0x101018,
                'R', 0xb8323a, 'r', 0x7a1e28, 'G', 0xe0b45a, 'B', 0x6e4628);
    }

    /** Inspirado en Veigar "Final Boss" (League of Legends): yordle de rostro en sombra, ojos brillantes,
     *  sombrero gigante y estética de jefe final de 8 bits. */
    private static BufferedImage mago() {
        return pixeles(new String[]{
                "..........K.............",
                ".........KLK............",
                ".........KLPK...........",
                "........KLPPK...........",
                "........KLPPpK..........",
                ".......KLPPPpK..........",
                ".......KLPMPPpK.........",
                "......KLPPPPPppK........",
                "......KLPPPPPPpK........",
                ".....KGGGGGMGGGGK...KKK.",
                "...KKPPPPPPPPPPPpKKKOOOK",
                ".KKLPPPPPPPPPPPPPpKOoWOK",
                "KppppppppppppppppppKOOOK",
                ".KKKKKVVVVVVVVVKKKK.KGK.",
                "......KVYyVVVYyK.....KBK",
                "......KVVVVVVVVK.....KBK",
                ".....KRGRRRRRRGRK....KBK",
                "....KRRGRRRRRRGRRK...KBK",
                "...KRRRGRRMRRRGRRRKKGGBK",
                "...KrRRGRRRRRRGRRRKKGGBK",
                "....KrrGGGGGGGGrrK...KBK",
                "....KrrrrrrrrrrrrK...KBK",
                ".....KGGK.KKK.KGGK...KBK",
                ".....KKKK.....KKKK....K.",
        },
                'K', 0x140a1e, 'P', 0x4b2a8c, 'p', 0x2e1a5c, 'L', 0x7a4fd0, 'G', 0xf0c040,
                'M', 0xff4fd8, 'V', 0x0c0614, 'Y', 0xfff36a, 'y', 0xffa020, 'R', 0x5a2a9a,
                'r', 0x3a1a6a, 'O', 0xff6ae0, 'o', 0xffc8f4, 'W', 0xffffff, 'B', 0x8a6a3a);
    }

    private static BufferedImage arquero() {
        return pixeles(new String[]{
                "........KKKK.......K....",
                ".......KHHHHK......KBK..",
                "......KHHHHHHK.....T.BK.",
                ".....KHHHHHHhhK....T..BK",
                ".....KHHFFFFHhK....T..BK",
                ".....KHFFFEFFhK....T...B",
                ".....KhFFFFFFhK....T...B",
                "....AKKhFFFFhKK....T...B",
                "....QQKKhhhhKK.....T...B",
                "....QKHHHHHHHHK....T...B",
                "....QKHHhHHHHhHKKFFFKKKB",
                "....QKHHhLLLLhHHFFFFF..B",
                "....QKHHhHHHHhHKKKK.T..B",
                "....QKHHhHHHHhK.....T..B",
                ".....KLLLLGLLLK.....T..B",
                ".....KhHHHHHHhK.....T.BK",
                ".....KhHHKKHHhK.....T.BK",
                "......KhHK.KhHK.....TBK.",
                "......KLLK.KLLK.....KBK.",
                "......KLLK.KLLK......K..",
                ".....KLLLK.KLLLK........",
                ".....KKKKK.KKKKK........",
        },
                'K', 0x1a1410, 'H', 0x3c8a3a, 'h', 0x24562a, 'F', 0xe6b48c, 'E', 0x2a1a10,
                'B', 0x8a5a2a, 'T', 0xe8e0c8, 'L', 0x6a4a2a, 'Q', 0xa07040, 'A', 0xc0c8d0,
                'G', 0xe0b45a);
    }

    private static BufferedImage curador() {
        return pixeles(new String[]{
                "...................LL...",
                "..................LllL..",
                "........KKKK......lGGl..",
                ".......KWWWWK.....LGGL..",
                "......KWWWWWWK.....GG...",
                "......KWFFFFWwK...KBBK..",
                "......KWFEFFEwK....KBK..",
                "......KwFFFFFwK....KBK..",
                ".......KwFFFwK.....KBK..",
                ".....KKWWGGGWWKK..KFBK..",
                "....KWWWWWGWWWWwKKFFBK..",
                "....KWWWWWGWWWWWwKKBK...",
                "...KWWWwWWGWWwWWwK.KBK..",
                "...KWWWwWWGWWwWWwK.KBK..",
                "...KFFKwWWGWWwKKK..KBK..",
                "....KK.KGGGGGGK....KBK..",
                ".......KWWWWWWwK...KBK..",
                "......KWWWWWWWWwK..KBK..",
                "......KWWWWWWWWwK..KBK..",
                ".....KWWWWWWWWWWwK.KBK..",
                ".....KGGGGGGGGGGGK.KBK..",
                ".....KKKKKKKKKKKKK..K...",
        },
                'K', 0x1e1828, 'W', 0xf0ece0, 'w', 0xbcb4a4, 'G', 0xe0b45a, 'F', 0xe6b48c,
                'E', 0x2a1a10, 'B', 0x8a5a2a, 'L', 0xfff4a0, 'l', 0xffd860);
    }

    // ---------------------------------------------------------------- enemigos

    private static BufferedImage goblin() {
        return pixeles(new String[]{
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........KKKKKKK.........",
                "KK....KKNNNNNNNKK....KK.",
                "KNKK.KNNNNNNNNNNNK.KKNK.",
                ".KNNKNNNRRNNNRRNNNKNNK..",
                "..KNNNNNRYNNNRYNNNNNK...",
                "...KKNNNNNNNNNNNNNKK....",
                ".....KNNnKKKKKnNNK......",
                ".....KNnKTKTKTKnNK...K..",
                "......KnnKKKKKnnK...KSK.",
                ".......KKnnnnnKK...KSK..",
                ".....KKBBBBBBBBKK.KSK...",
                "....KNKBbBBBBbBKNKSK....",
                "....KNKBBBbBBBBKNNKK....",
                "....KNKBBBBBbBBKKbK.....",
                ".....KKBbBBBBBBK.K......",
                "......KBBBKBBBBK........",
                "......KnnK.KnnK.........",
                ".....KNNNK.KNNNK........",
                ".....KKKKK.KKKKK........",
        },
                'K', 0x141a0e, 'N', 0x6ab040, 'n', 0x3e7a2a, 'R', 0xe03030, 'Y', 0xffe080,
                'T', 0xf0f0e0, 'B', 0x7a5230, 'b', 0x4e321e, 'S', 0xc0c8d0);
    }

    private static BufferedImage esqueleto() {
        return pixeles(new String[]{
                "........KKKKK...........",
                ".......KWWWWWK..........",
                "......KWWWWWWwK.........",
                "......KWEEWEEwK.........",
                "......KWERWERwK.........",
                "......KWWWKWWwK.........",
                ".......KwWWWwK......K...",
                ".......KWKWKWK.....KSK..",
                "........KKKKK.....KSK...",
                "...KKKKK.KWK.....KSK....",
                "..KUUUUUKKWKKK..KSK.....",
                "..KUuGuUKWWWWWKKSK......",
                "..KUGGGUKKWKWKKGK.......",
                "..KUuGuUKWWWWWKWWK......",
                "..KUuuuUKKWKWKKKK.......",
                "...KUuUK.KWWWK..........",
                "....KK...KwWwK..........",
                ".........KWKWK..........",
                "........KWK.KWK.........",
                "........KWK.KWK.........",
                "........KwK.KwK.........",
                ".......KWWK.KWWK........",
                ".......KKKK.KKKK........",
        },
                'K', 0x141010, 'W', 0xe8e4d4, 'w', 0xa8a494, 'E', 0x1a0a0a, 'R', 0xff3030,
                'S', 0x9aa0aa, 'U', 0x8a5a30, 'u', 0x5a3a1e, 'G', 0xb08a40);
    }

    private static BufferedImage chaman() {
        return pixeles(new String[]{
                "...................GG...",
                "..................GgGG..",
                "........KKKKK....GgGgG..",
                ".......KHHHHHK....KWWK..",
                "......KHHHHHHhK..KWEWEK.",
                ".....KHHKKKKKhhK.KWWWWK.",
                ".....KHKMMMMMKhK..KWKWK.",
                ".....KHKMEMMEKhK...KBK..",
                ".....KHKMMMMMKhK...KBK..",
                ".....KhHKMKMKHhK...KBK..",
                "....KHHhKKKKKhhHK..KBK..",
                "...KHHHhhHHHHhhHHKKFBK..",
                "...KHHHhHHGHHhHHHFFFFK..",
                "..KHHHhHHGGGHHhHKKKBK...",
                "..KHHHhHHHGHHHhHK.KBK...",
                "..KHHhHHHHHHHHhHK.KBK...",
                ".KHHHhHHHHHHHHhHHKKBK...",
                ".KHHhHHHHHHHHHHhHKKBK...",
                ".KhhhhhhhhhhhhhhhK.KBK..",
                "..KKKKKKKKKKKKKKK..KBK..",
                "...................KBK..",
                "...................KK...",
        },
                'K', 0x0e0a14, 'H', 0x3e2458, 'h', 0x261638, 'M', 0xd8d0b8, 'E', 0x40ff70,
                'B', 0x6a4a2a, 'W', 0xe8e4d4, 'G', 0x50ff80, 'g', 0x20a040, 'F', 0x7a9a5a);
    }

    private static BufferedImage arana() {
        return pixeles(new String[]{
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "..L..................L..",
                "...L....KKKKKKKK....L...",
                "...L..KKDDDDDDDDKK..L...",
                "L...LKDDLLDDDDDDDDKL...L",
                ".L...KDLDDVDDVDDDDK...L.",
                "..L.KDDDDDDVVDDDDDDK.L..",
                "...LKDDDDDVDDVDDDDDKL...",
                "LLLLKdDDDDDDDDDDDDdKLLLL",
                "....KKddDDDDDDDDddKK....",
                "..LLLLKKKKDDDDKKKKLLLL..",
                ".L...LKDDRDDDDRDDKL...L.",
                "L...L.KDRRDDDDRRDK.L...L",
                "...L..KDDDDDDDDDDK..L...",
                "..L....KdFKddKFdK....L..",
                ".L......KFK..KFK......L.",
                "L........K....K........L",
        },
                'K', 0x0c0812, 'D', 0x3a2a4a, 'd', 0x241830, 'L', 0x6a5484, 'V', 0x70e040,
                'R', 0xff3040, 'F', 0xe8e0d0);
    }

    private static BufferedImage senorDeLasSombras() {
        return pixeles(simetrico(
                "..KK............",
                ".KHHK...........",
                ".KHhK...........",
                "..KHhK..........",
                "..KHHhK...KKKKKK",
                "...KHHhKKKAAAAAA",
                "....KHHhKAALLAAA",
                ".....KHhKALAAAAA",
                "......KKAAAAAAAA",
                "......KAAKKKKKKK",
                "......KAKRRrKKKK",
                "......KaAKKKKKKK",
                ".......KaAAAAAAA",
                "...KKKKKKaaaaaaa",
                "..KLLLLAAKKKKKKK",
                ".KLLAAAAAKCAAAAA",
                ".KLAAAAAAKCAAAKG",
                ".KAAAAaAKCCAAKLG",
                ".KaAAAaAKCAAKKGG",
                "..KaaaKKCCAAKGGG",
                "...KLLKCCCAAAKKS",
                "...KLLKCCcAAAAKS",
                "....KKCCCcAAaAKS",
                "....KCCCcAAAaAKS",
                "...KCCCcKAAaaAKS",
                "...KCCcKKAAaAAKS",
                "..KCCCcKAAaaAAKS",
                "..KCCcKAAaKKaAKS",
                ".KCCCcKAAKPPKAKS",
                ".KCCcKKAAKPPKAKK",
                "KCCCcKLLLKPPKLLL",
                "KKKKKKKKKKPPKKKK"
        ),
                'K', 0x0a0610, 'A', 0x2e2640, 'a', 0x1a1426, 'L', 0x5a4c78, 'H', 0xc8b8a0,
                'h', 0x8a7a64, 'R', 0xff2a2a, 'r', 0xa01818, 'C', 0x6a1a7a, 'c', 0x3a0e46,
                'P', 0xb050ff, 'S', 0xc0c6d4, 'G', 0xc8a040);
    }

    private static BufferedImage desconocido() {
        return pixeles(simetrico(
                "....KKKK",
                "...KDDDD",
                "..KDDDDD",
                "..KDDKKD",
                "..KDDKRD",
                "..KDDDDD",
                "...KDDDD",
                "....KDDD",
                "...KDDDD",
                "..KDDDDD",
                "..KDDDDD",
                "..KKKKKK"
        ), 'K', 0x101010, 'D', 0x505060, 'R', 0xff4040);
    }
}
