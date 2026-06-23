package cl.duoc.ms_grupos.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.duoc.ms_grupos.dto.CrearGrupoDto;
import cl.duoc.ms_grupos.dto.GrupoRespuestaDto;
import cl.duoc.ms_grupos.model.Grupo;
import cl.duoc.ms_grupos.model.MiembroGrupo;
import cl.duoc.ms_grupos.model.RolGrupo;
import cl.duoc.ms_grupos.repository.GrupoRepository;
import cl.duoc.ms_grupos.repository.MiembroGrupoRepository;

@ExtendWith(MockitoExtension.class)
public class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @InjectMocks
    private GrupoService grupoService;

    private Grupo grupoEjemplo;
    private MiembroGrupo adminEjemplo;

    @BeforeEach
    void setUp(){
        grupoEjemplo = new Grupo();
        grupoEjemplo.setId(1);
        grupoEjemplo.setNombre("Jugadores Magic Santiago");
        grupoEjemplo.setDescripcion("Grupo para coordinar partidas");
        grupoEjemplo.setCreadoPorUsuarioId(3);
        grupoEjemplo.setCreadoPorNombre("Pedro");

        adminEjemplo = new MiembroGrupo();
        adminEjemplo.setId(1);
        adminEjemplo.setGrupo(grupoEjemplo);
        adminEjemplo.setUsuarioId(3);
        adminEjemplo.setNombreUsuario("Pedro");
        adminEjemplo.setRolGrupo(RolGrupo.ADMIN);
    }

    // =====================================================================
    // crearGrupo
    // =====================================================================

    @Test
    void crearGrupo_exitoso(){
        CrearGrupoDto dto = new CrearGrupoDto();
        dto.setNombre("Jugadores Magic Santiago");
        dto.setDescripcion("Grupo para coordinar partidas");

        when(grupoRepository.save(any(Grupo.class))).thenReturn(grupoEjemplo);
        when(miembroGrupoRepository.findByGrupoId(1)).thenReturn(Arrays.asList(adminEjemplo));

        GrupoRespuestaDto respuesta = grupoService.crearGrupo(3, "Pedro", dto);

        assertEquals("Jugadores Magic Santiago", respuesta.getNombre());
        assertEquals(1, respuesta.getCantidadMiembros());
        verify(miembroGrupoRepository).save(any(MiembroGrupo.class));
    }

    @Test
    void crearGrupo_nombreVacio_lanzaExcepcion(){
        CrearGrupoDto dto = new CrearGrupoDto();
        dto.setNombre("   ");

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.crearGrupo(3, "Pedro", dto));

        assertEquals("El nombre del grupo no puede estar vacío.", error.getMessage());
        verify(grupoRepository, never()).save(any());
    }

    // =====================================================================
    // listarGrupos
    // =====================================================================

    @Test
    void listarGrupos_retornaLista(){
        when(grupoRepository.findAll()).thenReturn(Arrays.asList(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoId(1)).thenReturn(Arrays.asList(adminEjemplo));

        List<GrupoRespuestaDto> resultado = grupoService.listarGrupos();

        assertEquals(1, resultado.size());
        assertEquals("Jugadores Magic Santiago", resultado.get(0).getNombre());
    }

    // =====================================================================
    // verGrupo
    // =====================================================================

    @Test
    void verGrupo_encontrado(){
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoId(1)).thenReturn(Arrays.asList(adminEjemplo));

        GrupoRespuestaDto resultado = grupoService.verGrupo(1);

        assertEquals(1, resultado.getId());
    }

    @Test
    void verGrupo_noEncontrado(){
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.verGrupo(99));

        assertEquals("Grupo no encontrado: 99", error.getMessage());
    }

    // =====================================================================
    // misGrupos
    // =====================================================================

    @Test
    void misGrupos_retornaLista(){
        when(miembroGrupoRepository.findByUsuarioId(3)).thenReturn(Arrays.asList(adminEjemplo));
        when(miembroGrupoRepository.findByGrupoId(1)).thenReturn(Arrays.asList(adminEjemplo));

        List<GrupoRespuestaDto> resultado = grupoService.misGrupos(3);

        assertEquals(1, resultado.size());
    }

    // =====================================================================
    // unirseAGrupo
    // =====================================================================

    @Test
    void unirseAGrupo_exitoso(){
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.existsByGrupoIdAndUsuarioId(1, 5)).thenReturn(false);
        when(miembroGrupoRepository.findByGrupoId(1)).thenReturn(Arrays.asList(adminEjemplo));

        GrupoRespuestaDto resultado = grupoService.unirseAGrupo(1, 5, "Ana");

        assertEquals("Jugadores Magic Santiago", resultado.getNombre());
        verify(miembroGrupoRepository).save(any(MiembroGrupo.class));
    }

    @Test
    void unirseAGrupo_grupoNoEncontrado(){
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.unirseAGrupo(99, 5, "Ana"));

        assertEquals("Grupo no encontrado: 99", error.getMessage());
    }

    @Test
    void unirseAGrupo_yaEsMiembro(){
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.existsByGrupoIdAndUsuarioId(1, 3)).thenReturn(true);

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.unirseAGrupo(1, 3, "Pedro"));

        assertEquals("Ya eres miembro de este grupo.", error.getMessage());
    }

    // =====================================================================
    // salirDeGrupo
    // =====================================================================

    @Test
    void salirDeGrupo_exitoso(){
        MiembroGrupo miembro = new MiembroGrupo();
        miembro.setId(2);
        miembro.setGrupo(grupoEjemplo);
        miembro.setUsuarioId(5);
        miembro.setNombreUsuario("Ana");
        miembro.setRolGrupo(RolGrupo.MIEMBRO);

        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioId(1, 5)).thenReturn(Optional.of(miembro));
        when(miembroGrupoRepository.findByGrupoId(1)).thenReturn(Arrays.asList(adminEjemplo));

        GrupoRespuestaDto resultado = grupoService.salirDeGrupo(1, 5);

        assertEquals("Jugadores Magic Santiago", resultado.getNombre());
        verify(miembroGrupoRepository).delete(miembro);
    }

    @Test
    void salirDeGrupo_grupoNoEncontrado(){
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.salirDeGrupo(99, 5));

        assertEquals("Grupo no encontrado: 99", error.getMessage());
    }

    @Test
    void salirDeGrupo_noEsMiembro(){
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioId(1, 99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.salirDeGrupo(1, 99));

        assertEquals("No eres miembro de este grupo.", error.getMessage());
    }

    @Test
    void salirDeGrupo_esAdmin_lanzaExcepcion(){
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioId(1, 3)).thenReturn(Optional.of(adminEjemplo));

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.salirDeGrupo(1, 3));

        assertEquals("Eres el administrador del grupo. Elimina el grupo en vez de salir.", error.getMessage());
    }

    // =====================================================================
    // eliminarGrupo
    // =====================================================================

    @Test
    void eliminarGrupo_exitoso(){
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioId(1, 3)).thenReturn(Optional.of(adminEjemplo));

        grupoService.eliminarGrupo(1, 3);

        verify(miembroGrupoRepository).deleteByGrupoId(1);
        verify(grupoRepository).delete(grupoEjemplo);
    }

    @Test
    void eliminarGrupo_grupoNoEncontrado(){
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.eliminarGrupo(99, 3));

        assertEquals("Grupo no encontrado: 99", error.getMessage());
    }

    @Test
    void eliminarGrupo_noEsMiembro(){
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioId(1, 99)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.eliminarGrupo(1, 99));

        assertEquals("No eres miembro de este grupo.", error.getMessage());
    }

    @Test
    void eliminarGrupo_noEsAdmin_lanzaExcepcion(){
        MiembroGrupo miembro = new MiembroGrupo();
        miembro.setId(2);
        miembro.setGrupo(grupoEjemplo);
        miembro.setUsuarioId(5);
        miembro.setNombreUsuario("Ana");
        miembro.setRolGrupo(RolGrupo.MIEMBRO);

        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupoEjemplo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioId(1, 5)).thenReturn(Optional.of(miembro));

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                grupoService.eliminarGrupo(1, 5));

        assertEquals("Solo el administrador puede eliminar el grupo.", error.getMessage());
    }
}
