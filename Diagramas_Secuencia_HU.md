# Diagramas de Secuencia – TaskflowCM

---

## HU01 – Registro de usuario

```mermaid
sequenceDiagram
    actor Cliente
    participant AuthController
    participant GlobalExceptionHandler
    participant AuthService
    participant UsuarioRepository
    participant DB

    Cliente->>AuthController: POST /usuarios {nombre, email, password}
    AuthController->>AuthService: registrar(RegisterRequest)
    alt @Valid falla (campo vacío o email inválido)
        AuthController-->>GlobalExceptionHandler: MethodArgumentNotValidException
        GlobalExceptionHandler-->>Cliente: 400 {campos: {campo: mensaje}}
    else Validaciones pasan
        AuthService->>UsuarioRepository: existsByEmail(email.toLowerCase())
        UsuarioRepository->>DB: SELECT COUNT(*) FROM usuarios WHERE email = ?
        DB-->>UsuarioRepository: count
        UsuarioRepository-->>AuthService: true/false
        alt Email ya registrado
            AuthService-->>GlobalExceptionHandler: ConflictException("Ya existe una cuenta con ese email")
            GlobalExceptionHandler-->>Cliente: 409 Conflict
        else Email disponible
            AuthService->>AuthService: validarPassword(password)
            alt Password inválido (< 8 chars, sin mayúscula, sin número, sin especial)
                AuthService-->>GlobalExceptionHandler: BadRequestException
                GlobalExceptionHandler-->>Cliente: 400 Bad Request
            else Password válido
                AuthService->>AuthService: passwordEncoder.encode(password)
                AuthService->>UsuarioRepository: save(usuario)
                UsuarioRepository->>DB: INSERT INTO usuarios (nombre, email, password, nivel, puntos, ...)
                DB-->>UsuarioRepository: Usuario guardado
                UsuarioRepository-->>AuthService: Usuario
                AuthService-->>AuthController: UsuarioResponse.from(usuario)
                AuthController-->>Cliente: 201 Created {id, nombre, email, nivel, puntos}
            end
        end
    end
```

---

## HU02 – Inicio de sesión

```mermaid
sequenceDiagram
    actor Cliente
    participant AuthController
    participant GlobalExceptionHandler
    participant AuthService
    participant UsuarioRepository
    participant DB

    Cliente->>AuthController: POST /login {email, password}
    alt @Valid falla (email vacío o inválido)
        AuthController-->>GlobalExceptionHandler: MethodArgumentNotValidException
        GlobalExceptionHandler-->>Cliente: 400 {campos: {campo: mensaje}}
    else Validaciones pasan
        AuthController->>AuthService: login(LoginRequest)
        AuthService->>UsuarioRepository: findByEmail(email.toLowerCase())
        UsuarioRepository->>DB: SELECT * FROM usuarios WHERE email = ?
        DB-->>UsuarioRepository: Optional<Usuario>
        UsuarioRepository-->>AuthService: Optional<Usuario>
        alt Usuario no encontrado o password incorrecto
            AuthService-->>GlobalExceptionHandler: BadRequestException("Credenciales incorrectas")
            GlobalExceptionHandler-->>Cliente: 400 Bad Request
        else Credenciales válidas
            AuthService->>AuthService: passwordEncoder.matches(raw, hash)
            AuthService-->>AuthController: UsuarioResponse.from(usuario)
            AuthController-->>Cliente: 200 OK {id, nombre, email, nivel, quizzesCompletados, puntos}
        end
    end
```

---

## HU03 – Visualización de tablero Scrum

```mermaid
sequenceDiagram
    actor Cliente
    participant TareaController
    participant TareaService
    participant TareaRepository
    participant DB

    Cliente->>TareaController: GET /tareas?usuarioId={id}
    TareaController->>TareaService: obtenerPorUsuario(usuarioId)
    TareaService->>TareaRepository: findByUsuarioIdOrderByCreatedAtDesc(usuarioId)
    TareaRepository->>DB: SELECT * FROM tareas WHERE usuario_id = ? ORDER BY created_at DESC
    DB-->>TareaRepository: List<Tarea>
    TareaRepository-->>TareaService: List<Tarea>
    TareaService-->>TareaController: List<Tarea>
    TareaController-->>Cliente: 200 OK [{id, titulo, descripcion, estado, prioridad, puntosHistoria, ...}]
    Note over Cliente: Agrupa tarjetas por estado: pendiente / en_proceso / completada
```

