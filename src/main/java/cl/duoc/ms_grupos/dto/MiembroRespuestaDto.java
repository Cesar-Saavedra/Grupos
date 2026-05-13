package cl.duoc.ms_grupos.dto;

import cl.duoc.ms_grupos.model.RolGrupo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Representa un miembro dentro de la respuesta de un grupo.
 *
 * Ejemplo:
 * {
 *   "id": 1,
 *   "usuarioId": 3,
 *   "nombreUsuario": "Pedro",
 *   "rolGrupo": "ADMIN"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiembroRespuestaDto {

    private Integer id;
    private Integer usuarioId;
    private String nombreUsuario;
    private RolGrupo rolGrupo;
}
