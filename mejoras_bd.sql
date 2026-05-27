-- ============================================================
-- TASKFLOW — MEJORAS BD
-- Ejecutar en orden: primero ALTER TABLE, luego el resto
-- ============================================================

-- ============================================================
-- 1. CAMBIOS ESTRUCTURALES (ejecutar primero)
-- ============================================================

-- Agregar puntos de historia a tareas (para usar en simulación)
ALTER TABLE tareas ADD COLUMN IF NOT EXISTS puntos_historia INTEGER DEFAULT 3;

-- Enlazar items de simulación con tareas reales
ALTER TABLE simulacion_items ADD COLUMN IF NOT EXISTS tarea_id BIGINT NULL;


-- ============================================================
-- 2. MEJORAR TARJETAS DE TEORÍA
-- ============================================================

-- Módulo 1: ¿Qué es Scrum? — ampliar con ejemplo práctico
UPDATE teorias SET contenido = 
'Scrum es un framework ágil liviano que ayuda a personas, equipos y organizaciones a generar valor a través de soluciones adaptativas para problemas complejos. Fue creado por Ken Schwaber y Jeff Sutherland y está documentado en la Scrum Guide (2020).

A diferencia de los métodos tradicionales (como Cascada), Scrum no planifica todo al inicio. En cambio, entrega valor en ciclos cortos llamados Sprints, permitiendo ajustar el rumbo según el feedback real.

Ejemplo práctico: un equipo que usa Scrum para construir una app no diseña toda la app antes de escribir código. En cada Sprint entrega algo funcional — primero el login, luego el perfil, luego las notificaciones — y aprende de cada entrega.'
WHERE id = 1;

UPDATE teorias SET contenido = 
'Scrum se basa en tres pilares empíricos que guían todas las decisiones del equipo:

• Transparencia: el estado real del trabajo es visible para todos. El tablero Kanban, el backlog y los impedimentos no se ocultan.

• Inspección: el equipo revisa constantemente su avance. Las ceremonias (Daily, Review, Retrospectiva) son puntos formales de inspección.

• Adaptación: cuando algo no funciona, se cambia. Si el sprint va mal a mitad de camino, el equipo ajusta su plan — no espera al final.

Estos tres pilares son la razón por la que Scrum funciona en entornos de alta incertidumbre: no se finge que todo está bajo control, se trabaja con la realidad.'
WHERE id = 2;

UPDATE teorias SET contenido = 
'El equipo Scrum tiene cinco valores que no son decorativos — son el fundamento del trabajo diario:

• Compromiso: cada miembro se compromete con los objetivos del Sprint, no solo con sus tareas individuales.

• Foco: durante el Sprint, el equipo se concentra en el Sprint Goal. Las distracciones y cambios de alcance se gestionan.

• Apertura: se habla con honestidad sobre el progreso, los problemas y las incertidumbres. No hay "todo va bien" cuando no va bien.

• Respeto: se reconoce que cada persona tiene habilidades y perspectivas valiosas. No hay jerarquías de conocimiento.

• Coraje: implica decir "no sé", "esto no va a funcionar" o "cometí un error". Es el valor más difícil de mantener en la práctica.'
WHERE id = 3;

UPDATE teorias SET contenido = 
'Aunque Scrum nació en el desarrollo de software (Sutherland lo implementó por primera vez en Easel Corporation en 1993), hoy se aplica en:

• Marketing y contenido: sprints de campañas, A/B testing iterativo
• Hardware y manufactura: diseño de productos físicos con prototipos rápidos
• Educación: diseño curricular iterativo
• Investigación científica: experimentos en ciclos cortos con revisión de hipótesis
• Construcción y arquitectura: diseño de espacios con feedback temprano

El denominador común: Scrum es útil cuando el problema es complejo y la solución no se conoce completamente de antemano. Si el problema es complicado pero conocido (construir un puente según especificaciones fijas), los métodos tradicionales pueden ser más adecuados.'
WHERE id = 4;

-- Módulo 3: Roles — mejorar con responsabilidades concretas
UPDATE teorias SET contenido = 
'Un equipo Scrum ideal tiene entre 3 y 9 personas (10 como máximo). Tiene tres roles exactos: Product Owner, Scrum Master y Developers. No hay sub-equipos, no hay líder técnico separado, no hay gerente de proyecto.