---

## HU04 – Movimiento de tareas (Drag & Drop)

```mermaid
sequenceDiagram
    actor Cliente
    participant TareaController
    participant GlobalExceptionHandler
    participant TareaService
    participant TareaRepository
    participant GamificacionService
    participant UsuarioRepository
    participant LogroRepository
    participant LogroUsuarioRepository
    participant DB

    Cliente->>TareaController: PATCH /tareas/{id}/status {estado: "completada"}
    TareaController->>TareaService: actualizarEstado(id, nuevoEstado)
    alt Estado no está en [pendiente, en_proceso, completada]
        TareaService-->>GlobalExceptionHandler: BadRequestException("Estado no válido")
        GlobalExceptionHandler-->>Cliente: 400 Bad Request
    else Estado válido
        TareaService->>TareaRepository: findById(id)
        TareaRepository->>DB: SELECT * FROM tareas WHERE id = ?
        DB-->>TareaRepository: Optional<Tarea>
        alt Tarea no encontrada
            TareaService-->>GlobalExceptionHandler: ResourceNotFoundException("Tarea no encontrada")
            GlobalExceptionHandler-->>Cliente: 404 Not Found
        else Tarea encontrada
            TareaService->>TareaRepository: save(tarea con estado actualizado)
            TareaRepository->>DB: UPDATE tareas SET estado = ? WHERE id = ?
            DB-->>TareaRepository: Tarea
            TareaRepository-->>TareaService: Tarea actualizada
            alt estado == "completada" y usuarioId != null
                TareaService->>GamificacionService: procesarActividad(usuarioId, "TAREA_COMPLETADA")
                GamificacionService->>UsuarioRepository: findById(usuarioId)
                GamificacionService->>TareaRepository: findByUsuarioIdAndEstado(usuarioId, "completada")
                opt completadas >= 5
                    GamificacionService->>LogroRepository: findByCodigo("CINCO_TAREAS")
                    GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
                    GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
                end
                opt completadas >= 10
                    GamificacionService->>LogroRepository: findByCodigo("DIEZ_TAREAS")
                    GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
                    GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
                end
                GamificacionService->>UsuarioRepository: save(usuario con puntos + 20)
                GamificacionService-->>TareaService: {puntosGanados: 20, puntosTotal, nuevosLogros}
            end
            TareaService-->>TareaController: Tarea actualizada
            TareaController-->>Cliente: 200 OK {id, estado, ...}
        end
    end
```

---

## HU05 – Contenido educativo en tarjetas

```mermaid
sequenceDiagram
    actor Cliente
    participant QuizController
    participant QuizService
    participant CuestionarioRepository
    participant TeoriaRepository
    participant DB

    Cliente->>QuizController: GET /api/quiz/nivel/{nivel}
    QuizController->>QuizService: obtenerPorNivel(nivel)
    QuizService->>CuestionarioRepository: findByNivel(nivel)
    CuestionarioRepository->>DB: SELECT * FROM cuestionarios WHERE nivel = ?
    DB-->>CuestionarioRepository: List<Cuestionario>
    CuestionarioRepository-->>QuizService: List<Cuestionario>
    QuizService-->>QuizController: List<Cuestionario>
    QuizController-->>Cliente: 200 OK [lista de cuestionarios con titulo, descripcion, tipo, nivel]

    Cliente->>QuizController: GET /api/teoria/{cuestionarioId}
    QuizController->>QuizService: obtenerTeoriaPorCuestionario(cuestionarioId)
    QuizService->>TeoriaRepository: findByCuestionarioIdOrderByOrden(cuestionarioId)
    TeoriaRepository->>DB: SELECT * FROM teorias WHERE cuestionario_id = ? ORDER BY orden
    DB-->>TeoriaRepository: List<Teoria>
    TeoriaRepository-->>QuizService: List<Teoria>
    QuizService-->>QuizController: List<Teoria>
    QuizController-->>Cliente: 200 OK [contenido teórico ordenado por campo orden]
```

