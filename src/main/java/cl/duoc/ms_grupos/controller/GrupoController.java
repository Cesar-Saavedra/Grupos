package cl.duoc.ms_grupos.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_grupos.dto.CrearGrupoDto;
import cl.duoc.ms_grupos.dto.GrupoRespuestaDto;
import cl.duoc.ms_grupos.security.JwtUtil;
import cl.duoc.ms_grupos.service.GrupoService;

/*
 * Controlador REST de ms-grupos.
 * Puerto: 8083
 * Base URL: http://localhost:8083/api/grupos
 *
 * TODOS los endpoints requieren:
 *   Header -> Authorization: Bearer {token}
 *
 * Endpoints:
 * ============================================================
 * POST   /api/grupos              -> Crear un grupo nuevo
 * GET    /api/grupos              -> Ver todos los grupos
 * GET    /api/grupos/mios         -> Ver los grupos en los que participo
 * GET    /api/grupos/{id}         -> Ver detalle de un grupo
 * POST   /api/grupos/{id}/unirse  -> Unirse a un grupo
 * DELETE /api/grupos/{id}/salir   -> Salir de un grupo
 * DELETE /api/grupos/{id}         -> Eliminar un grupo (solo ADMIN)
 * ============================================================
 */
@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping
    public ResponseEntity<?> crearGrupo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CrearGrupoDto dto) {

        String token = validarHeader(authHeader);
        if (token == null) {
            return respuestaNoAutorizado("Token requerido.");
        }

        Integer usuarioId   = jwtUtil.extraerId(token);
        String nombreUsuario = jwtUtil.extraerNombre(token);

        try {
            GrupoRespuestaDto grupo = grupoService.crearGrupo(usuarioId, nombreUsuario, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(grupo);
        } catch (RuntimeException e) {
            return respuestaError(e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<?> listarGrupos(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = validarHeader(authHeader);
        if (token == null) {
            return respuestaNoAutorizado("Token requerido.");
        }

        List<GrupoRespuestaDto> grupos = grupoService.listarGrupos();
        return ResponseEntity.ok(grupos);
    }


    @GetMapping("/mios")
    public ResponseEntity<?> misGrupos(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = validarHeader(authHeader);
        if (token == null) {
            return respuestaNoAutorizado("Token requerido.");
        }

        Integer usuarioId = jwtUtil.extraerId(token);
        List<GrupoRespuestaDto> grupos = grupoService.misGrupos(usuarioId);
        return ResponseEntity.ok(grupos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> verGrupo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        String token = validarHeader(authHeader);
        if (token == null) {
            return respuestaNoAutorizado("Token requerido.");
        }

        try {
            GrupoRespuestaDto grupo = grupoService.verGrupo(id);
            return ResponseEntity.ok(grupo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{id}/unirse")
    public ResponseEntity<?> unirseAGrupo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        String token = validarHeader(authHeader);
        if (token == null) {
            return respuestaNoAutorizado("Token requerido.");
        }

        Integer usuarioId    = jwtUtil.extraerId(token);
        String nombreUsuario = jwtUtil.extraerNombre(token);

        try {
            GrupoRespuestaDto grupo = grupoService.unirseAGrupo(id, usuarioId, nombreUsuario);
            return ResponseEntity.ok(grupo);
        } catch (RuntimeException e) {
            return respuestaError(e.getMessage());
        }
    }


    @DeleteMapping("/{id}/salir")
    public ResponseEntity<?> salirDeGrupo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        String token = validarHeader(authHeader);
        if (token == null) {
            return respuestaNoAutorizado("Token requerido.");
        }

        Integer usuarioId = jwtUtil.extraerId(token);

        try {
            GrupoRespuestaDto grupo = grupoService.salirDeGrupo(id, usuarioId);
            return ResponseEntity.ok(grupo);
        } catch (RuntimeException e) {
            return respuestaError(e.getMessage());
        }
    }

    // =========================================================
    // DELETE /api/grupos/{id}
    // Eliminar un grupo (solo el ADMIN puede hacerlo)
    // =========================================================
    /*
     * Header: Authorization: Bearer {token}
     *
     * Elimina el grupo y todos sus miembros.
     * Solo funciona si el usuario del token es el ADMIN del grupo.
     * Respuesta 200: mensaje de confirmacion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarGrupo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        String token = validarHeader(authHeader);
        if (token == null) {
            return respuestaNoAutorizado("Token requerido.");
        }

        Integer usuarioId = jwtUtil.extraerId(token);

        try {
            grupoService.eliminarGrupo(id, usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Grupo eliminado correctamente."));
        } catch (RuntimeException e) {
            return respuestaError(e.getMessage());
        }
    }

    // =========================================================
    // METODOS PRIVADOS DE AYUDA (iguales que en ms-usuarios)
    // =========================================================

    private String validarHeader(String authHeader) {
        String token = jwtUtil.obtenerTokenDelHeader(authHeader);
        if (token == null || !jwtUtil.esTokenValido(token)) {
            return null;
        }
        return token;
    }

    private ResponseEntity<?> respuestaNoAutorizado(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    private ResponseEntity<?> respuestaError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
