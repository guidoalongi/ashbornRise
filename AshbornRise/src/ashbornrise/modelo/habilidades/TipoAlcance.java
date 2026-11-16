package ashbornrise.modelo.habilidades;

import ashbornrise.modelo.Combatiente;

/**
 * A quién puede apuntar una habilidad.
 *
 * Cada valor sabe validar sus propios objetivos (un enum también puede tener comportamiento):
 * así el MODELO impide, por ejemplo, curar a un enemigo o usar una habilidad ofensiva
 * sobre un aliado, aunque la llamada venga mal desde otra parte del sistema.
 */
public enum TipoAlcance {

    UN_ENEMIGO {
        @Override
        public boolean admite(Combatiente origen, Combatiente objetivo) {
            return objetivo.estaVivo() && !origen.esAliadoDe(objetivo);
        }
    },
    VARIOS_ENEMIGOS {
        @Override
        public boolean admite(Combatiente origen, Combatiente objetivo) {
            return objetivo.estaVivo() && !origen.esAliadoDe(objetivo);
        }
    },
    UN_ALIADO {
        @Override
        public boolean admite(Combatiente origen, Combatiente objetivo) {
            return objetivo.estaVivo() && origen.esAliadoDe(objetivo);
        }
    },
    PROPIO {
        @Override
        public boolean admite(Combatiente origen, Combatiente objetivo) {
            return objetivo == origen;
        }
    };

    /** true si "objetivo" es un objetivo válido para una habilidad de este alcance usada por "origen". */
    public abstract boolean admite(Combatiente origen, Combatiente objetivo);
}
