package ashbornrise.persistencia;

import ashbornrise.modelo.Equipo;
import ashbornrise.modelo.FabricaJuego;
import ashbornrise.modelo.Inventario;
import ashbornrise.modelo.Partida;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda la partida en un archivo con formato JSON, sin librerías externas.
 *
 * Se guarda solo el estado básico: progreso, oro, ítems (por nombre) y, por cada
 * personaje, su clase, nombre, nivel, experiencia, vida y recurso actuales.
 * Las estadísticas (ataque, defensa, etc.) no se guardan: al cargar, el personaje
 * se crea en nivel 1 y se sube de nivel hasta el nivel guardado, lo que da los mismos valores.
 *
 * Para que la lectura sea simple, el archivo siempre se escribe con un dato por línea
 * (y cada personaje en una sola línea). El lector entiende ese formato.
 */
public class PersistenciaArchivoJSON implements IPersistencia {

    @Override
    public void guardar(Partida p, String ruta) throws IOException {
        String json = serializar(p);
        escribirArchivo(json, ruta);
    }

    @Override
    public Partida cargar(String ruta) throws IOException {
        Path archivo = Paths.get(ruta);
        if (!Files.exists(archivo)) {
            throw new IOException("No existe una partida guardada en " + ruta);
        }
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        try {
            return deserializar(lineas);
        } catch (RuntimeException e) {
            throw new IOException("El archivo de guardado está dañado: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- escritura

    private String serializar(Partida p) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"fechaGuardado\": \"").append(p.getFechaGuardado()).append("\",\n");
        sb.append("  \"progresoBatallas\": ").append(p.getProgresoBatallas()).append(",\n");
        sb.append("  \"oro\": ").append(p.getInventario().getOro()).append(",\n");

        sb.append("  \"items\": [");
        List<Item> items = p.getInventario().obtenerItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(items.get(i).getNombre()).append("\"");
        }
        sb.append("],\n");

        sb.append("  \"personajes\": [\n");
        List<Personaje> personajes = p.getEquipo().getIntegrantes();
        for (int i = 0; i < personajes.size(); i++) {
            Personaje pj = personajes.get(i);
            sb.append("    {")
                    .append("\"clase\": \"").append(pj.getNombreClase()).append("\", ")
                    .append("\"nombre\": \"").append(pj.getNombre()).append("\", ")
                    .append("\"nivel\": ").append(pj.getNivel()).append(", ")
                    .append("\"experiencia\": ").append(pj.getExperiencia()).append(", ")
                    .append("\"vidaActual\": ").append(pj.getVidaActual()).append(", ")
                    .append("\"recursoActual\": ").append(pj.getRecursoActual())
                    .append("}");
            if (i < personajes.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private void escribirArchivo(String json, String ruta) throws IOException {
        Files.write(Paths.get(ruta), json.getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------- lectura

    private Partida deserializar(List<String> lineas) {
        String fecha = "";
        int progreso = 0;
        Inventario inventario = new Inventario();
        Equipo equipo = new Equipo();

        for (String linea : lineas) {
            if (linea.contains("\"clase\"")) {
                equipo.agregarIntegrante(leerPersonaje(linea));
            } else if (linea.contains("\"fechaGuardado\"")) {
                fecha = leerTexto(linea, "fechaGuardado");
            } else if (linea.contains("\"progresoBatallas\"")) {
                progreso = leerEntero(linea, "progresoBatallas");
            } else if (linea.contains("\"oro\"")) {
                inventario.agregarOro(leerEntero(linea, "oro"));
            } else if (linea.contains("\"items\"")) {
                for (String nombre : leerListaDeTextos(linea)) {
                    inventario.agregarItem(FabricaJuego.crearConsumible(nombre));
                }
            }
        }
        if (!equipo.esValido()) {
            throw new IllegalStateException("el equipo guardado no es válido");
        }
        Partida partida = new Partida(equipo, inventario, progreso);
        partida.setFechaGuardado(fecha);
        return partida;
    }

    private Personaje leerPersonaje(String linea) {
        Personaje p = FabricaJuego.crearPersonaje(leerTexto(linea, "clase"), leerTexto(linea, "nombre"));
        p.restaurarProgreso(
                leerEntero(linea, "nivel"),
                leerEntero(linea, "experiencia"),
                leerEntero(linea, "vidaActual"),
                leerEntero(linea, "recursoActual"));
        return p;
    }

    /** Devuelve lo que viene después de "clave": hasta la próxima coma o llave. */
    private String leerValor(String linea, String clave) {
        String buscado = "\"" + clave + "\":";
        int inicio = linea.indexOf(buscado);
        if (inicio < 0) {
            throw new IllegalStateException("falta el dato " + clave);
        }
        inicio = inicio + buscado.length();
        String resto = linea.substring(inicio).trim();
        if (resto.startsWith("\"")) {
            int fin = resto.indexOf('"', 1);
            return resto.substring(0, fin + 1);
        }
        int fin = 0;
        while (fin < resto.length() && resto.charAt(fin) != ',' && resto.charAt(fin) != '}') {
            fin++;
        }
        return resto.substring(0, fin).trim();
    }

    private String leerTexto(String linea, String clave) {
        String valor = leerValor(linea, clave);
        return valor.substring(1, valor.length() - 1); // saca las comillas
    }

    private int leerEntero(String linea, String clave) {
        return Integer.parseInt(leerValor(linea, clave));
    }

    private List<String> leerListaDeTextos(String linea) {
        List<String> textos = new ArrayList<>();
        int inicio = linea.indexOf('[');
        int fin = linea.lastIndexOf(']');
        String contenido = linea.substring(inicio + 1, fin).trim();
        if (contenido.isEmpty()) {
            return textos;
        }
        for (String parte : contenido.split(",")) {
            String limpio = parte.trim();
            textos.add(limpio.substring(1, limpio.length() - 1));
        }
        return textos;
    }
}