---

## HU06 – Evaluación mediante quizzes

```mermaid
sequenceDiagram
    actor Cliente
    participant QuizController
    participant QuizService
    participant PreguntaRepository
    participant AuthController
    participant GlobalExceptionHandler
    participant AuthService
    participant UsuarioRepository
    participant GamificacionService
    participant DB

    Cliente->>QuizController: GET /api/quiz/{cuestionarioId}
    QuizController->>QuizService: obtenerPreguntasPorCuestionario(cuestionarioId)
    QuizService->>PreguntaRepository: findByCuestionarioId(cuestionarioId)
    PreguntaRepository->>DB: SELECT * FROM preguntas WHERE cuestionario_id = ?
    DB-->>PreguntaRepository: List<Pregunta>
    PreguntaRepository-->>QuizService: List<Pregunta>
    QuizService-->>QuizController: List<Pregunta>
    QuizController-->>Cliente: 200 OK [preguntas con opciones]

    Cliente->>QuizController: POST /api/quiz/answer {preguntaId, respuesta}
    QuizController->>QuizService: validarRespuesta(preguntaId, respuesta)
    QuizService->>PreguntaRepository: findById(preguntaId)
    PreguntaRepository->>DB: SELECT * FROM preguntas WHERE id = ?
    DB-->>PreguntaRepository: Pregunta
    PreguntaRepository-->>QuizService: Pregunta
    QuizService->>QuizService: respuestaCorrecta.equalsIgnoreCase(respuesta.trim())
    QuizService-->>QuizController: {correcto, respuestaCorrecta, explicacion, puntaje}
    QuizController-->>Cliente: 200 OK {correcto: true/false, respuestaCorrecta, explicacion, puntaje}

    Cliente->>AuthController: POST /usuarios/{id}/completar-quiz
    AuthController->>AuthService: completarQuiz(id)
    AuthService->>UsuarioRepository: findById(id)
    UsuarioRepository->>DB: SELECT * FROM usuarios WHERE id = ?
    DB-->>UsuarioRepository: Optional<Usuario>
    alt Usuario no encontrado
        AuthService-->>GlobalExceptionHandler: ResourceNotFoundException("Usuario no encontrado")
        GlobalExceptionHandler-->>Cliente: 404 Not Found
    else Usuario encontrado
        AuthService->>AuthService: quizzesCompletados++ / si >= 4 y nivel="basico" → setNivel("avanzado")
        AuthService->>UsuarioRepository: save(usuario)
        UsuarioRepository->>DB: UPDATE usuarios SET quizzes_completados = ?, nivel = ? WHERE id = ?
        DB-->>UsuarioRepository: Usuario
        AuthService->>GamificacionService: procesarActividad(id, "QUIZ_COMPLETADO")
        opt nivel cambió de "basico" a "avanzado"
            AuthService->>GamificacionService: procesarActividad(id, "NIVEL_AVANZADO")
        end
        AuthService-->>AuthController: UsuarioResponse.from(usuario)
        AuthController-->>Cliente: 200 OK {nivel, quizzesCompletados, puntos}
    end
```

---

## HU07 – Seguimiento de progreso

```mermaid
sequenceDiagram
    actor Cliente
    participant TareaController
    participant QuizController
    participant TareaService
    participant TareaRepository
    participant UsuarioRepository
    participant DB

    Cliente->>TareaController: GET /api/user/stats?usuarioId={id}
    TareaController->>TareaService: obtenerStats(usuarioId)
    TareaService->>TareaRepository: countGroupByEstado(usuarioId)
    TareaRepository->>DB: SELECT estado, COUNT(*) as total FROM tareas WHERE usuario_id = ? GROUP BY estado
    DB-->>TareaRepository: List<Map<String,Object>>
    TareaRepository-->>TareaService: [{estado, total}, ...]
    TareaService->>TareaService: calcular pendientes / enProceso / completadas / total
    TareaService-->>TareaController: Map {total, pendientes, enProceso, completadas}
    TareaController-->>Cliente: 200 OK {total, pendientes, enProceso, completadas}

    Cliente->>QuizController: GET /api/user/progress?usuarioId={id}
    QuizController->>UsuarioRepository: findById(usuarioId)
    UsuarioRepository->>DB: SELECT * FROM usuarios WHERE id = ?
    DB-->>UsuarioRepository: Optional<Usuario>
    UsuarioRepository-->>QuizController: Usuario
    QuizController-->>Cliente: 200 OK {nivel, quizzesCompletados, totalQuizzes: 8}
```

