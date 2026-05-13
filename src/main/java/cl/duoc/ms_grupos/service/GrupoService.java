package cl.duoc.ms_grupos.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms_grupos.dto.CrearGrupoDto;
import cl.duoc.ms_grupos.dto.GrupoRespuestaDto;
import cl.duoc.ms_grupos.dto.MiembroRespuestaDto;
import cl.duoc.ms_grupos.model.Grupo;
import cl.duoc.ms_grupos.model.MiembroGrupo;
import cl.duoc.ms_grupos.model.RolGrupo;
import cl.duoc.ms_grupos.repository.GrupoRepository;
import cl.duoc.ms_grupos.repository.MiembroGrupoRepository;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private MiembroGrupoRepository miembroGrupoRepository;

    // =========================================================
    // CREAR GRUPO
    // =========================================================

    /*
     * Crea un grupo nuevo y agrega automaticamente al creador como ADMIN.
     *
     * Pasos:
     * 1. Guardar el grupo en la tabla "grupos".
     * 2. Crear la membresia del creador con rol ADMIN.
     */
    public GrupoRespuestaDto crearGrupo(Integer usuarioId, String nombreUsuario, CrearGrupoDto dto) {

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del grupo no puede estar vacío.");
        }

        // Paso 1: crear el grupo
        Grupo grupo = new Grupo();
        grupo.setNombre(dto.getNombre());
        grupo.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion() : "");
        grupo.setCreadoPorUsuarioId(usuarioId);
        grupo.setCreadoPorNombre(nombreUsuario);

        Grupo guardado = grupoRepository.save(grupo);

        // Paso 2: agregar al creador como ADMIN del grupo
        MiembroGrupo admin = new MiembroGrupo();
        admin.setGrupo(guardado);
        admin.setUsuarioId(usuarioId);
        admin.setNombreUsuario(nombreUsuario);
        admin.setRolGrupo(RolGrupo.ADMIN);

        miembroGrupoRepository.save(admin);

        return construirRespuesta(guardado);
    }

    // =========================================================
    // LISTAR TODOS LOS GRUPOS
    // =========================================================

    /*
     * Devuelve todos los grupos existentes en CardLink.
     * Cualquier usuario autenticado puede ver la lista.
     */
    public List<GrupoRespuestaDto> listarGrupos() {
        return grupoRepository.findAll()
                .stream()
                .map(this::construirRespuesta)
                .toList();
    }

    // =========================================================
    // VER UN GRUPO POR ID
    // =========================================================

    /*
     * Devuelve los detalles de un grupo, incluyendo su lista de miembros.
     */
    public GrupoRespuestaDto verGrupo(Integer grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));
        return construirRespuesta(grupo);
    }

    // =========================================================
    // MIS GRUPOS
    // =========================================================

    /*
     * Devuelve todos los grupos en los que participa el usuario autenticado.
     */
    public List<GrupoRespuestaDto> misGrupos(Integer usuarioId) {
        List<MiembroGrupo> membresias = miembroGrupoRepository.findByUsuarioId(usuarioId);

        return membresias.stream()
                .map(m -> construirRespuesta(m.getGrupo()))
                .toList();
    }

    // =========================================================
    // UNIRSE A UN GRUPO
    // =========================================================

    /*
     * Agrega al usuario autenticado como MIEMBRO del grupo indicado.
     * Lanza excepcion si el usuario ya es miembro.
     */
    public GrupoRespuestaDto unirseAGrupo(Integer grupoId, Integer usuarioId, String nombreUsuario) {

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));

        // Verificar que el usuario no sea ya miembro
        if (miembroGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuarioId)) {
            throw new RuntimeException("Ya eres miembro de este grupo.");
        }

        MiembroGrupo nuevo = new MiembroGrupo();
        nuevo.setGrupo(grupo);
        nuevo.setUsuarioId(usuarioId);
        nuevo.setNombreUsuario(nombreUsuario);
        nuevo.setRolGrupo(RolGrupo.MIEMBRO);

        miembroGrupoRepository.save(nuevo);

        return construirRespuesta(grupo);
    }

    // =========================================================
    // SALIR DE UN GRUPO
    // =========================================================

    /*
     * El usuario autenticado sale del grupo.
     * El ADMIN no puede salir si es el unico admin (debe eliminar el grupo).
     */
    public GrupoRespuestaDto salirDeGrupo(Integer grupoId, Integer usuarioId) {

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));

        MiembroGrupo membresia = miembroGrupoRepository
                .findByGrupoIdAndUsuarioId(grupoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo."));

        // Si es ADMIN no puede simplemente salir
        if (membresia.getRolGrupo() == RolGrupo.ADMIN) {
            throw new RuntimeException(
                "Eres el administrador del grupo. Elimina el grupo en vez de salir."
            );
        }

        miembroGrupoRepository.delete(membresia);

        return construirRespuesta(grupo);
    }

    // =========================================================
    // ELIMINAR GRUPO
    // =========================================================

    /*
     * Elimina el grupo y todos sus miembros.
     * Solo el ADMIN del grupo puede hacer esto.
     *
     * @Transactional asegura que si algo falla en el medio,
     * la BD queda igual que antes (no quedan datos a medias).
     */
    @Transactional
    public void eliminarGrupo(Integer grupoId, Integer usuarioId) {

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));

        // Verificar que quien pide eliminar sea el ADMIN
        MiembroGrupo membresia = miembroGrupoRepository
                .findByGrupoIdAndUsuarioId(grupoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo."));

        if (membresia.getRolGrupo() != RolGrupo.ADMIN) {
            throw new RuntimeException("Solo el administrador puede eliminar el grupo.");
        }

        // Primero eliminar todos los miembros, luego el grupo
        miembroGrupoRepository.deleteByGrupoId(grupoId);
        grupoRepository.delete(grupo);
    }

    // =========================================================
    // METODO AUXILIAR PRIVADO
    // =========================================================

    /*
     * Construye el DTO de respuesta a partir de una entidad Grupo.
     * Busca los miembros del grupo y los convierte a DTOs.
     */
    private GrupoRespuestaDto construirRespuesta(Grupo grupo) {

        List<MiembroGrupo> miembros = miembroGrupoRepository.findByGrupoId(grupo.getId());

        List<MiembroRespuestaDto> miembrosDto = miembros.stream()
                .map(m -> new MiembroRespuestaDto(
                        m.getId(),
                        m.getUsuarioId(),
                        m.getNombreUsuario(),
                        m.getRolGrupo()
                ))
                .toList();

        return new GrupoRespuestaDto(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getDescripcion(),
                grupo.getCreadoPorNombre(),
                miembros.size(),
                miembrosDto
        );
    }
}
