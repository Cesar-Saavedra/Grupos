package cl.duoc.ms_grupos.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Lo que el servidor devuelve cuando piden ver un grupo.
 *
 * Incluye la lista de miembros con sus roles dentro del grupo.
 *
 * Ejemplo:
 * {
 *   "id": 1,
 *   "nombre": "Jugadores Magic Santiago",
 *   "descripcion": "Grupo para coordinar partidas",
 *   "creadoPorNombre": "Pedro",
 *   "cantidadMiembros": 3,
 *   "miembros": [
 *     { "id": 1, "usuarioId": 3, "nombreUsuario": "Pedro", "rolGrupo": "ADMIN" },
 *     { "id": 2, "usuarioId": 5, "nombreUsuario": "Ana",   "rolGrupo": "MIEMBRO" }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoRespuestaDto {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String creadoPorNombre;
    private Integer cantidadMiembros;
    private List<MiembroRespuestaDto> miembros;
}