---

## HU08 – Visualización de tareas en tiempo real

```mermaid
sequenceDiagram
    actor Cliente
    participant TareaController
    participant TareaService
    participant TareaRepository
    participant DB

    Note over Cliente,DB: El frontend ejecuta setInterval cada 10 segundos

    loop Cada 10 segundos (si sección tablero visible)
        Cliente->>TareaController: GET /tareas?usuarioId={id}
        TareaController->>TareaService: obtenerPorUsuario(usuarioId)
        TareaService->>TareaRepository: findByUsuarioIdOrderByCreatedAtDesc(usuarioId)
        TareaRepository->>DB: SELECT * FROM tareas WHERE usuario_id = ? ORDER BY created_at DESC
        DB-->>TareaRepository: List<Tarea>
        TareaRepository-->>TareaService: List<Tarea>
        TareaService-->>TareaController: List<Tarea>
        TareaController-->>Cliente: 200 OK [tareas actualizadas]
        alt Fetch falla (red caída)
            Note over Cliente: conexionActiva = false → muestra "⚠️ Sin conexión — reintentando..."
        else Fetch exitoso
            Note over Cliente: Re-renderiza tarjetas si hay cambios
        end
    end
```

---

## HU09 – Gestión de tareas (CRUD)

```mermaid
sequenceDiagram
    actor Cliente
    participant TareaController
    participant GlobalExceptionHandler
    participant TareaService
    participant TareaRepository
    participant GamificacionService
    participant UsuarioRepository
    participant LogroRepository
    participant LogroUsuarioRepository
    participant DB

    Cliente->>TareaController: POST /tareas {titulo, usuarioId, prioridad, ...}
    alt @Valid falla
        TareaController-->>GlobalExceptionHandler: MethodArgumentNotValidException
        GlobalExceptionHandler-->>Cliente: 400 {campos: {campo: mensaje}}
    else Válido
        TareaController->>TareaService: crear(TareaRequest)
        TareaService->>TareaRepository: save(tarea con estado="pendiente")
        TareaRepository->>DB: INSERT INTO tareas (titulo, estado, usuario_id, ...)
        DB-->>TareaRepository: Tarea
        TareaRepository-->>TareaService: Tarea nueva
        TareaService->>GamificacionService: procesarActividad(usuarioId, "TAREA_CREADA")
        GamificacionService->>TareaRepository: countByUsuarioId(usuarioId)
        opt totalTareas == 1
            GamificacionService->>LogroRepository: findByCodigo("PRIMERA_TAREA")
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
        end
        GamificacionService->>UsuarioRepository: save(usuario con puntos + 10)
        TareaService-->>TareaController: Tarea
        TareaController-->>Cliente: 201 Created {tarea}
    end

    Cliente->>TareaController: PUT /tareas/{id} {titulo, descripcion, ...}
    alt @Valid falla
        TareaController-->>GlobalExceptionHandler: MethodArgumentNotValidException
        GlobalExceptionHandler-->>Cliente: 400 {campos: {campo: mensaje}}
    else Válido
        TareaController->>TareaService: editar(id, TareaRequest)
        TareaService->>TareaRepository: findById(id)
        alt Tarea no encontrada
            TareaService-->>GlobalExceptionHandler: ResourceNotFoundException
            GlobalExceptionHandler-->>Cliente: 404 Not Found
        else Encontrada
            TareaService->>TareaRepository: save(tarea con campos actualizados)
            TareaRepository->>DB: UPDATE tareas SET titulo=?, descripcion=?, ... WHERE id=?
            TareaController-->>Cliente: 200 OK {tarea actualizada}
        end
    end

    Cliente->>TareaController: DELETE /tareas/{id}
    TareaController->>TareaService: eliminar(id)
    TareaService->>TareaRepository: existsById(id)
    alt Tarea no existe
        TareaService-->>GlobalExceptionHandler: ResourceNotFoundException
        GlobalExceptionHandler-->>Cliente: 404 Not Found
    else Existe
        TareaService->>TareaRepository: deleteById(id)
        TareaRepository->>DB: DELETE FROM tareas WHERE id = ?
        TareaController-->>Cliente: 200 OK {mensaje: "Tarea eliminada correctamente"}
    end
```

