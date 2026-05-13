package cl.duoc.ms_grupos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.ms_grupos.model.MiembroGrupo;

@Repository
public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, Integer> {

    // Todos los miembros de un grupo
    List<MiembroGrupo> findByGrupoId(Integer grupoId);

    // Todos los grupos en los que esta un usuario (para "mis grupos")
    List<MiembroGrupo> findByUsuarioId(Integer usuarioId);

    // Buscar la membresia especifica de un usuario en un grupo
    Optional<MiembroGrupo> findByGrupoIdAndUsuarioId(Integer grupoId, Integer usuarioId);

    // Verificar si un usuario ya es miembro de un grupo
    boolean existsByGrupoIdAndUsuarioId(Integer grupoId, Integer usuarioId);

    // Eliminar todas las membresías de un grupo (cuando se borra el grupo)
    void deleteByGrupoId(Integer grupoId);
}