Son multifuncionales: entre todos tienen las habilidades para entregar el producto sin depender de externos. Son autogestionados: deciden internamente cómo hacer el trabajo.

Una confusión frecuente: el Scrum Master NO es el jefe del equipo. El Product Owner NO asigna tareas. Los Developers NO son solo programadores — pueden ser diseñadores, testers, analistas, etc.'
WHERE id = 5;

UPDATE teorias SET contenido = 
'El Product Owner (PO) es una sola persona — no un comité — responsable de maximizar el valor del producto. Sus responsabilidades concretas:

• Definir y comunicar el Product Goal
• Crear y ordenar el Product Backlog (lo más valioso, primero)
• Asegurarse de que el backlog sea transparente, visible y entendido
• Decidir qué se construye y en qué orden — pero NO cómo se construye

El PO trabaja con stakeholders para entender necesidades, y con el equipo para asegurar que el backlog sea estimable. Si el PO no está disponible, el equipo no puede trabajar bien: las decisiones de valor se bloquean.'
WHERE id = 6;

UPDATE teorias SET contenido = 
'El Scrum Master es un líder servicial — su trabajo es servir al equipo, no dirigirlo. Sus responsabilidades:

• Facilitar todas las ceremonias Scrum (Planning, Daily, Review, Retro)
• Eliminar impedimentos: bloqueos externos que el equipo no puede resolver solo
• Proteger al equipo de interrupciones y cambios de alcance durante el Sprint
• Entrenar al equipo en prácticas ágiles
• Ayudar al Product Owner a gestionar el backlog efectivamente

Lo que el Scrum Master NO hace: asignar tareas, tomar decisiones técnicas, reportar métricas a la gerencia para controlar al equipo, ni actuar como "secretario" de las reuniones.'
WHERE id = 7;

UPDATE teorias SET contenido = 
'Los Developers son quienes crean el Increment. Sus responsabilidades en Scrum:

• Crear el plan del Sprint (Sprint Backlog) durante el Planning
• Mantener la calidad — son responsables de aplicar la Definition of Done
• Adaptar su plan diariamente en el Daily Scrum
• Responsabilizarse mutuamente como profesionales

Importante: "Developer" no significa solo programador. Un equipo Scrum puede tener diseñadores UX, testers, analistas de datos o especialistas en seguridad — todos son Developers en Scrum.

También importante: los Developers deciden CÓMO hacer el trabajo. El PO decide QUÉ construir. Esta separación es fundamental para la autogestión del equipo.'
WHERE id = 8;

-- Módulo 5: Ceremonias — mejorar con duraciones y propósitos
UPDATE teorias SET contenido = 
'El Sprint es el contenedor de todo el trabajo en Scrum. Características clave:

• Duración fija: entre 1 y 4 semanas. Una vez definida, no cambia.
• Durante el Sprint: no se cambia el Sprint Goal, la calidad no baja, el equipo no se modifica.
• El Sprint Goal puede hacer que algunas tareas planificadas cambien, pero el objetivo permanece.
• Un Sprint puede cancelarse si el Sprint Goal se vuelve obsoleto — solo el Product Owner puede cancelarlo.

¿Por qué sprints cortos? Porque reducen el riesgo. Si algo sale mal, solo perdiste 1-2 semanas, no 6 meses. El feedback llega antes y los ajustes cuestan menos.'
WHERE id = 9;

UPDATE teorias SET contenido = 
'El Sprint Planning define el trabajo del Sprint. Duración máxima: 8 horas para Sprints de 4 semanas (proporcional para sprints más cortos).

Se responden tres preguntas:
1. ¿Por qué es valioso este Sprint? → El equipo define el Sprint Goal junto con el PO.
2. ¿Qué se puede hacer? → Los Developers seleccionan items del Product Backlog que pueden completar.
3. ¿Cómo se hará el trabajo? → Los Developers descomponen los items en tareas de 1 día o menos.

El resultado es el Sprint Backlog: el Sprint Goal + los items seleccionados + el plan de acción.

Error común: el PO dicta qué va en el sprint. En realidad, los Developers deciden cuánto pueden comprometer basándose en su velocidad histórica.'
WHERE id = 10;

UPDATE teorias SET contenido = 
'El Daily Scrum es una reunión de 15 minutos exactos, todos los días, para los Developers. Su propósito: inspeccionar el progreso hacia el Sprint Goal y adaptar el Sprint Backlog.

