package cl.duoc.ms_grupos.controller;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.ms_grupos.dto.CrearGrupoDto;
import cl.duoc.ms_grupos.dto.GrupoRespuestaDto;
import cl.duoc.ms_grupos.security.JwtUtil;
import cl.duoc.ms_grupos.service.GrupoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(GrupoController.class)
public class GrupoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GrupoService grupoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GrupoRespuestaDto grupoEjemplo;

    @BeforeEach
    void setUp(){
        grupoEjemplo = new GrupoRespuestaDto(1, "Jugadores Magic Santiago", "Descripcion", "Pedro", 1, Collections.emptyList());
    }

    // =====================================================================
    // POST /api/grupos
    // =====================================================================

    @Test
    void crearGrupo_sinToken_retorna401() throws Exception {
        CrearGrupoDto dto = new CrearGrupoDto();
        dto.setNombre("Jugadores Magic Santiago");

        mockMvc.perform(post("/api/grupos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearGrupo_exitoso_retorna201() throws Exception {
        CrearGrupoDto dto = new CrearGrupoDto();
        dto.setNombre("Jugadores Magic Santiago");

        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(3);
        when(jwtUtil.extraerNombre("token-bueno")).thenReturn("Pedro");
        when(grupoService.crearGrupo(eq(3), eq("Pedro"), any(CrearGrupoDto.class))).thenReturn(grupoEjemplo);

        mockMvc.perform(post("/api/grupos")
                        .header("Authorization", "Bearer token-bueno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Jugadores Magic Santiago"));
    }

    @Test
    void crearGrupo_nombreVacio_retorna400() throws Exception {
        CrearGrupoDto dto = new CrearGrupoDto();
        dto.setNombre("abc");

        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(3);
        when(jwtUtil.extraerNombre("token-bueno")).thenReturn("Pedro");
        when(grupoService.crearGrupo(eq(3), eq("Pedro"), any(CrearGrupoDto.class)))
                .thenThrow(new RuntimeException("El nombre del grupo no puede estar vacío."));

        mockMvc.perform(post("/api/grupos")
                        .header("Authorization", "Bearer token-bueno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // GET /api/grupos
    // =====================================================================

    @Test
    void listarGrupos_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/grupos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarGrupos_retorna200() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(grupoService.listarGrupos()).thenReturn(Arrays.asList(grupoEjemplo));

        mockMvc.perform(get("/api/grupos").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // =====================================================================
    // GET /api/grupos/mios
    // =====================================================================

    @Test
    void misGrupos_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/grupos/mios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void misGrupos_retorna200() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(3);
        when(grupoService.misGrupos(3)).thenReturn(Arrays.asList(grupoEjemplo));

        mockMvc.perform(get("/api/grupos/mios").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // =====================================================================
    // GET /api/grupos/{id}
    // =====================================================================

    @Test
    void verGrupo_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/grupos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verGrupo_encontrado_retorna200() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(grupoService.verGrupo(1)).thenReturn(grupoEjemplo);

        mockMvc.perform(get("/api/grupos/1").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isOk());
    }

    @Test
    void verGrupo_noEncontrado_retorna404() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(grupoService.verGrupo(99)).thenThrow(new RuntimeException("Grupo no encontrado: 99"));

        mockMvc.perform(get("/api/grupos/99").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // POST /api/grupos/{id}/unirse
    // =====================================================================

    @Test
    void unirseAGrupo_sinToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/grupos/1/unirse"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unirseAGrupo_exitoso_retorna200() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(5);
        when(jwtUtil.extraerNombre("token-bueno")).thenReturn("Ana");
        when(grupoService.unirseAGrupo(1, 5, "Ana")).thenReturn(grupoEjemplo);

        mockMvc.perform(post("/api/grupos/1/unirse").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isOk());
    }

    @Test
    void unirseAGrupo_yaEsMiembro_retorna400() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(5);
        when(jwtUtil.extraerNombre("token-bueno")).thenReturn("Ana");
        when(grupoService.unirseAGrupo(1, 5, "Ana")).thenThrow(new RuntimeException("Ya eres miembro de este grupo."));

        mockMvc.perform(post("/api/grupos/1/unirse").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // DELETE /api/grupos/{id}/salir
    // =====================================================================

    @Test
    void salirDeGrupo_sinToken_retorna401() throws Exception {
        mockMvc.perform(delete("/api/grupos/1/salir"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void salirDeGrupo_exitoso_retorna200() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(5);
        when(grupoService.salirDeGrupo(1, 5)).thenReturn(grupoEjemplo);

        mockMvc.perform(delete("/api/grupos/1/salir").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isOk());
    }

    // =====================================================================
    // DELETE /api/grupos/{id}
    // =====================================================================

    @Test
    void eliminarGrupo_sinToken_retorna401() throws Exception {
        mockMvc.perform(delete("/api/grupos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eliminarGrupo_exitoso_retorna200() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(3);

        mockMvc.perform(delete("/api/grupos/1").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Grupo eliminado correctamente."));
    }

    @Test
    void eliminarGrupo_noEsAdmin_retorna400() throws Exception {
        when(jwtUtil.obtenerTokenDelHeader("Bearer token-bueno")).thenReturn("token-bueno");
        when(jwtUtil.esTokenValido("token-bueno")).thenReturn(true);
        when(jwtUtil.extraerId("token-bueno")).thenReturn(5);
        org.mockito.Mockito.doThrow(new RuntimeException("Solo el administrador puede eliminar el grupo."))
                .when(grupoService).eliminarGrupo(1, 5);

        mockMvc.perform(delete("/api/grupos/1").header("Authorization", "Bearer token-bueno"))
                .andExpect(status().isBadRequest());
    }
}
