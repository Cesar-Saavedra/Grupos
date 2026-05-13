package cl.duoc.ms_grupos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.ms_grupos.model.Grupo;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

    // No necesitamos metodos extra por ahora.
    // findById, findAll, save, deleteById vienen gratis de JpaRepository.

}