No es un reporte de estado para el Scrum Master ni para el PO. Es una reunión de planificación táctica del equipo para sí mismo.

La Scrum Guide no prescribe un formato específico. El clásico "¿Qué hice ayer? ¿Qué haré hoy? ¿Tengo impedimentos?" es solo un ejemplo. Lo importante es que el equipo sincronice y detecte problemas temprano.

Si la reunión toma más de 15 minutos, hay una conversación más profunda que debe continuar fuera del Daily — no en él.'
WHERE id = 11;

UPDATE teorias SET contenido = 
'La Sprint Review ocurre al final del Sprint. Duración máxima: 4 horas para Sprints de 4 semanas.

El equipo presenta el Increment a los stakeholders. Se inspeciona lo que se logró y se adapta el Product Backlog según el feedback recibido.

Es una sesión de trabajo colaborativa — no una demo formal ni una presentación de PowerPoint. Los stakeholders hacen preguntas, sugieren cambios, priorizan según lo que ven.

El output clave: un Product Backlog actualizado con las nuevas prioridades producto del feedback. El PO puede agregar, quitar o reordenar items en base a lo aprendido.'
WHERE id = 12;

UPDATE teorias SET contenido = 
'La Retrospectiva cierra el Sprint. Duración máxima: 3 horas para Sprints de 4 semanas.

Solo participa el Scrum Team (sin stakeholders externos). El equipo reflexiona sobre:
• ¿Qué salió bien y debe mantenerse?
• ¿Qué no funcionó y debe cambiar?
• ¿Qué mejoras concretas implementaremos en el próximo Sprint?

La clave es que la Retrospectiva produce acciones concretas y medibles — no solo una lista de quejas. Al menos una mejora debe incorporarse al siguiente Sprint Backlog.

Error frecuente: equipos que tienen Retrospectivas donde siempre dicen lo mismo pero nunca cambian nada. Si eso ocurre, la Retrospectiva no está funcionando.'
WHERE id = 13;

-- Módulo 10: Estimación — mejorar con ejemplos
UPDATE teorias SET contenido = 
'Los Story Points son una unidad de medida relativa — no representan horas ni días. Miden el esfuerzo, complejidad e incertidumbre de una historia de usuario combinados.

¿Por qué no horas? Porque las estimaciones en horas son consistentemente incorrectas. Los Story Points son más honestos: "esta historia es el doble de compleja que aquella" es más fácil de estimar que "esta historia tarda exactamente 6 horas".

Escala común: Fibonacci modificado (1, 2, 3, 5, 8, 13, 21). Los números grandes reflejan la incertidumbre creciente.

Regla importante: los Story Points son del equipo. No se pueden comparar entre equipos distintos. Un equipo que completa 40 SP por Sprint no es "más productivo" que uno que completa 20 SP — pueden estar usando escalas completamente diferentes.'
WHERE id = 18;

UPDATE teorias SET contenido = 
'Planning Poker es la técnica de estimación más usada en Scrum. El proceso:

1. El PO lee una historia de usuario
2. Cada Developer elige en secreto una carta con su estimación (valores Fibonacci)
3. Todos revelan sus cartas simultáneamente
4. Si hay consenso (o diferencias pequeñas): se acepta la estimación
5. Si hay divergencia grande: los que eligieron el valor más alto y más bajo explican su razonamiento
6. Se repite hasta llegar a consenso

¿Por qué simultáneo? Para evitar el "anchoring bias": si alguien dice su número primero, los demás tienden a ajustarse a él inconscientemente. La revelación simultánea elimina ese sesgo y produce estimaciones más honestas.'
WHERE id = 19;

UPDATE teorias SET contenido = 
'La velocidad es la cantidad promedio de Story Points que el equipo completa por Sprint. Se calcula promediando los últimos 3-5 Sprints.

Usos legítimos de la velocidad:
• Planificar cuánto trabajo comprometer en el próximo Sprint
• Hacer proyecciones de fecha de entrega ("¿cuántos Sprints necesitamos para completar el backlog?")
• Detectar que algo está afectando al equipo (si la velocidad cae sin explicación)

Usos incorrectos de la velocidad:
• Comparar equipos entre sí
• Presionar al equipo para aumentarla Sprint a Sprint
• Usarla como métrica de desempeño individual

Una velocidad inflada artificialmente (estimando las historias más grandes para parecer productivos) solo engaña la planificación.'
WHERE id = 20;

