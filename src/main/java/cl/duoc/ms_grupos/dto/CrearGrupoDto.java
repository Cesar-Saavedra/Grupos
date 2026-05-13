package cl.duoc.ms_grupos.dto;

import lombok.Data;

/*
 * Body del POST /api/grupos para crear un grupo nuevo.
 *
 * Ejemplo:
 * {
 *   "nombre": "Jugadores Magic Santiago",
 *   "descripcion": "Grupo para coordinar partidas en el centro"
 * }
 */
@Data
public class CrearGrupoDto {

    private String nombre;
    private String descripcion;
}