---

## HU10 – Reportes y estadísticas

```mermaid
sequenceDiagram
    actor Cliente
    participant TareaController
    participant TareaService
    participant TareaRepository
    participant GamificacionController
    participant GamificacionService
    participant LogroUsuarioRepository
    participant DB

    Cliente->>TareaController: GET /api/user/stats?usuarioId={id}
    TareaController->>TareaService: obtenerStats(usuarioId)
    TareaService->>TareaRepository: countGroupByEstado(usuarioId)
    TareaRepository->>DB: SELECT estado, COUNT(*) as total FROM tareas WHERE usuario_id = ? GROUP BY estado
    DB-->>TareaRepository: [{estado, total}, ...]
    TareaRepository-->>TareaService: List<Map>
    TareaService->>TareaService: calcular pendientes / enProceso / completadas / total
    TareaService-->>TareaController: Map {total, pendientes, enProceso, completadas}
    TareaController-->>Cliente: 200 OK {total, pendientes, enProceso, completadas}

    Cliente->>GamificacionController: GET /api/gamificacion/logros/{usuarioId}
    GamificacionController->>GamificacionService: obtenerLogrosUsuario(usuarioId)
    GamificacionService->>LogroUsuarioRepository: findByUsuarioId(usuarioId)
    LogroUsuarioRepository->>DB: SELECT * FROM logros_usuario WHERE usuario_id = ?
    DB-->>LogroUsuarioRepository: List<LogroUsuario>
    LogroUsuarioRepository-->>GamificacionService: List<LogroUsuario>
    GamificacionService-->>GamificacionController: List<LogroUsuario>
    GamificacionController-->>Cliente: 200 OK [logros con logro.nombre, logro.icono, obtenidoAt]
```

---

## HU11 – Niveles de aprendizaje

```mermaid
sequenceDiagram
    actor Cliente
    participant AuthController
    participant GlobalExceptionHandler
    participant AuthService
    participant UsuarioRepository
    participant GamificacionService
    participant DB

    Cliente->>AuthController: POST /usuarios/{id}/completar-quiz
    AuthController->>AuthService: completarQuiz(id)
    AuthService->>UsuarioRepository: findById(id)
    UsuarioRepository->>DB: SELECT * FROM usuarios WHERE id = ?
    DB-->>UsuarioRepository: Optional<Usuario>
    alt Usuario no encontrado
        AuthService-->>GlobalExceptionHandler: ResourceNotFoundException("Usuario no encontrado")
        GlobalExceptionHandler-->>Cliente: 404 Not Found
    else Usuario encontrado
        AuthService->>AuthService: nivelAntes = usuario.getNivel()
        AuthService->>AuthService: quizzesCompletados++
        opt quizzesCompletados >= 4 y nivel == "basico"
            AuthService->>AuthService: usuario.setNivel("avanzado")
        end
        AuthService->>UsuarioRepository: save(usuario)
        UsuarioRepository->>DB: UPDATE usuarios SET quizzes_completados = ?, nivel = ? WHERE id = ?
        DB-->>UsuarioRepository: Usuario (con nivel actualizado)
        UsuarioRepository-->>AuthService: Usuario
        AuthService->>GamificacionService: procesarActividad(id, "QUIZ_COMPLETADO")
        GamificacionService->>UsuarioRepository: save(usuario con puntos + 50)
        opt nivelAntes != usuario.getNivel() (nivel recién cambió a "avanzado")
            AuthService->>GamificacionService: procesarActividad(id, "NIVEL_AVANZADO")
            GamificacionService->>LogroRepository: findByCodigo("NIVEL_AVANZADO")
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
            GamificacionService->>UsuarioRepository: save(usuario con puntosRecompensa sumados)
        end
        AuthService-->>AuthController: UsuarioResponse.from(usuario)
        AuthController-->>Cliente: 200 OK {nivel, quizzesCompletados, puntos}
    end
```