-- Módulo 14: Métricas — mejorar
UPDATE teorias SET contenido = 
'El Burndown Chart es la métrica visual más usada en Scrum. Muestra cuánto trabajo queda por hacer en función del tiempo.

Ejes: X = días del Sprint, Y = Story Points restantes.
Dos líneas: la línea ideal (progreso lineal perfecto) y la línea real (cómo avanza el equipo realmente).

Cómo leerlo:
• Línea real por encima de la ideal: el equipo va atrasado
• Línea real por debajo: el equipo va adelantado (¿sobreestimaron? ¿pueden comprometer más?)
• Línea plana varios días: hay impedimentos o el trabajo no se está actualizando

Se usa en el Daily Scrum para visualizar si el Sprint Goal está en riesgo.'
WHERE id = 26;

UPDATE teorias SET contenido = 
'El Burnup Chart muestra el trabajo completado acumulado versus el scope total del Sprint o release.

Ventaja sobre el Burndown: permite visualizar el "scope creep" (cuando se agregan tareas al sprint después de iniciar). En el Burndown, si se agregan tareas, la línea no baja aunque el equipo trabaje — lo cual confunde. En el Burnup, se ven dos líneas separadas: scope total y trabajo completado.

Cuándo preferir Burnup sobre Burndown: cuando el alcance cambia frecuentemente durante el Sprint, o para releases de varios Sprints donde el backlog crece.'
WHERE id = 27;


-- ============================================================
-- 3. REEMPLAZAR PREGUNTAS DEL EXAMEN BÁSICO (cuestionario 9)
-- Con preguntas de aplicación y síntesis, no repetición
-- ============================================================

UPDATE preguntas SET 
texto = 'Un equipo Scrum lleva 3 días en el Sprint y el desarrollador senior dice "necesitamos cambiar el Sprint Goal porque el cliente cambió de opinión". ¿Qué debería hacer el Scrum Master?',
opciones = '["Aceptar el cambio de inmediato para satisfacer al cliente","Cancelar el Sprint si el cambio hace el Sprint Goal obsoleto, o proteger el Sprint Goal si sigue siendo válido","Ignorar al desarrollador y continuar","Pedir al Product Owner que asigne más tareas"]',
respuesta_correcta = 'Cancelar el Sprint si el cambio hace el Sprint Goal obsoleto, o proteger el Sprint Goal si sigue siendo válido'
WHERE id = 21;

UPDATE preguntas SET 
texto = 'El Product Owner quiere agregar una tarea urgente al Sprint Backlog a mitad del Sprint. Los Developers dicen que no cabe sin comprometer la calidad. ¿Quién tiene razón?',
opciones = '["El Product Owner, porque es quien decide el backlog","Los Developers, porque son quienes gestionan el Sprint Backlog y su capacidad","El Scrum Master, quien debe mediar y decidir","Ambos tienen razón por igual"]',
respuesta_correcta = 'Los Developers, porque son quienes gestionan el Sprint Backlog y su capacidad'
WHERE id = 22;

UPDATE preguntas SET 
texto = 'En la Sprint Review, los stakeholders ven el Increment y dicen "esto no es lo que pedimos". ¿Cuál es el resultado más valioso de esta situación?',
opciones = '["Cancelar el proyecto","Reprochar al equipo por no hacer lo correcto","Actualizar el Product Backlog con las nuevas prioridades aprendidas del feedback","Extender el Sprint para corregir el problema"]',
respuesta_correcta = 'Actualizar el Product Backlog con las nuevas prioridades aprendidas del feedback'
WHERE id = 23;

UPDATE preguntas SET 
texto = 'Un equipo acaba de terminar su quinto Sprint. En la Retrospectiva identifican siempre los mismos problemas pero nunca cambia nada. ¿Qué está fallando?',
opciones = '["La duración del Sprint es muy corta","La Retrospectiva no está generando acciones concretas que se incorporen al siguiente Sprint","El Scrum Master no facilita bien las reuniones","El Product Owner no asiste a la Retrospectiva"]',
respuesta_correcta = 'La Retrospectiva no está generando acciones concretas que se incorporen al siguiente Sprint'
WHERE id = 24;

