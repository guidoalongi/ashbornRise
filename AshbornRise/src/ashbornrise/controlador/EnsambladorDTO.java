package ashbornrise.controlador;

import ashbornrise.dto.AlcanceDTO;
import ashbornrise.dto.BatallaDTO;
import ashbornrise.dto.CombatienteDTO;
import ashbornrise.dto.EfectoDTO;
import ashbornrise.dto.EquipadoDTO;
import ashbornrise.dto.HabilidadDTO;
import ashbornrise.dto.ItemDTO;
import ashbornrise.dto.PartidaDTO;
import ashbornrise.dto.PersonajeDTO;
import ashbornrise.dto.RecompensaDTO;
import ashbornrise.dto.TipoAccionDTO;
import ashbornrise.dto.TurnoDTO;
import ashbornrise.modelo.Batalla;
import ashbornrise.modelo.Combatiente;
import ashbornrise.modelo.EstadoBatalla;
import ashbornrise.modelo.FabricaJuego;
import ashbornrise.modelo.Inventario;
import ashbornrise.modelo.Partida;
import ashbornrise.modelo.Recompensa;
import ashbornrise.modelo.TipoRecurso;
import ashbornrise.modelo.acciones.Accion;
import ashbornrise.modelo.enemigos.Enemigo;
import ashbornrise.modelo.estados.EstadoEfecto;
import ashbornrise.modelo.estados.TipoEstado;
import ashbornrise.modelo.habilidades.Habilidad;
import ashbornrise.modelo.items.Equipable;
import ashbornrise.modelo.items.Item;
import ashbornrise.modelo.personajes.Personaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Arma los DTO que reciben las vistas a partir de los objetos del modelo (Fabricación Pura).
 *
 * Es el único lugar donde se "traduce" el modelo para la vista. Así las vistas y las interfaces
 * IVistaBatalla / IVentanaJuego no conocen ninguna clase del modelo.
 *
 * Identificadores:
 *  - En batalla, el id de un combatiente es su posición en batalla.getCombatientes()
 *    (primero los aliados, después los enemigos; el orden no cambia durante la batalla).
 *  - Fuera de batalla, el id de un personaje es su posición en el equipo.
 *  - Las habilidades se identifican por su posición en la lista del personaje, y los ítems por su nombre.
 * Los controladores usan esos mismos datos para volver del id al objeto del modelo.
 */
public class EnsambladorDTO {

    // ---------------------------------------------------------------- partida

    public static PartidaDTO partida(Partida p) {
        PartidaDTO dto = new PartidaDTO();
        dto.setOro(p.getInventario().getOro());
        dto.setNumeroBatallaActual(p.getNumeroBatallaActual());
        dto.setCantidadBatallas(FabricaJuego.getCantidadBatallas());
        dto.setCompleta(p.estaCompleta());
        dto.setFechaGuardado(p.getFechaGuardado());
        List<Personaje> integrantes = p.getEquipo().getIntegrantes();
        for (int i = 0; i < integrantes.size(); i++) {
            dto.getPersonajes().add(personaje(integrantes.get(i), i));
        }
        dto.setItems(items(p.getInventario()));
        return dto;
    }

    public static RecompensaDTO recompensa(Recompensa r) {
        RecompensaDTO dto = new RecompensaDTO();
        dto.setExperienciaPorPersonaje(r.getExperienciaPorPersonaje());
        dto.setOro(r.getOro());
        for (Item item : r.getItems()) {
            dto.getItems().add(item.getNombre());
        }
        dto.setSubidasDeNivel(r.getSubidasDeNivel());
        return dto;
    }

    // ---------------------------------------------------------------- batalla

    public static BatallaDTO batalla(Batalla b) {
        List<Combatiente> participantes = b.getCombatientes();
        BatallaDTO dto = new BatallaDTO();
        dto.setNumero(b.getNumeroBatalla());
        dto.setRonda(b.getRonda());
        dto.setEnCurso(b.getEstado() == EstadoBatalla.EN_CURSO);
        dto.setVictoria(b.getEstado() == EstadoBatalla.VICTORIA);
        dto.setRegistro(b.getRegistro());
        for (Personaje p : b.getAliados()) {
            dto.getAliados().add(personaje(p, participantes.indexOf(p)));
        }
        for (Enemigo e : b.getEnemigos()) {
            CombatienteDTO c = new CombatienteDTO();
            completarCombatiente(c, e, participantes.indexOf(e));
            c.setEnemigo(true);
            c.setJefe(e.esJefe());
            dto.getEnemigos().add(c);
        }
        dto.setActorActualId(idDe(b.getActorActual(), participantes));
        dto.setTurnoDelJugador(b.esTurnoDelJugador());
        dto.setUltimoActorId(idDe(b.getUltimoActor(), participantes));
        for (Combatiente c : b.getProximosTurnos()) {
            dto.getProximosTurnos().add(participantes.indexOf(c));
        }
        dto.setUltimoTurno(turno(b.getUltimoActor(), b.getUltimaAccion(), participantes));
        dto.setItems(items(b.getInventario()));
        return dto;
    }

