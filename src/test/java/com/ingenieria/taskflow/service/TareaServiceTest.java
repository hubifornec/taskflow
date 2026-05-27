package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.dto.TareaRequest;
import com.ingenieria.taskflow.exception.BadRequestException;
import com.ingenieria.taskflow.exception.ResourceNotFoundException;
import com.ingenieria.taskflow.model.Tarea;
import com.ingenieria.taskflow.repository.TareaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TareaService - Pruebas unitarias")
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private GamificacionService gamificacionService;

    @InjectMocks
    private TareaService tareaService;

    @Test
    @DisplayName("Crear tarea con datos válidos retorna tarea guardada")
    void crear_datosValidos_retornaTarea() {
        TareaRequest req = new TareaRequest();
        req.setTitulo("Implementar login");
        req.setDescripcion("Crear formulario de autenticación");
        req.setUsuarioId(1L);
        req.setPrioridad(8);
        req.setPuntosHistoria(5);

        Tarea tareaGuardada = new Tarea();
        tareaGuardada.setId(1L);
        tareaGuardada.setTitulo("Implementar login");
        tareaGuardada.setUsuarioId(1L);
        tareaGuardada.setEstado("pendiente");

        when(tareaRepository.save(any(Tarea.class))).thenReturn(tareaGuardada);
        when(gamificacionService.procesarActividad(anyLong(), anyString())).thenReturn(Map.of());

        Tarea result = tareaService.crear(req);

        assertNotNull(result);
        assertEquals("Implementar login", result.getTitulo());
        assertEquals("pendiente", result.getEstado());
        verify(gamificacionService).procesarActividad(1L, "TAREA_CREADA");
    }

    @Test
    @DisplayName("Actualizar estado a 'completada' dispara evento de gamificación")
    void actualizarEstado_aCompletada_disparaGamificacion() {
        Tarea tarea = new Tarea();
        tarea.setId(1L);
        tarea.setUsuarioId(2L);
        tarea.setEstado("en_proceso");

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any())).thenReturn(tarea);
        when(gamificacionService.procesarActividad(anyLong(), anyString())).thenReturn(Map.of());

        tareaService.actualizarEstado(1L, "completada");

        verify(gamificacionService).procesarActividad(2L, "TAREA_COMPLETADA");
    }

    @Test
    @DisplayName("Actualizar a estado inválido lanza BadRequestException")
    void actualizarEstado_estadoInvalido_lanzaBadRequest() {
        assertThrows(BadRequestException.class,
                () -> tareaService.actualizarEstado(1L, "estado_inventado"));
        verify(tareaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Eliminar tarea inexistente lanza ResourceNotFoundException")
    void eliminar_tareaNoExiste_lanzaResourceNotFound() {
        when(tareaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> tareaService.eliminar(99L));
        verify(tareaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Eliminar tarea existente la borra correctamente")
    void eliminar_tareaExiste_eliminaCorrectamente() {
        when(tareaRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> tareaService.eliminar(1L));
        verify(tareaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("obtenerStats calcula totales correctamente desde grupos")
    void obtenerStats_retornaTotalesCorrectos() {
        List<Map<String, Object>> grupos = List.of(
                Map.of("estado", "pendiente",   "total", 3L),
                Map.of("estado", "en_proceso",  "total", 2L),
                Map.of("estado", "completada",  "total", 5L)
        );
        when(tareaRepository.countGroupByEstado(1L)).thenReturn(grupos);

        Map<String, Object> stats = tareaService.obtenerStats(1L);

        assertEquals(10L, stats.get("total"));
        assertEquals(3L,  stats.get("pendientes"));
        assertEquals(2L,  stats.get("enProceso"));
        assertEquals(5L,  stats.get("completadas"));
    }

    @Test
    @DisplayName("Editar tarea inexistente lanza ResourceNotFoundException")
    void editar_tareaNoExiste_lanzaResourceNotFound() {
        TareaRequest req = new TareaRequest();
        req.setTitulo("Nuevo título");
        req.setUsuarioId(1L);

        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tareaService.editar(99L, req));
    }

    @Test
    @DisplayName("Obtener tareas por usuario retorna lista ordenada")
    void obtenerPorUsuario_retornaListaOrdenada() {
        List<Tarea> tareas = List.of(new Tarea(), new Tarea());
        when(tareaRepository.findByUsuarioIdOrderByCreatedAtDesc(1L)).thenReturn(tareas);

        List<Tarea> result = tareaService.obtenerPorUsuario(1L);

        assertEquals(2, result.size());
    }
}