UPDATE preguntas SET 
texto = 'Un item del Product Backlog fue "completado" pero no pasó las pruebas de integración. Según Scrum, ¿qué ocurre con este item?',
opciones = '["Se incluye en el Increment de todas formas si el equipo lo decidió","No puede incluirse en el Increment porque no cumple la Definition of Done","Se mueve al siguiente Sprint automáticamente","El Product Owner decide si se incluye o no"]',
respuesta_correcta = 'No puede incluirse en el Increment porque no cumple la Definition of Done'
WHERE id = 25;

UPDATE preguntas SET 
texto = '¿Cuál es la diferencia entre el Sprint Goal y el Sprint Backlog?',
opciones = '["Son lo mismo","El Sprint Goal es el objetivo del Sprint; el Sprint Backlog es el conjunto de tareas para lograrlo","El Sprint Backlog define el objetivo; el Sprint Goal lista las tareas","El Sprint Goal lo define el Scrum Master; el Sprint Backlog el Product Owner"]',
respuesta_correcta = 'El Sprint Goal es el objetivo del Sprint; el Sprint Backlog es el conjunto de tareas para lograrlo'
WHERE id = 26;

UPDATE preguntas SET 
texto = 'Un gerente externo asiste al Daily Scrum y comienza a hacer preguntas al equipo. ¿Qué debería hacer el Scrum Master?',
opciones = '["Permitirlo, el Daily es una reunión abierta","Dejar que el gerente participe si el Product Owner lo aprueba","Proteger al equipo: el Daily es para los Developers; el gerente puede observar en silencio","Cancelar el Daily y reprogramarlo"]',
respuesta_correcta = 'Proteger al equipo: el Daily es para los Developers; el gerente puede observar en silencio'
WHERE id = 27;

UPDATE preguntas SET 
texto = 'El equipo completa consistentemente entre 18 y 22 Story Points por Sprint. ¿Cuántos SP debería comprometer en el siguiente Sprint Planning?',
opciones = '["30 SP para desafiarse a sí mismos","10 SP para tener margen","Alrededor de 20 SP basándose en su velocidad histórica","Los que el Product Owner indique"]',
respuesta_correcta = 'Alrededor de 20 SP basándose en su velocidad histórica'
WHERE id = 28;

UPDATE preguntas SET 
texto = '¿Por qué Scrum no recomienda comparar la velocidad entre distintos equipos?',
opciones = '["Porque la velocidad es confidencial","Porque cada equipo usa su propia escala de Story Points, haciendo la comparación inútil","Porque la velocidad solo la conoce el Scrum Master","Porque los Story Points son iguales en todos los equipos"]',
respuesta_correcta = 'Porque cada equipo usa su propia escala de Story Points, haciendo la comparación inútil'
WHERE id = 29;

UPDATE preguntas SET 
texto = 'El Increment al final del Sprint debe ser "potencialmente entregable". ¿Qué significa esto en la práctica?',
opciones = '["Que se entregará al cliente obligatoriamente","Que cumple la Definition of Done y podría usarse, aunque el Product Owner decida no lanzarlo aún","Que está documentado correctamente","Que el Scrum Master lo aprobó"]',
respuesta_correcta = 'Que cumple la Definition of Done y podría usarse, aunque el Product Owner decida no lanzarlo aún'
WHERE id = 30;


-- ============================================================
-- 4. REEMPLAZAR PREGUNTAS DEL EXAMEN AVANZADO (cuestionario 18)
-- Con preguntas de análisis y escenarios reales
-- ============================================================

UPDATE preguntas SET 
texto = 'El equipo nota que su velocidad bajó de 40 a 22 SP en los últimos 3 Sprints. ¿Cuál es el primer paso correcto?',
opciones = '["Presionar al equipo para recuperar la velocidad","Investigar en la Retrospectiva qué cambió: personas, proceso, complejidad técnica o deuda técnica","Aumentar la duración del Sprint","Reducir los Story Points de las historias para que parezca que se completan más"]',
respuesta_correcta = 'Investigar en la Retrospectiva qué cambió: personas, proceso, complejidad técnica o deuda técnica'
WHERE id = 51;

UPDATE preguntas SET 
texto = 'En un Planning Poker, tres Developers votan 3 SP y uno vota 21 SP para la misma historia. ¿Cuál es el siguiente paso correcto?',
opciones = '["Promediar los valores y usar 9 SP","Descartar el valor de 21 por ser un outlier","Pedir al Developer que votó 21 que explique su razonamiento — puede estar viendo riesgos que los demás no","Usar el valor más bajo para ser optimistas"]',
respuesta_correcta = 'Pedir al Developer que votó 21 que explique su razonamiento — puede estar viendo riesgos que los demás no'
WHERE id = 52;