    private static TurnoDTO turno(Combatiente actor, Accion accion, List<Combatiente> participantes) {
        if (actor == null || accion == null) {
            return null;
        }
        TurnoDTO dto = new TurnoDTO();
        dto.setTipo(TipoAccionDTO.valueOf(accion.getTipo().name()));
        dto.setActorId(participantes.indexOf(actor));
        for (Combatiente objetivo : accion.getObjetivos()) {
            dto.getObjetivos().add(participantes.indexOf(objetivo));
        }
        dto.setNombreUsado(accion.getNombreDeLoUsado());
        Habilidad h = accion.getHabilidadUsada();
        if (h != null) {
            dto.setEfecto(efecto(h));
        }
        return dto;
    }

    private static EfectoDTO efecto(Habilidad h) {
        if (h.getEstadoAplicado() != null) {
            return EfectoDTO.valueOf(h.getEstadoAplicado().name());   // VENENO, ESCUDO o ATURDIMIENTO
        }
        return h.curaVida() ? EfectoDTO.CURACION : EfectoDTO.DANIO;
    }

    // ---------------------------------------------------------------- personajes y combatientes

    public static PersonajeDTO personaje(Personaje p, int id) {
        PersonajeDTO dto = new PersonajeDTO();
        completarCombatiente(dto, p, id);
        dto.setNombreClase(p.getNombreClase());
        dto.setNivel(p.getNivel());
        dto.setExperiencia(p.getExperiencia());
        dto.setExperienciaParaSubir(p.experienciaParaSubir());
        dto.setUsaMana(p.getTipoRecurso() == TipoRecurso.MANA);
        dto.setRecursoActual(p.getRecursoActual());
        dto.setRecursoMaximo(p.getRecursoMaximo());
        dto.setBonusAtaque(p.getBonusAtaque());
        dto.setBonusDefensa(p.getBonusDefensa());
        dto.setBonusVelocidad(p.getBonusVelocidad());
        List<Habilidad> habilidades = p.getHabilidades();
        for (int i = 0; i < habilidades.size(); i++) {
            Habilidad h = habilidades.get(i);
            HabilidadDTO hd = new HabilidadDTO();
            hd.setIndice(i);
            hd.setNombre(h.getNombre());
            hd.setCosto(h.getCostoRecurso());
            hd.setUsaMana(h.getTipoRecurso() == TipoRecurso.MANA);
            hd.setAlcance(AlcanceDTO.valueOf(h.getAlcance().name()));
            hd.setUsable(p.puedeUsar(h));
            dto.getHabilidades().add(hd);
        }
        for (Equipable e : p.getEquipamiento()) {
            EquipadoDTO ed = new EquipadoDTO();
            ed.setRanura(e.getRanura().name());
            ed.setNombreRanura(e.getRanura().getNombre());
            ed.setNombre(e.getNombre());
            ed.setDescripcion(e.getDescripcion());
            dto.getEquipamiento().add(ed);
        }
        return dto;
    }

    private static void completarCombatiente(CombatienteDTO dto, Combatiente c, int id) {
        dto.setId(id);
        dto.setNombre(c.getNombre());
        dto.setClase(c.getClass().getSimpleName());
        dto.setVivo(c.estaVivo());
        dto.setVidaActual(c.getVidaActual());
        dto.setVidaMaxima(c.getVidaMaximo());
        dto.setAtaque(c.getAtaque());
        dto.setDefensa(c.getDefensa());
        dto.setVelocidad(c.getVelocidad());
        dto.setEnvenenado(c.tieneEstado(TipoEstado.VENENO));
        dto.setConEscudo(c.tieneEstado(TipoEstado.ESCUDO));
        dto.setAturdido(c.tieneEstado(TipoEstado.ATURDIMIENTO));
        dto.setDefendiendo(c.estaDefendiendo());
        for (EstadoEfecto e : c.getEstados()) {
            dto.getEstados().add(e.getNombre());
        }
    }

    // ---------------------------------------------------------------- ítems

    public static List<ItemDTO> items(Inventario inventario) {
        List<ItemDTO> lista = new ArrayList<>();
        for (Item item : inventario.obtenerItemsDistintos()) {
            ItemDTO dto = new ItemDTO();
            dto.setNombre(item.getNombre());
            dto.setDescripcion(item.getDescripcion());
            dto.setCantidad(inventario.cantidadDe(item.getNombre()));
            dto.setEquipable(item.esEquipable());
            dto.setUsableEnBatalla(item.sePuedeUsarEnBatalla());
            lista.add(dto);
        }
        return lista;
    }

    private static int idDe(Combatiente c, List<Combatiente> participantes) {
        return c == null ? -1 : participantes.indexOf(c);
    }
}