---

## HU12 – Retroalimentación en tiempo real

```mermaid
sequenceDiagram
    actor Cliente
    participant QuizController
    participant QuizService
    participant PreguntaRepository
    participant DB

    Cliente->>QuizController: POST /api/quiz/answer {preguntaId, respuesta}
    QuizController->>QuizService: validarRespuesta(preguntaId, respuesta)
    QuizService->>PreguntaRepository: findById(preguntaId)
    PreguntaRepository->>DB: SELECT * FROM preguntas WHERE id = ?
    DB-->>PreguntaRepository: Optional<Pregunta>
    alt Pregunta no encontrada
        PreguntaRepository-->>QuizService: empty
        QuizService-->>QuizController: RuntimeException("Pregunta no encontrada")
        QuizController-->>Cliente: 500 {error: "Pregunta no encontrada"}
    else Pregunta encontrada
        PreguntaRepository-->>QuizService: Pregunta {respuestaCorrecta, explicacion, puntaje}
        QuizService->>QuizService: correcto = respuestaCorrecta.equalsIgnoreCase(respuesta.trim())
        alt Respuesta correcta
            QuizService-->>QuizController: {correcto: true, respuestaCorrecta, explicacion, puntaje: N}
            QuizController-->>Cliente: 200 OK {correcto: true, respuestaCorrecta, explicacion, puntaje}
        else Respuesta incorrecta
            QuizService-->>QuizController: {correcto: false, respuestaCorrecta, explicacion, puntaje: 0}
            QuizController-->>Cliente: 200 OK {correcto: false, respuestaCorrecta, explicacion, puntaje: 0}
        end
    end
```

---

## HU13 – Simulación de sprint