UPDATE preguntas SET 
texto = 'Un equipo tiene una velocidad de 30 SP y el Product Backlog tiene 180 SP pendientes. ¿Cuántos Sprints de 2 semanas se necesitan aproximadamente para terminarlo?',
opciones = '["3 Sprints","6 Sprints","12 Sprints","No se puede calcular con esa información"]',
respuesta_correcta = '6 Sprints'
WHERE id = 53;

UPDATE preguntas SET 
texto = 'Al revisar el Burndown Chart a mitad del Sprint, la línea real está significativamente por encima de la línea ideal. ¿Cuál es la acción más apropiada?',
opciones = '["Extender el Sprint automáticamente","En el Daily Scrum, el equipo discute qué está bloqueando el avance y adapta el plan","Eliminar items del Sprint Backlog sin consultar al Product Owner","Ignorarlo y esperar a ver si mejora"]',
respuesta_correcta = 'En el Daily Scrum, el equipo discute qué está bloqueando el avance y adapta el plan'
WHERE id = 54;

UPDATE preguntas SET 
texto = 'Una organización tiene 5 equipos Scrum trabajando en el mismo producto. ¿Cuál es el principal desafío que deben resolver?',
opciones = '["Cada equipo debe tener su propio Product Owner","Coordinar dependencias entre equipos sin perder la agilidad de cada uno","Usar el mismo número de Story Points en todos los equipos","Tener el mismo Scrum Master para todos"]',
respuesta_correcta = 'Coordinar dependencias entre equipos sin perder la agilidad de cada uno'
WHERE id = 55;

UPDATE preguntas SET 
texto = 'Un item del backlog tiene criterios de aceptación vagos como "debe ser rápido". ¿Cumple la Definition of Ready?',
opciones = '["Sí, mientras el equipo lo entienda","No, los criterios de aceptación deben ser específicos y medibles (ej: carga en menos de 2 segundos)","Depende del Product Owner","Sí, si el Scrum Master lo aprueba"]',
respuesta_correcta = 'No, los criterios de aceptación deben ser específicos y medibles (ej: carga en menos de 2 segundos)'
WHERE id = 56;

UPDATE preguntas SET 
texto = 'El equipo está acumulando deuda técnica Sprint a Sprint. ¿Cuál es la forma correcta de gestionarla en Scrum?',
opciones = '["Ignorarla hasta que afecte la entrega","Dedicar un Sprint completo solo a deuda técnica cada 3 meses","Incluir items de deuda técnica en el Product Backlog y priorizarlos junto con las demás historias","Crear un equipo separado solo para deuda técnica"]',
respuesta_correcta = 'Incluir items de deuda técnica en el Product Backlog y priorizarlos junto con las demás historias'
WHERE id = 57;

UPDATE preguntas SET 
texto = '¿Cuándo es preferible usar un Burnup Chart en lugar de un Burndown Chart?',
opciones = '["Siempre, el Burnup es superior","Cuando el alcance del Sprint cambia frecuentemente y se quiere visualizar el scope creep","Cuando el equipo tiene poca experiencia","Cuando los Sprints duran más de 4 semanas"]',
respuesta_correcta = 'Cuando el alcance del Sprint cambia frecuentemente y se quiere visualizar el scope creep'
WHERE id = 58;

UPDATE preguntas SET 
texto = 'En SAFe, ¿qué es un ART (Agile Release Train)?',
opciones = '["Un solo equipo Scrum muy grande","Un conjunto de equipos Scrum alineados que trabajan juntos en un flujo de valor común","Un tipo de Sprint más largo","Una ceremonia adicional de SAFe"]',
respuesta_correcta = 'Un conjunto de equipos Scrum alineados que trabajan juntos en un flujo de valor común'
WHERE id = 59;

UPDATE preguntas SET 
texto = 'Un equipo usa Scrum of Scrums. ¿Cuál es el objetivo principal de esa reunión?',
opciones = '["Reemplazar el Daily Scrum de cada equipo","Reportar avances a la gerencia","Sincronizar dependencias entre equipos e identificar impedimentos compartidos","Planificar el próximo Sprint de todos los equipos juntos"]',
respuesta_correcta = 'Sincronizar dependencias entre equipos e identificar impedimentos compartidos'
WHERE id = 60;

