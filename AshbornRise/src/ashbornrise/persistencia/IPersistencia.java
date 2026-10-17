package ashbornrise.persistencia;

import ashbornrise.modelo.Partida;

import java.io.IOException;

/**
 * Variación Protegida: el resto del sistema guarda y carga a través de esta interfaz,
 * sin saber si por detrás hay un archivo JSON, un .txt o una base de datos.
 */
public interface IPersistencia {

    void guardar(Partida p, String ruta) throws IOException;

    Partida cargar(String ruta) throws IOException;
}