```mermaid
sequenceDiagram
    actor Cliente
    participant SimulacionController
    participant SimulacionService
    participant SimulacionRepository
    participant SimulacionItemRepository
    participant DB

    Cliente->>SimulacionController: POST /api/simulacion/iniciar {usuarioId, metaSprint, velocidad}
    SimulacionController->>SimulacionService: iniciarSimulacion(usuarioId, goal, velocidad)
    SimulacionService->>SimulacionRepository: findByUsuarioIdAndEstado(usuarioId, "activo")
    SimulacionRepository->>DB: SELECT * FROM simulaciones WHERE usuario_id=? AND estado='activo'
    DB-->>SimulacionRepository: Optional<Simulacion>
    opt Existe simulación activa previa
        SimulacionService->>SimulacionRepository: save(sim con estado="abandonado")
    end
    SimulacionService->>SimulacionRepository: save(nueva Simulacion con estado="activo")
    SimulacionRepository->>DB: INSERT INTO simulaciones (usuario_id, estado, sprint_goal, velocidad_equipo, ...)
    DB-->>SimulacionRepository: Simulacion
    loop 12 ítems del backlog predefinido
        SimulacionService->>SimulacionItemRepository: save(SimulacionItem)
        SimulacionItemRepository->>DB: INSERT INTO simulacion_items (simulacion_id, titulo, story_points, en_sprint=false, ...)
    end
    SimulacionService->>SimulacionItemRepository: findBySimulacionId(sim.getId())
    SimulacionController-->>Cliente: 200 OK {simulacion, items[]}

    Cliente->>SimulacionController: GET /api/simulacion/activa/{usuarioId}
    SimulacionController->>SimulacionService: obtenerSimulacionActiva(usuarioId)
    SimulacionService->>SimulacionRepository: findByUsuarioIdAndEstado(usuarioId, "activo")
    alt No hay simulación activa
        SimulacionService-->>SimulacionController: {activa: false}
        SimulacionController-->>Cliente: 200 OK {activa: false}
    else Hay simulación activa
        SimulacionService->>SimulacionItemRepository: findBySimulacionId(sim.getId())
        SimulacionService-->>SimulacionController: {activa: true, simulacion, items[]}
        SimulacionController-->>Cliente: 200 OK {activa: true, simulacion, items[]}
    end

    Cliente->>SimulacionController: PATCH /api/simulacion/item/{itemId}/toggle
    SimulacionController->>SimulacionService: toggleItemSprint(itemId)
    SimulacionService->>SimulacionItemRepository: findById(itemId)
    alt Item no encontrado
        SimulacionService-->>SimulacionController: RuntimeException("Item no encontrado")
        SimulacionController-->>Cliente: 500 {error: "Item no encontrado"}
    else Item encontrado
        SimulacionService->>SimulacionItemRepository: save(item con enSprint = !enSprint)
        SimulacionItemRepository->>DB: UPDATE simulacion_items SET en_sprint = ? WHERE id = ?
        SimulacionController-->>Cliente: 200 OK {item con enSprint actualizado}
    end

    Cliente->>SimulacionController: PATCH /api/simulacion/item/{itemId}/completar
    SimulacionController->>SimulacionService: completarItem(itemId)
    SimulacionService->>SimulacionItemRepository: findById(itemId)
    alt Item no encontrado
        SimulacionService-->>SimulacionController: RuntimeException("Item no encontrado")
        SimulacionController-->>Cliente: 500 {error: "Item no encontrado"}
    else Item encontrado
        SimulacionService->>SimulacionItemRepository: save(item con completado = true)
        SimulacionItemRepository->>DB: UPDATE simulacion_items SET completado = true WHERE id = ?
        SimulacionController-->>Cliente: 200 OK {item completado}
    end

    Cliente->>SimulacionController: POST /api/simulacion/cerrar/{simulacionId}
    SimulacionController->>SimulacionService: cerrarSprint(simulacionId)
    SimulacionService->>SimulacionRepository: findById(simulacionId)
    alt Simulación no encontrada
        SimulacionService-->>SimulacionController: RuntimeException("Simulación no encontrada")
        SimulacionController-->>Cliente: 500 {error: "Simulación no encontrada"}
    else Encontrada
        SimulacionService->>SimulacionItemRepository: findBySimulacionIdAndEnSprint(simulacionId, true)
        SimulacionService->>SimulacionService: filter completados / calcular planeados, entregados
        SimulacionService->>SimulacionService: exitoso = entregados >= planeados * 0.8
        SimulacionService->>SimulacionRepository: save(sim con estado="cerrado", cerradoAt=now())
        SimulacionService->>SimulacionService: generarRetrospectiva(exitoso, planeados, entregados)
        SimulacionController-->>Cliente: 200 OK {puntosPlaneados, puntosEntregados, velocidadReal, exitoso, completados, total, retrospectiva}
    end
```

---

## HU14 – Modo oscuro

```mermaid
sequenceDiagram
    actor Cliente
    participant script.js
    participant localStorage

    Note over Cliente,localStorage: Al cargar la aplicación
    Cliente->>script.js: initTema()
    script.js->>localStorage: getItem("taskflow-tema")
    localStorage-->>script.js: "oscuro" | "claro" | null
    script.js->>script.js: tema = valor || "claro"
    script.js->>script.js: aplicarTema(tema)
    script.js->>script.js: document.documentElement.setAttribute("data-tema", tema)
    script.js->>localStorage: setItem("taskflow-tema", tema)
    script.js->>script.js: actualizar texto btnTema ("☀️ Modo claro" / "🌙 Modo oscuro")

    Note over Cliente,localStorage: Usuario hace clic en el botón de tema
    Cliente->>script.js: toggleTema()
    script.js->>localStorage: getItem("taskflow-tema")
    localStorage-->>script.js: tema actual ("oscuro" / "claro")
    script.js->>script.js: nuevoTema = tema == "oscuro" ? "claro" : "oscuro"
    script.js->>script.js: aplicarTema(nuevoTema)
    script.js->>script.js: document.documentElement.setAttribute("data-tema", nuevoTema)
    script.js->>localStorage: setItem("taskflow-tema", nuevoTema)
    script.js->>script.js: actualizar texto btnTema
```

