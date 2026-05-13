package cl.duoc.ms_grupos.model;

/*
 * Rol que tiene un usuario DENTRO de un grupo.
 * Es distinto al Rol general de CardLink (JUGADOR / TIENDA / ORGANIZADOR).
 *
 * ADMIN   -> quien creo el grupo. Puede eliminarlo y expulsar miembros.
 * MIEMBRO -> cualquier usuario que se unio al grupo.
 */
public enum RolGrupo {
    ADMIN,
    MIEMBRO
}