---

## HU15 – Gamificación

```mermaid
sequenceDiagram
    actor Cliente
    participant GamificacionController
    participant GamificacionService
    participant UsuarioRepository
    participant TareaRepository
    participant LogroRepository
    participant LogroUsuarioRepository
    participant DB

    Cliente->>GamificacionController: POST /api/gamificacion/actividad {usuarioId, tipo}
    GamificacionController->>GamificacionService: procesarActividad(usuarioId, tipoActividad)
    GamificacionService->>UsuarioRepository: findById(usuarioId)
    UsuarioRepository->>DB: SELECT * FROM usuarios WHERE id = ?
    DB-->>UsuarioRepository: Usuario
    UsuarioRepository-->>GamificacionService: Usuario

    alt tipoActividad == "TAREA_CREADA" (+10 pts)
        GamificacionService->>TareaRepository: countByUsuarioId(usuarioId)
        TareaRepository->>DB: SELECT COUNT(*) FROM tareas WHERE usuario_id = ?
        DB-->>TareaRepository: totalTareas
        opt totalTareas == 1
            GamificacionService->>LogroRepository: findByCodigo("PRIMERA_TAREA")
            LogroRepository->>DB: SELECT * FROM logros WHERE codigo = ?
            DB-->>LogroRepository: Optional<Logro>
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            LogroUsuarioRepository->>DB: SELECT COUNT(*) FROM logros_usuario WHERE ...
            DB-->>LogroUsuarioRepository: false (no otorgado)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
            LogroUsuarioRepository->>DB: INSERT INTO logros_usuario (...)
            GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosRecompensa) [en memoria]
        end
    else tipoActividad == "TAREA_COMPLETADA" (+20 pts)
        GamificacionService->>TareaRepository: findByUsuarioIdAndEstado(usuarioId, "completada")
        TareaRepository->>DB: SELECT * FROM tareas WHERE usuario_id=? AND estado='completada'
        DB-->>TareaRepository: List<Tarea>
        opt completadas >= 5
            GamificacionService->>LogroRepository: findByCodigo("CINCO_TAREAS")
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
            GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosRecompensa) [en memoria]
        end
        opt completadas >= 10
            GamificacionService->>LogroRepository: findByCodigo("DIEZ_TAREAS")
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
            GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosRecompensa) [en memoria]
        end
    else tipoActividad == "QUIZ_COMPLETADO" (+50 pts)
        GamificacionService->>GamificacionService: quizzes = usuario.getQuizzesCompletados()
        opt quizzes == 1
            GamificacionService->>LogroRepository: findByCodigo("PRIMER_QUIZ")
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
            GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosRecompensa) [en memoria]
        end
        opt quizzes >= 4
            GamificacionService->>LogroRepository: findByCodigo("TODOS_BASICO")
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
            GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosRecompensa) [en memoria]
        end
        opt quizzes >= 8
            GamificacionService->>LogroRepository: findByCodigo("TODOS_AVANZADO")
            GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
            GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
            GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosRecompensa) [en memoria]
        end
    else tipoActividad == "NIVEL_AVANZADO" (+0 pts)
        GamificacionService->>LogroRepository: findByCodigo("NIVEL_AVANZADO")
        GamificacionService->>LogroUsuarioRepository: existsByUsuarioIdAndLogroId(uid, lid)
        GamificacionService->>LogroUsuarioRepository: save(LogroUsuario)
        GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosRecompensa) [en memoria]
    end

    GamificacionService->>GamificacionService: usuario.setPuntos(puntos + puntosGanados) [en memoria]
    GamificacionService->>UsuarioRepository: save(usuario) [único save al final]
    UsuarioRepository->>DB: UPDATE usuarios SET puntos = ? WHERE id = ?
    GamificacionService-->>GamificacionController: {puntosGanados, puntosTotal, nuevosLogros[]}
    GamificacionController-->>Cliente: 200 OK {puntosGanados, puntosTotal, nuevosLogros}
```
