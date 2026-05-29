// ============================================================
// TOAST — Sistema profesional de notificaciones
// ============================================================
(function () {
  const container = document.createElement("div");
  container.id = "toast-container";
  document.body.appendChild(container);
})();

function toast(mensaje, tipo = "success", duracion = 3500) {
  const iconos = { success: "✅", error: "❌", warning: "⚠️", info: "ℹ️" };
  const container = document.getElementById("toast-container");
  const t = document.createElement("div");
  t.className = `toast toast-${tipo}`;
  t.innerHTML = `
    <span class="toast-icon">${iconos[tipo] || "ℹ️"}</span>
    <span class="toast-msg">${mensaje}</span>
    <button class="toast-close" onclick="this.parentElement.remove()">✕</button>`;
  container.appendChild(t);
  requestAnimationFrame(() => t.classList.add("show"));
  setTimeout(() => {
    t.classList.add("hiding");
    setTimeout(() => t.remove(), 350);
  }, duracion);
}

// ============================================================
// CONFIRM DIALOG — Reemplaza confirm() nativo
// ============================================================
function confirmDialog(mensaje, titulo = "¿Estás seguro?", icono = "🗑️", okTexto = "Eliminar", okClass = "") {
  return new Promise((resolve) => {
    const overlay = document.createElement("div");
    overlay.className = "confirm-overlay";
    overlay.innerHTML = `
      <div class="confirm-box">
        <div class="confirm-icon">${icono}</div>
        <div class="confirm-title">${titulo}</div>
        <div class="confirm-msg">${mensaje}</div>
        <div class="confirm-btns">
          <button class="confirm-btn-cancel" id="cdc-cancel">Cancelar</button>
          <button class="confirm-btn-ok ${okClass}" id="cdc-ok">${okTexto}</button>
        </div>
      </div>`;
    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add("show"));
    const close = (val) => {
      overlay.classList.remove("show");
      setTimeout(() => overlay.remove(), 200);
      resolve(val);
    };
    overlay.querySelector("#cdc-cancel").onclick = () => close(false);
    overlay.querySelector("#cdc-ok").onclick    = () => close(true);
    overlay.onclick = (e) => { if (e.target === overlay) close(false); };
  });
}

// ============================================================
// SKELETON LOADERS
// ============================================================
function skeletonTarjeta() {
  return `<div class="skeleton-card">
    <div class="skeleton skeleton-line titulo"></div>
    <div class="skeleton skeleton-line desc"></div>
    <div class="skeleton skeleton-line short"></div>
  </div>`;
}

function mostrarSkeletons(contenedorId, cantidad = 3) {
  const el = document.getElementById(contenedorId);
  if (el) el.innerHTML = Array(cantidad).fill(skeletonTarjeta()).join("");
}

function estadoVacio(icono, titulo, desc) {
  return `<div class="empty-state">
    <div class="empty-state-icon">${icono}</div>
    <div class="empty-state-title">${titulo}</div>
    <div class="empty-state-desc">${desc}</div>
  </div>`;
}

// ============================================================
// URL BASE DEL BACKEND — cambia esto cuando reinicies el túnel
// ============================================================
const API_BASE = "";

// ============================================================
// ESTADO GLOBAL
// ============================================================
let tareaArrastrada = null;
let quizzes = [];
let preguntasActivas = [];
let indicePregunta = 0;
let puntajeAcumulado = 0;

function mostrarSeccion(nombre) {
  document
    .querySelectorAll(".seccion")
    .forEach((s) => s.classList.add("oculto"));

  const mapa = {
    inicio: "seccionInicio",
    tablero: "seccionTablero",
    quiz: "seccionQuiz",
    simulacion: "seccionSimulacion",
    progreso: "seccionProgreso",
    reportes: "seccionReportes",
    ajustes: "seccionAjustes",
    ayuda: "seccionAyuda",
  };

  const id = mapa[nombre];
  if (!id) return;
  document.getElementById(id)?.classList.remove("oculto");

  // Sidebar (desktop)
  document
    .querySelectorAll(".nav-link")
    .forEach((l) => l.classList.remove("active"));
  const navIds = {
    inicio: "navInicio",
    tablero: "navTablero",
    quiz: "navQuiz",
    simulacion: "navSimulacion",
    progreso: "navProgreso",
    reportes: "navReportes",
    ajustes: "navAjustes",
    ayuda: "navAyuda",
  };
  if (navIds[nombre])
    document.getElementById(navIds[nombre])?.classList.add("active");

  // Bottom nav (mobile)
  document
    .querySelectorAll(".mob-nav-item")
    .forEach((l) => l.classList.remove("active"));
  const mobNavIds = {
    inicio: "mobNavInicio",
    tablero: "mobNavTablero",
    quiz: "mobNavQuiz",
    simulacion: "mobNavSimulacion",
    progreso: "mobNavProgreso",
    reportes: "mobNavReportes",
  };
  if (mobNavIds[nombre])
    document.getElementById(mobNavIds[nombre])?.classList.add("active");

  if (nombre === "inicio") cargarInicio();
  if (nombre === "progreso") cargarProgreso();
  if (nombre === "reportes") cargarReportes();
  if (nombre === "quiz") cargarQuizzes();
  if (nombre === "simulacion") cargarSimulacion();
  if (nombre === "ajustes") cargarAjustes();
}

function cerrarSesion() {
  sessionStorage.removeItem("usuario");
  window.location.href = "login.html";
}

async function cargarTareas() {
  mostrarSkeletons("tareas-pendiente", 2);
  mostrarSkeletons("tareas-en_proceso", 1);
  mostrarSkeletons("tareas-completada", 1);
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  const res = await fetch(`${API_BASE}/tareas?usuarioId=${usuario.id}`);
  const tareas = await res.json();

  ["pendiente", "en_proceso", "completada"].forEach((estado) => {
    const el = document.getElementById("tareas-" + estado);
    if (el) el.innerHTML = "";
  });

  tareas.forEach((tarea) => {
    const estado = tarea.estado || "pendiente";
    const contenedor = document.getElementById("tareas-" + estado);
    if (contenedor) contenedor.appendChild(crearTarjetaTarea(tarea));
  });

  actualizarContadoresKanban(tareas);
  actualizarSprintProgress(tareas);
}

function actualizarContadoresKanban(tareas) {
  const contadores = { pendiente: 0, en_proceso: 0, completada: 0 };
  tareas.forEach((t) => {
    if (contadores[t.estado] !== undefined) contadores[t.estado]++;
  });

  Object.keys(contadores).forEach((e) => {
    const el = document.getElementById("count-" + e);
    if (el) el.textContent = contadores[e];
  });

  const activas = contadores.pendiente + contadores.en_proceso;
  const badgeEl = document.getElementById("badgeActivas");
  if (badgeEl) badgeEl.textContent = activas + " activas";
}

function actualizarSprintProgress(tareas) {
  const total = tareas.length;
  const hechas = tareas.filter((t) => t.estado === "completada").length;
  const activas = tareas.filter((t) => t.estado === "en_proceso").length;
  const pct = total > 0 ? Math.round((hechas / total) * 100) : 0;

  const barra = document.getElementById("barraProgreso");
  if (barra) barra.style.width = pct + "%";

  const nh = document.getElementById("numHechas");
  const na = document.getElementById("numActivas");
  if (nh) nh.textContent = hechas;
  if (na) na.textContent = activas;
}

async function guardarTarea() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  const titulo = document.getElementById("titulo").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();
  const fechaVencimiento = document.getElementById("fechaLimite").value;
  const prioridad = document.getElementById("numPrioridad").value;

  if (!titulo) {
    toast("El título es obligatorio", "warning"); return;
  }

  await fetch(API_BASE + "/tareas", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      titulo,
      descripcion,
      columnaId: 1,
      usuarioId: usuario.id,
      prioridad: prioridad ? parseInt(prioridad) : null,
      fechaVencimiento: fechaVencimiento
        ? fechaVencimiento + "T00:00:00"
        : null,
    }),
  });

  cerrarFormulario();
  toast("Tarea creada correctamente", "success");
  await cargarTareas();
}

async function cargarProgreso() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");

  const [resStats, resProgreso] = await Promise.all([
    fetch(`${API_BASE}/api/user/stats?usuarioId=${usuario.id}`),
    fetch(`${API_BASE}/api/user/progress?usuarioId=${usuario.id}`),
  ]);

  const stats = await resStats.json();
  const progreso = await resProgreso.json();

  const total = stats.total || 0;
  const completadas = stats.completadas || 0;
  const pendientes = stats.pendientes || 0;
  const enProceso = stats.enProceso || 0;
  const pctTareas = total > 0 ? Math.round((completadas / total) * 100) : 0;
  const quizzesCompletados = progreso.quizzesCompletados || 0;

  const usuarioActualizado = { ...usuario, nivel: progreso.nivel };
  sessionStorage.setItem("usuario", JSON.stringify(usuarioActualizado));

  const set = (id, v) => {
    const el = document.getElementById(id);
    if (el) el.textContent = v;
  };

  set("kpiCompletado", pctTareas + "%");
  set("kpiQuizzes", quizzesCompletados);
  set("kpiTotalTareas", total);
  set("pctTareasLabel", pctTareas + "%");

  const barra = document.getElementById("pctTareasBarra");
  if (barra) barra.style.width = pctTareas + "%";

  const detalleEl = document.getElementById("detalleEstados");
  if (detalleEl) {
    detalleEl.innerHTML = [
      { label: "Tareas pendientes", val: pendientes },
      { label: "Tareas en proceso", val: enProceso },
      { label: "Tareas completadas", val: completadas },
    ]
      .map(
        (item) => `
            <div class="modulo-progreso-item">
                <div class="modulo-progreso-header">
                    <span>${item.label}</span>
                    <span class="modulo-progreso-pct">${item.val}</span>
                </div>
            </div>
        `,
      )
      .join("");
  }

  const nivel = progreso.nivel === "avanzado" ? 2 : 1;
  set("nivelCirculo", nivel);
  set("nivelDesc", progreso.nivel === "avanzado" ? "Avanzado" : "Básico");
  set("quizzesAside", quizzesCompletados);

  // Show points
  const puntosEl = document.getElementById("progresosPuntos");
  if (puntosEl) {
    const u = JSON.parse(sessionStorage.getItem("usuario") || "null");
    puntosEl.textContent = (u ? u.puntos || 0 : 0) + " pts";
  }

  // Logros
  try {
    const resLogros = await fetch(
      `${API_BASE}/api/gamificacion/logros/${usuario.id}`,
    );
    const logros = await resLogros.json();
    const logrosEl = document.getElementById("logrosLista");
    if (logrosEl) {
      if (logros.length === 0) {
        logrosEl.innerHTML =
          '<p style="color:var(--text-secondary);font-size:0.85rem;">Aún no tienes logros. ¡Completa tareas y quizzes!</p>';
      } else {
        logrosEl.innerHTML = logros
          .map(
            (lu) => `
                    <div class="logro-item">
                        <span class="logro-icono">${lu.logro?.icono || "🏅"}</span>
                        <div>
                            <strong>${lu.logro?.nombre || ""}</strong>
                            <p>${lu.logro?.descripcion || ""}</p>
                        </div>
                        <span class="logro-pts">+${lu.logro?.puntosRecompensa || 0} pts</span>
                    </div>
                `,
          )
          .join("");
      }
    }
  } catch (e) {
    console.error("Error logros:", e);
  }
}

async function cargarReportes() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  const [resStats, resProgreso] = await Promise.all([
    fetch(`${API_BASE}/api/user/stats?usuarioId=${usuario.id}`),
    fetch(`${API_BASE}/api/user/progress?usuarioId=${usuario.id}`),
  ]);
  const stats = await resStats.json();
  const progreso = await resProgreso.json();

  const total = stats.total || 0;
  const pendientes = stats.pendientes || 0;
  const enProceso = stats.enProceso || 0;
  const completadas = stats.completadas || 0;
  const pctComp = total > 0 ? Math.round((completadas / total) * 100) : 0;
  const quizzes = progreso.quizzesCompletados || 0;

  // ── KPIs superiores ──
  const set = (id, v) => {
    const el = document.getElementById(id);
    if (el) el.textContent = v;
  };
  set("rptTotal", total);
  set("rptPendientes", pendientes);
  set("rptEnProceso", enProceso);
  set("rptTareasHechas", completadas);
  set("resumenTotal", total);
  set("resumenPendientes", pendientes);
  set("resumenEnProceso", enProceso);
  set("resumenCompletadas", completadas);

  // ── Reemplazar contenido del reporte con diseño mejorado ──
  const main = document.querySelector("#seccionReportes .reportes-main");
  if (!main) return;

  const pctPend = total > 0 ? Math.round((pendientes / total) * 100) : 0;
  const pctProc = total > 0 ? Math.round((enProceso / total) * 100) : 0;

  main.innerHTML = `
        <!-- Stat cards row -->
        <div class="reporte-grid">
            <div class="stat-card" style="--stat-color:#3a86c8">
                <span class="stat-card-num">${total}</span>
                <span class="stat-card-lbl">Total tareas</span>
            </div>
            <div class="stat-card" style="--stat-color:#ef9a9a">
                <span class="stat-card-num">${pendientes}</span>
                <span class="stat-card-lbl">Pendientes</span>
                <span class="stat-card-pct" style="color:#e57373">${pctPend}%</span>
            </div>
            <div class="stat-card" style="--stat-color:#fff176">
                <span class="stat-card-num">${enProceso}</span>
                <span class="stat-card-lbl">En proceso</span>
                <span class="stat-card-pct" style="color:#f9a825">${pctProc}%</span>
            </div>
            <div class="stat-card" style="--stat-color:#a5d6a7">
                <span class="stat-card-num">${completadas}</span>
                <span class="stat-card-lbl">Completadas</span>
                <span class="stat-card-pct" style="color:#4caf50">${pctComp}%</span>
            </div>
            <div class="stat-card" style="--stat-color:#ce93d8">
                <span class="stat-card-num">${quizzes}</span>
                <span class="stat-card-lbl">Quizzes OK</span>
            </div>
            <div class="stat-card" style="--stat-color:#80cbc4">
                <span class="stat-card-num">${progreso.nivel === "avanzado" ? "🎓" : "📘"}</span>
                <span class="stat-card-lbl">Nivel actual</span>
                <span class="stat-card-pct" style="color:var(--accent)">${progreso.nivel === "avanzado" ? "Avanzado" : "Básico"}</span>
            </div>
        </div>

        <!-- Charts row -->
        <div class="charts-row" style="margin-bottom:20px;">
            <!-- Donut de completado -->
            <div class="chart-card">
                <p class="chart-card-title">Distribución por estado</p>
                <div class="donut-wrap">
                    <canvas id="graficaDonut" width="130" height="130"></canvas>
                    <div class="donut-legend">
                        <div class="legend-item">
                            <span class="legend-dot" style="background:#ef9a9a"></span>
                            Pendientes
                            <span class="legend-val">${pendientes}</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-dot" style="background:#fff176;border:1px solid #f9a825"></span>
                            En proceso
                            <span class="legend-val">${enProceso}</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-dot" style="background:#a5d6a7"></span>
                            Completadas
                            <span class="legend-val">${completadas}</span>
                        </div>
                        <div class="legend-item" style="border-top:1px solid var(--border);padding-top:8px;margin-top:4px;">
                            <span style="font-weight:700;color:var(--text-primary)">Tasa de completado</span>
                            <span class="legend-val" style="color:${pctComp >= 70 ? "#4caf50" : pctComp >= 40 ? "#f9a825" : "#e57373"}">${pctComp}%</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Barras horizontales por estado -->
            <div class="chart-card">
                <p class="chart-card-title">Progreso por estado</p>
                ${
                  total === 0
                    ? '<p style="color:var(--text-muted);font-size:0.85rem;text-align:center;padding:24px 0;">Sin tareas aún</p>'
                    : `
                <div class="estado-bar-wrap">
                    <div class="estado-bar-item">
                        <div class="estado-bar-header">
                            <span class="estado-bar-label">📋 Pendientes</span>
                            <span class="estado-bar-count">${pendientes} — ${pctPend}%</span>
                        </div>
                        <div class="estado-bar-track">
                            <div class="estado-bar-fill" style="width:${pctPend}%;background:#ef9a9a"></div>
                        </div>
                    </div>
                    <div class="estado-bar-item">
                        <div class="estado-bar-header">
                            <span class="estado-bar-label">⚙️ En proceso</span>
                            <span class="estado-bar-count">${enProceso} — ${pctProc}%</span>
                        </div>
                        <div class="estado-bar-track">
                            <div class="estado-bar-fill" style="width:${pctProc}%;background:#f9a825"></div>
                        </div>
                    </div>
                    <div class="estado-bar-item">
                        <div class="estado-bar-header">
                            <span class="estado-bar-label">✅ Completadas</span>
                            <span class="estado-bar-count">${completadas} — ${pctComp}%</span>
                        </div>
                        <div class="estado-bar-track">
                            <div class="estado-bar-fill" style="width:${pctComp}%;background:#4caf50"></div>
                        </div>
                    </div>
                </div>
                `
                }

                <!-- Análisis rápido -->
                <div style="margin-top:20px;padding-top:16px;border-top:1px solid var(--border);">
                    <p style="font-size:0.78rem;font-weight:700;text-transform:uppercase;letter-spacing:0.04em;color:var(--text-muted);margin-bottom:10px;">Análisis</p>
                    <p style="font-size:0.83rem;color:var(--text-secondary);line-height:1.6;">
                        ${
                          total === 0
                            ? "Aún no tienes tareas. ¡Crea tu primera tarea en el tablero!"
                            : pctComp >= 80
                              ? "🎉 ¡Excelente progreso! Tienes más del 80% completado."
                              : pctComp >= 50
                                ? "📈 Buen ritmo. Ya superaste la mitad del trabajo."
                                : pendientes > completadas
                                  ? "📋 Tienes más tareas pendientes que completadas. ¡A trabajar!"
                                  : "⚙️ Estás en proceso. Sigue adelante con las tareas activas."
                        }
                    </p>
                </div>
            </div>
        </div>

        <!-- Gráfica de barras Chart.js -->
        <div class="chart-card" style="margin:0 28px 20px;">
            <p class="chart-card-title">Resumen de tareas</p>
            ${
              total === 0
                ? '<p style="color:var(--text-muted);font-size:0.85rem;text-align:center;padding:24px 0;">Sin datos que mostrar</p>'
                : '<div style="position:relative;height:110px;"><canvas id="graficaEstados"></canvas></div>'
            }
        </div>
    `;

  if (total > 0) {
    dibujarDonut(document.getElementById("graficaDonut"), [
      pendientes,
      enProceso,
      completadas,
    ]);
    dibujarGrafica(stats);
  }
}

function dibujarDonut(canvas, valores) {
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  const colores = ["#ef9a9a", "#fff176", "#a5d6a7"];
  const total = valores.reduce((a, b) => a + b, 0);
  if (total === 0) return;

  const cx = canvas.width / 2,
    cy = canvas.height / 2;
  const radio = Math.min(cx, cy) - 8;
  const radioInner = radio * 0.55;
  let angulo = -Math.PI / 2;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  valores.forEach((val, i) => {
    if (val === 0) return;
    const slice = (val / total) * 2 * Math.PI;
    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.arc(cx, cy, radio, angulo, angulo + slice);
    ctx.closePath();
    ctx.fillStyle = colores[i];
    ctx.fill();
    angulo += slice;
  });

  // Hueco central
  ctx.beginPath();
  ctx.arc(cx, cy, radioInner, 0, 2 * Math.PI);
  ctx.fillStyle =
    getComputedStyle(document.documentElement)
      .getPropertyValue("--card-bg")
      .trim() || "#fff";
  ctx.fill();

  // Texto central
  const pct = Math.round((valores[2] / total) * 100);
  ctx.fillStyle =
    getComputedStyle(document.documentElement)
      .getPropertyValue("--text-primary")
      .trim() || "#1a4f7a";
  ctx.font = `bold ${Math.round(radioInner * 0.55)}px sans-serif`;
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.fillText(pct + "%", cx, cy);
}

function crearTarjetaTarea(tarea) {
  const div = document.createElement("div");
  div.classList.add("tarjeta");
  div.setAttribute("draggable", "true");
  div.setAttribute("data-id", tarea.id);

  const prioridadTexto = tarea.prioridad ? `Prioridad: ${tarea.prioridad}` : "";
  const fechaTexto = tarea.fechaVencimiento
    ? `📅 ${tarea.fechaVencimiento}`
    : "";

  div.innerHTML = `
    <strong>${tarea.titulo}</strong>
    <p>${tarea.descripcion || ""}</p>
    <small>${prioridadTexto} ${fechaTexto}</small>
    <div class="tarjeta-acciones">
      <button class="btn-editar" onclick="abrirEdicion(${tarea.id}, '${tarea.titulo.replace(/'/g, "\\'")}', '${(tarea.descripcion || "").replace(/'/g, "\\'")}', '${tarea.prioridad || ""}', '${tarea.fechaVencimiento || ""}')">✏️</button>
      <button class="btn-eliminar" onclick="eliminarTarea(${tarea.id})">🗑️</button>
    </div>
  `;

  div.addEventListener("dragstart", () => {
    tareaArrastrada = tarea.id;
    div.classList.add("arrastrando");
  });

  div.addEventListener("dragend", () => {
    div.classList.remove("arrastrando");
    tareaArrastrada = null;
  });

  return div;
}

async function eliminarTarea(id) {
  if (!await confirmDialog("Esta acción no se puede deshacer.", "¿Eliminar tarea?", "🗑️", "Sí, eliminar")) return;
  const res = await fetch(`${API_BASE}/tareas/${id}`, { method: "DELETE" });
  if (res.ok) toast("Tarea eliminada", "info");
  else toast("Error al eliminar la tarea", "error");
  await cargarTareas();
}

function abrirEdicion(id, titulo, descripcion, prioridad, fecha) {
  document.getElementById("editId").value = id;
  document.getElementById("editTitulo").value = titulo;
  document.getElementById("editDescripcion").value = descripcion;
  document.getElementById("editPrioridad").value = prioridad;
  document.getElementById("editFecha").value = fecha;
  document.getElementById("modalEdicion").classList.remove("oculto");
}

async function guardarEdicion() {
  const id = document.getElementById("editId").value;
  const titulo = document.getElementById("editTitulo").value.trim();
  const descripcion = document.getElementById("editDescripcion").value.trim();
  const prioridad = document.getElementById("editPrioridad").value;
  const fecha = document.getElementById("editFecha").value;
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");

  if (!titulo) {
    toast("El título es obligatorio", "warning"); return;
  }

  const res = await fetch(`${API_BASE}/tareas/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      titulo,
      descripcion,
      usuarioId: usuario ? usuario.id : null,
      prioridad: prioridad ? parseInt(prioridad) : null,
      fechaVencimiento: fecha || null,
    }),
  });

  document.getElementById("modalEdicion").classList.add("oculto");
  if (res.ok) {
    toast("Tarea actualizada correctamente", "success");
  } else {
    toast("Error al actualizar la tarea", "error");
  }
  await cargarTareas();
}

function allowDrop(e) {
  e.preventDefault();
  e.currentTarget.classList.add("drop-activo");
}

async function drop(e) {
  e.preventDefault();
  e.currentTarget.classList.remove("drop-activo");

  const columna = e.currentTarget;
  const nuevoEstado = columna.getAttribute("data-estado");

  // Capturar el id ANTES de que dragend lo ponga en null
  const idTarea = tareaArrastrada;
  if (!idTarea) return;

  const res = await fetch(`${API_BASE}/tareas/${idTarea}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ estado: nuevoEstado }),
  });

  if (res.ok && nuevoEstado === "completada") {
    await notificarActividad("TAREA_COMPLETADA");
  }

  await cargarTareas();
}

document.querySelectorAll(".columna").forEach((col) => {
  col.addEventListener("dragleave", () => col.classList.remove("drop-activo"));
});

function cerrarFormulario() {
  document.getElementById("formulario").classList.add("oculto");
  document.getElementById("titulo").value = "";
  document.getElementById("descripcion").value = "";
  document.getElementById("fechaLimite").value = "";
  document.getElementById("numPrioridad").value = "";
}

function mostrarPrioridad() {
  const check = document.getElementById("prioridad");
  document.getElementById("numPrioridad").style.display = check.checked
    ? "none"
    : "block";
}

// ============================================================
// HU05/06 — CONTENIDO EDUCATIVO (NIVELES + TEORÍA + QUIZ)
// ============================================================
let nivelActivo = null;

async function cargarQuizzes() {
  nivelActivo = null;
  document.getElementById("listaQuizzesWrap").classList.remove("oculto");
  document.getElementById("vistaTeoria").classList.add("oculto");
  document.getElementById("vistaQuiz").classList.add("oculto");

  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  const qTotal = usuario ? usuario.quizzesCompletados || 0 : 0;
  const nivelUsr = usuario ? usuario.nivel || "basico" : "basico";
  const puntos = usuario ? usuario.puntos || 0 : 0;
  const basicoDone = nivelUsr === "avanzado" ? 4 : Math.min(qTotal, 4);
  const avDone =
    nivelUsr === "avanzado" ? Math.min(Math.max(qTotal - 5, 0), 4) : 0;
  const basicoPct = Math.round((basicoDone / 4) * 100);
  const avPct = Math.round((avDone / 4) * 100);
  const faltanBasico = 4 - basicoDone;

  const c = document.getElementById("listaQuizzes");

  const heroHTML =
    '<div class="nivel-hero">' +
    '<div class="nivel-hero-info"><h2 class="nivel-hero-titulo">Tu ruta de aprendizaje</h2>' +
    '<p class="nivel-hero-sub">Domina Scrum paso a paso — teoria, practica y examen por nivel</p></div>' +
    '<div class="nivel-hero-badges">' +
    '<span class="nivel-badge-pill ' +
    (nivelUsr === "avanzado" ? "badge-avanzado" : "badge-basico") +
    '">' +
    (nivelUsr === "avanzado"
      ? "&#127891; Nivel Avanzado"
      : "&#128216; Nivel Basico") +
    "</span>" +
    '<span class="nivel-pts-chip">&#9889; ' +
    puntos +
    " pts</span>" +
    "</div></div>";

  const basicoCardClass =
    nivelUsr !== "basico" ? "nivel-card-done" : "nivel-card-active";
  const basicoBtn =
    nivelUsr !== "basico"
      ? "&#128260; Repasar"
      : basicoDone > 0
        ? "&#9654; Continuar"
        : "&#9654; Comenzar";
  const basicoBtnClass =
    nivelUsr !== "basico" ? "nivel-btn-v2 btn-review" : "nivel-btn-v2";

  const basicoCard =
    '<div class="nivel-card-v2 ' +
    basicoCardClass +
    '">' +
    '<div class="nivel-card-v2-header">' +
    '<div class="nivel-icon-wrap basico-ico">&#128216;</div>' +
    '<div class="nivel-card-v2-meta"><h3>Nivel Basico</h3><p>Scrum: roles, ceremonias y artefactos</p></div>' +
    (nivelUsr !== "basico"
      ? '<span class="nivel-completado-chip">&#9989; Completado</span>'
      : "") +
    "</div>" +
    '<div class="nivel-card-v2-stats">' +
    '<div class="nivel-stat"><span class="nivel-stat-num">' +
    basicoDone +
    '/4</span><span class="nivel-stat-lbl">modulos</span></div>' +
    '<div class="nivel-stat"><span class="nivel-stat-num">~2h</span><span class="nivel-stat-lbl">estimado</span></div>' +
    '<div class="nivel-stat"><span class="nivel-stat-num">+200</span><span class="nivel-stat-lbl">puntos</span></div>' +
    "</div>" +
    '<div class="nivel-progress-wrap">' +
    '<div class="nivel-progress-header"><span>Progreso</span><span>' +
    basicoPct +
    "%</span></div>" +
    '<div class="nivel-progress-track"><div class="nivel-progress-fill basico-fill" style="width:' +
    basicoPct +
    '%"></div></div>' +
    "</div>" +
    '<div class="nivel-topics">' +
    '<span class="nivel-topic-chip">Que es Scrum</span><span class="nivel-topic-chip">Roles</span>' +
    '<span class="nivel-topic-chip">Ceremonias</span><span class="nivel-topic-chip">Artefactos</span>' +
    "</div>" +
    '<button class="' +
    basicoBtnClass +
    '" onclick="seleccionarNivel(\'basico\')">' +
    basicoBtn +
    "</button>" +
    "</div>";

  const avCardClass =
    nivelUsr === "basico"
      ? "nivel-card-locked"
      : avDone >= 4
        ? "nivel-card-done"
        : "nivel-card-active";
  const avBtn =
    nivelUsr === "basico"
      ? "&#128274; Bloqueado"
      : avDone > 0
        ? "&#9654; Continuar"
        : "&#9654; Comenzar";
  const avProgBar =
    nivelUsr !== "basico"
      ? '<div class="nivel-progress-wrap"><div class="nivel-progress-header"><span>Progreso</span><span>' +
        avPct +
        "%</span></div>" +
        '<div class="nivel-progress-track"><div class="nivel-progress-fill avanzado-fill" style="width:' +
        avPct +
        '%"></div></div></div>'
      : '<div class="nivel-unlock-info"><div class="nivel-unlock-track"><div class="nivel-unlock-fill" style="width:' +
        basicoPct +
        '%"></div></div>' +
        '<span class="nivel-unlock-text">Completa ' +
        faltanBasico +
        " modulo" +
        (faltanBasico !== 1 ? "s" : "") +
        " mas para desbloquear</span></div>";

  const avCard =
    '<div class="nivel-card-v2 ' +
    avCardClass +
    '">' +
    '<div class="nivel-card-v2-header">' +
    '<div class="nivel-icon-wrap ' +
    (nivelUsr === "basico" ? "locked-ico" : "avanzado-ico") +
    '">' +
    (nivelUsr === "basico" ? "&#128274;" : "&#128215;") +
    "</div>" +
    '<div class="nivel-card-v2-meta"><h3>Nivel Avanzado</h3><p>' +
    (nivelUsr === "basico"
      ? "Completa el Nivel Basico para desbloquear"
      : "Estimacion, metricas y Scrum a escala") +
    "</p></div>" +
    (avDone >= 4
      ? '<span class="nivel-completado-chip">&#9989; Completado</span>'
      : "") +
    "</div>" +
    '<div class="nivel-card-v2-stats' +
    (nivelUsr === "basico" ? " stats-locked" : "") +
    '">' +
    '<div class="nivel-stat"><span class="nivel-stat-num">' +
    (nivelUsr === "basico" ? "—" : avDone + "/4") +
    '</span><span class="nivel-stat-lbl">modulos</span></div>' +
    '<div class="nivel-stat"><span class="nivel-stat-num">~3h</span><span class="nivel-stat-lbl">estimado</span></div>' +
    '<div class="nivel-stat"><span class="nivel-stat-num">+300</span><span class="nivel-stat-lbl">puntos</span></div>' +
    "</div>" +
    avProgBar +
    '<div class="nivel-topics' +
    (nivelUsr === "basico" ? " topics-locked" : "") +
    '">' +
    '<span class="nivel-topic-chip">Estimacion</span><span class="nivel-topic-chip">Refinamiento</span>' +
    '<span class="nivel-topic-chip">Metricas</span><span class="nivel-topic-chip">Scrum a escala</span>' +
    "</div>" +
    '<button class="nivel-btn-v2" onclick="seleccionarNivel(\'avanzado\')"' +
    (nivelUsr === "basico" ? " disabled" : "") +
    ">" +
    avBtn +
    "</button>" +
    "</div>";

  function criterio(done, titulo, desc) {
    return (
      '<div class="nivel-criterio-item' +
      (done ? " criterio-done" : "") +
      '">' +
      '<span class="criterio-icon">' +
      (done ? "&#9989;" : "&#11036;") +
      "</span>" +
      "<div><strong>" +
      titulo +
      "</strong><p>" +
      desc +
      "</p></div></div>"
    );
  }

  const criteriosHTML =
    '<div class="nivel-criterios-card">' +
    '<p class="nivel-criterios-title">&#128203; Como funciona la progresion</p>' +
    '<div class="nivel-criterios-row">' +
    criterio(
      basicoDone >= 1,
      "Primer modulo basico",
      "Completa teoria y quiz del Modulo 1",
    ) +
    criterio(
      basicoDone >= 2,
      "Progreso en Basico",
      "Completa al menos 2 modulos del nivel basico",
    ) +
    criterio(
      nivelUsr === "avanzado",
      "Desbloquear Nivel Avanzado",
      "Completa los 4 modulos del nivel basico",
    ) +
    criterio(
      avDone >= 4,
      "Maestria Scrum",
      "Completa todos los modulos del nivel avanzado",
    ) +
    "</div></div>";

  c.innerHTML =
    heroHTML +
    '<div class="niveles-row-v2">' +
    basicoCard +
    avCard +
    "</div>" +
    criteriosHTML;
}

async function seleccionarNivel(nivel) {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  if (nivel === "avanzado" && usuario.nivel === "basico") {
    mostrarNotificacion("Debes completar el Nivel Basico primero", "error");
    return;
  }
  nivelActivo = nivel;
  const res = await fetch(API_BASE + "/api/quiz/nivel/" + nivel);
  const modulos = await res.json();

  document.getElementById("listaQuizzesWrap").classList.remove("oculto");
  document.getElementById("vistaTeoria").classList.add("oculto");
  document.getElementById("vistaQuiz").classList.add("oculto");

  const qTotal = usuario ? usuario.quizzesCompletados || 0 : 0;
  const nivelUsr = usuario ? usuario.nivel || "basico" : "basico";
  let modDone;
  if (nivel === "basico") {
    modDone = nivelUsr === "avanzado" ? 4 : Math.min(qTotal, 4);
  } else {
    modDone = Math.min(Math.max(qTotal - 5, 0), 4);
  }

  const teorias = modulos.filter(function (m) {
    return m.tipo === "teoria";
  });
  const quizzesNiv = modulos.filter(function (m) {
    return m.tipo === "quiz";
  });
  const examenes = modulos.filter(function (m) {
    return m.tipo === "examen";
  });
  const totalMods = teorias.length;
  const progPct = totalMods ? Math.round((modDone / totalMods) * 100) : 0;
  const fillClass = nivel === "basico" ? "basico-fill" : "avanzado-fill";

  const headerHTML =
    '<button onclick="cargarQuizzes()" class="btn-volver">&#8592; Volver a niveles</button>' +
    '<div class="nivel-detail-header">' +
    '<div class="nivel-detail-icon">' +
    (nivel === "basico" ? "&#128216;" : "&#128215;") +
    "</div>" +
    '<div class="nivel-detail-info">' +
    '<h2 class="nivel-detail-titulo">' +
    (nivel === "basico" ? "Nivel Basico" : "Nivel Avanzado") +
    "</h2>" +
    '<p class="nivel-detail-sub">' +
    totalMods +
    " modulos &nbsp;&#183;&nbsp; " +
    (nivel === "basico" ? "~2h" : "~3h") +
    " estimado &nbsp;&#183;&nbsp; " +
    modDone +
    "/" +
    totalMods +
    " completados</p>" +
    "</div>" +
    '<div class="nivel-detail-progress">' +
    '<div class="nivel-progress-track wide"><div class="nivel-progress-fill ' +
    fillClass +
    '" style="width:' +
    progPct +
    '%"></div></div>' +
    '<span class="nivel-detail-pct">' +
    progPct +
    "%</span>" +
    "</div></div>" +
    '<div class="modulos-lista-v2" id="modulosLista"></div>';

  const contenedor = document.getElementById("listaQuizzes");
  contenedor.innerHTML = headerHTML;
  const lista = document.getElementById("modulosLista");

  teorias.forEach(function (teoria, i) {
    var quiz = quizzesNiv[i];
    var done = i < modDone;
    var current = i === modDone;
    var locked = i > modDone;
    var statusClass = done
      ? "modulo-done"
      : current
        ? "modulo-current"
        : "modulo-locked";
    var statusLabel = done
      ? "Completado"
      : current
        ? "Disponible"
        : "Bloqueado";
    var numIcon = done ? "&#9989;" : current ? "&#9654;" : "&#128274;";

    var tituloSafe = teoria.titulo.replace(/'/g, "\\'");
    var quizBtn = quiz
      ? '<button class="btn-quiz-v2' +
        (done ? " btn-repaso" : "") +
        '" ' +
        (locked ? "disabled" : "") +
        ' onclick="iniciarQuiz(' +
        quiz.id +
        ", '" +
        quiz.titulo.replace(/'/g, "\\'") +
        "')\">" +
        (done ? "&#128260; Repasar quiz" : "&#9998; Hacer quiz") +
        "</button>" +
        '<button class="btn-scorm-v2" title="Descargar paquete SCORM 1.2 para LMS" onclick="descargarScorm(' +
        quiz.id +
        ", '" +
        quiz.titulo.replace(/'/g, "\\'") +
        "')\">" +
        "&#128230; SCORM" +
        "</button>"
      : "";

    var div = document.createElement("div");
    div.className = "modulo-card-v2 " + statusClass;
    div.innerHTML =
      '<div class="modulo-v2-left">' +
      '<div class="modulo-v2-num num-' +
      (done ? "done" : current ? "current" : "locked") +
      '">' +
      numIcon +
      "</div>" +
      "</div>" +
      '<div class="modulo-v2-body">' +
      '<div class="modulo-v2-top">' +
      '<span class="modulo-v2-label">Modulo ' +
      (i + 1) +
      "</span>" +
      '<span class="modulo-status-chip status-' +
      statusClass +
      '">' +
      statusLabel +
      "</span>" +
      "</div>" +
      '<h4 class="modulo-v2-titulo">' +
      teoria.titulo +
      "</h4>" +
      '<p class="modulo-v2-desc">' +
      (teoria.descripcion || "") +
      "</p>" +
      '<div class="modulo-v2-acciones">' +
      '<button class="btn-teoria-v2" ' +
      (locked ? "disabled" : "") +
      ' onclick="verTeoria(' +
      teoria.id +
      ", '" +
      tituloSafe +
      "', " +
      (i + 1) +
      ')">&#128218; Ver teoria</button>' +
      quizBtn +
      "</div></div>" +
      (done ? '<div class="modulo-v2-check">&#9989;</div>' : "");
    lista.appendChild(div);
  });

  if (examenes.length > 0) {
    var examen = examenes[0];
    var exAvail = modDone >= totalMods;
    var exClass = exAvail
      ? "modulo-current modulo-examen"
      : "modulo-locked modulo-examen";
    var exTitSafe = examen.titulo.replace(/'/g, "\\'");
    var div = document.createElement("div");
    div.className = "modulo-card-v2 " + exClass;
    div.innerHTML =
      '<div class="modulo-v2-left">' +
      '<div class="modulo-v2-num num-' +
      (exAvail ? "examen" : "locked") +
      '">' +
      (exAvail ? "&#127891;" : "&#128274;") +
      "</div>" +
      "</div>" +
      '<div class="modulo-v2-body">' +
      '<div class="modulo-v2-top">' +
      '<span class="modulo-v2-label examen-label">Examen Final</span>' +
      '<span class="modulo-status-chip status-' +
      (exAvail ? "modulo-current" : "modulo-locked") +
      '">' +
      (exAvail
        ? "Disponible"
        : "Completa " +
          (totalMods - modDone) +
          " modulo" +
          (totalMods - modDone !== 1 ? "s" : "") +
          " antes") +
      "</span>" +
      "</div>" +
      '<h4 class="modulo-v2-titulo">' +
      examen.titulo +
      "</h4>" +
      '<p class="modulo-v2-desc">' +
      (examen.descripcion || "") +
      "</p>" +
      '<div class="modulo-v2-acciones">' +
      '<button class="btn-quiz-v2 btn-examen" ' +
      (exAvail ? "" : "disabled") +
      ' onclick="iniciarQuiz(' +
      examen.id +
      ", '" +
      exTitSafe +
      "')\">&#127891; Iniciar examen</button>" +
      "</div></div>";
    lista.appendChild(div);
  }
}
function mostrarResultadoFinal() {
  document.getElementById("preguntaTexto").textContent = "";
  document.getElementById("preguntaContainer").innerHTML = "";
  document.getElementById("feedbackRespuesta").classList.add("oculto");
  document.getElementById("btnSiguiente").style.display = "none";
  document.getElementById("btnVerExplicacion").style.display = "none";

  const totalPts = preguntasActivas.reduce(function (a, p) {
    return a + (p.puntaje || 50);
  }, 0);
  const porcentaje =
    totalPts > 0 ? Math.round((puntajeAcumulado / totalPts) * 100) : 0;
  const aprobado = porcentaje >= 60;

  var emoji =
    porcentaje >= 90
      ? "&#127942;"
      : porcentaje >= 70
        ? "&#127919;"
        : porcentaje >= 60
          ? "&#9989;"
          : "&#10060;";
  var mensaje =
    porcentaje >= 90
      ? "Excelente dominio del tema!"
      : porcentaje >= 70
        ? "Muy bien — solido conocimiento."
        : porcentaje >= 60
          ? "Aprobado. Sigue practicando."
          : "No aprobado. Repasa el material e intentalo de nuevo.";

  var ptsGanados = aprobado
    ? Math.round((puntajeAcumulado / (totalPts || 1)) * 50)
    : 0;

  const resultadoEl = document.getElementById("resultadoFinal");
  resultadoEl.classList.remove("oculto");
  document.getElementById("puntuacionFinal").innerHTML =
    '<div class="resultado-emoji">' +
    emoji +
    "</div>" +
    '<div class="resultado-score">' +
    porcentaje +
    "%</div>" +
    '<p class="resultado-mensaje">' +
    mensaje +
    "</p>" +
    '<div class="resultado-detalle">' +
    '<div class="resultado-det correcto"><span class="rd-num">' +
    numCorrectas +
    '</span><span class="rd-lbl">Correctas</span></div>' +
    '<div class="resultado-det incorrecto"><span class="rd-num">' +
    numIncorrectas +
    '</span><span class="rd-lbl">Incorrectas</span></div>' +
    '<div class="resultado-det puntos"><span class="rd-num">+' +
    ptsGanados +
    '</span><span class="rd-lbl">Puntos</span></div>' +
    "</div>" +
    '<div class="resultado-barra-wrap">' +
    '<div class="resultado-barra-fill" style="width:' +
    porcentaje +
    "%;background:" +
    (porcentaje >= 70 ? "#4caf50" : porcentaje >= 60 ? "#f9a825" : "#e57373") +
    '"></div>' +
    "</div>";

  // Notificar al LMS si hay SCORM activo
  if (window.TaskFlowSCORM && window.TaskFlowSCORM.isActive()) {
    window.TaskFlowSCORM.reportarQuiz(porcentaje, aprobado);
  }

  if (aprobado) {
    const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
    if (usuario) {
      fetch(API_BASE + "/usuarios/" + usuario.id + "/completar-quiz", {
        method: "POST",
      })
        .then(function (r) {
          return r.json();
        })
        .then(function (usuarioAct) {
          sessionStorage.setItem("usuario", JSON.stringify(usuarioAct));
          if (usuarioAct.nivel !== usuario.nivel) {
            mostrarNotificacion(
              "&#127891; Nivel Avanzado desbloqueado! Felicitaciones!",
              "nivel",
              6000,
            );
            lanzarConfetti();
          }
          notificarActividad("QUIZ_COMPLETADO");
        });
    }
  } else {
    mostrarNotificacion("No alcanzaste el 60%. Intentalo de nuevo!", "error");
  }
}

async function verTeoria(cuestionarioId, titulo, numModulo) {
  const res = await fetch(`${API_BASE}/api/teoria/${cuestionarioId}`);
  const secciones = await res.json();

  document.getElementById("listaQuizzesWrap").classList.add("oculto");
  document.getElementById("vistaQuiz").classList.add("oculto");
  document.getElementById("vistaTeoria").classList.remove("oculto");

  document.getElementById("teoriaModuloSubtitulo").textContent =
    `Módulo ${numModulo || ""} — ${titulo}`;
  document.getElementById("asideModuloLabel").textContent =
    `MÓDULO ${numModulo || ""}`;
  document.getElementById("asideModuloNombre").textContent = titulo;

  tarjetasTeoria = secciones;
  indiceTarjeta = 0;
  renderTarjetaTeoria();
}

let tarjetasTeoria = [];
let indiceTarjeta = 0;

function renderTarjetaTeoria() {
  const total = tarjetasTeoria.length;
  const tarjeta = tarjetasTeoria[indiceTarjeta];
  const pct = Math.round(((indiceTarjeta + 1) / total) * 100);

  document.getElementById("badgeTarjeta").textContent =
    `Tarjeta ${indiceTarjeta + 1} / ${total}`;
  document.getElementById("asidePct").textContent = pct + "%";
  document.getElementById("asideBarra").style.width = pct + "%";

  document.getElementById("teoriaTags").innerHTML = `
    <span class="teoria-tag">${tarjeta.categoria || "Scrum"}</span>
    <span class="teoria-tag">Nivel básico</span>
    <span class="teoria-tag">5 min lectura</span>
  `;

  document.getElementById("teoriaTitulo").textContent = tarjeta.titulo || "";
  document.getElementById("teoriaIntro").textContent = tarjeta.contenido || "";

  const bodyEl = document.getElementById("teoriaBody");
  bodyEl.innerHTML = "";

  if (tarjeta.puntos && tarjeta.puntos.length) {
    const bloque = document.createElement("div");
    bloque.className = "teoria-bloque";
    bloque.innerHTML = `
      <p class="teoria-bloque-titulo">${tarjeta.puntosTitulo || "PUNTOS CLAVE"}</p>
      ${tarjeta.puntos
        .map(
          (p, i) => `
        <div class="teoria-bloque-item">
          <span class="teoria-num">${i + 1}</span>
          <span>${p}</span>
        </div>
      `,
        )
        .join("")}
    `;
    bodyEl.appendChild(bloque);
  }

  if (tarjeta.nota) {
    const nota = document.createElement("div");
    nota.className = "teoria-nota";
    nota.textContent = tarjeta.nota;
    bodyEl.appendChild(nota);
  }

  const temasEl = document.getElementById("asideTemas");
  if (temasEl) {
    temasEl.innerHTML = tarjetasTeoria
      .map(
        (t, i) => `
      <li class="${i === indiceTarjeta ? "activo" : ""}">${t.titulo || "Tema " + (i + 1)}</li>
    `,
      )
      .join("");
  }

  const btnAnt = document.getElementById("btnAnteriorTarjeta");
  const btnSig = document.getElementById("btnSiguienteTarjeta");
  if (btnAnt) btnAnt.style.display = indiceTarjeta === 0 ? "none" : "block";
  if (btnSig)
    btnSig.textContent =
      indiceTarjeta === total - 1 ? "Finalizar →" : "Siguiente tarjeta →";
  document.getElementById("teoriaIntro").textContent = tarjeta.contenido || "";

  const refEl = document.getElementById("teoriaReferencia");
  if (refEl) {
    if (tarjeta.fuente) {
      refEl.textContent = `📚 Fuente: ${tarjeta.fuente}`;
      refEl.classList.remove("oculto");
    } else {
      refEl.classList.add("oculto");
    }
  }
}

function tarjetaAnterior() {
  if (indiceTarjeta > 0) {
    indiceTarjeta--;
    renderTarjetaTeoria();
  }
}

function tarjetaSiguiente() {
  if (indiceTarjeta < tarjetasTeoria.length - 1) {
    indiceTarjeta++;
    renderTarjetaTeoria();
  } else {
    volverListaQuiz();
  }
}

async function iniciarQuiz(cuestionarioId, titulo) {
  const res = await fetch(`${API_BASE}/api/quiz/${cuestionarioId}`);
  preguntasActivas = await res.json();
  indicePregunta = 0;
  puntajeAcumulado = 0;

  document.getElementById("listaQuizzesWrap").classList.add("oculto");
  document.getElementById("vistaTeoria").classList.add("oculto");
  document.getElementById("vistaQuiz").classList.remove("oculto");

  document.getElementById("quizTitulo").textContent = titulo;
  document.getElementById("resultadoFinal").classList.add("oculto");
  numCorrectas = 0;
  numIncorrectas = 0;

  mostrarPregunta();
}

function mostrarPregunta() {
  if (indicePregunta >= preguntasActivas.length) {
    mostrarResultadoFinal();
    return;
  }

  const pregunta = preguntasActivas[indicePregunta];
  let opciones = [];

  try {
    const opcionesLimpias = pregunta.opciones
      .replace(/\\\"/g, '"')
      .replace(/^"/, "")
      .replace(/"$/, "");
    opciones = JSON.parse(opcionesLimpias);
  } catch (e) {
    console.error("Error parseando opciones:", pregunta.opciones, e);
    opciones = [];
  }

  document.getElementById("preguntaTexto").textContent = pregunta.texto;
  document.getElementById("preguntaContainer").innerHTML =
    opciones
      .map(
        (op, i) => `
    <button class="opcion-btn" onclick="seleccionarOpcion(this)" data-valor="${op.replace(/"/g, "&quot;")}">
      <span class="opcion-letra">${["A", "B", "C", "D", "E"][i] || i + 1}</span>
      <span>${op}</span>
    </button>
  `,
      )
      .join("") +
    `
    <button id="btnConfirmar" class="btn-confirmar" disabled onclick="confirmarRespuesta(${pregunta.id})">
      Confirmar respuesta
    </button>
  `;

  document.getElementById("feedbackRespuesta").classList.add("oculto");
  document.getElementById("btnSiguiente").style.display = "none";
  document.getElementById("btnVerExplicacion").style.display = "none";

  // Barra de progreso segmentada
  const barra = document.getElementById("quizProgressBar");
  if (barra) {
    barra.innerHTML = preguntasActivas
      .map((_, i) => {
        const cls =
          i < indicePregunta ? "done" : i === indicePregunta ? "active" : "";
        return `<div class="quiz-progress-seg ${cls}"></div>`;
      })
      .join("");
  }

  // Aside
  const total = preguntasActivas.length;
  const restantes = total - indicePregunta - 1;
  const set = (id, v) => {
    const el = document.getElementById(id);
    if (el) el.textContent = v;
  };
  set("quizSubtitulo", `Pregunta ${indicePregunta + 1} de ${total}`);
  set("badgeQuizNum", `${indicePregunta + 1} / ${total}`);
  set("numCorrectas", numCorrectas || 0);
  set("numIncorrectas", numIncorrectas || 0);
  set(
    "asideRestantes",
    restantes > 0 ? `${restantes} preguntas restantes` : "Última pregunta",
  );
}

let opcionSeleccionada = null;
let numCorrectas = 0;
let numIncorrectas = 0;

function seleccionarOpcion(botonEl) {
  document.querySelectorAll(".opcion-btn").forEach((b) => {
    b.classList.remove("opcion-seleccionada");
    b.style.transition = "all 0.15s ease";
  });

  botonEl.classList.add("opcion-seleccionada");
  opcionSeleccionada = botonEl;

  const btnConfirmar = document.getElementById("btnConfirmar");
  if (btnConfirmar) {
    btnConfirmar.disabled = false;
    btnConfirmar.textContent = "Confirmar respuesta";
  }
}

async function confirmarRespuesta(preguntaId) {
  if (!opcionSeleccionada) return;

  const respuesta = opcionSeleccionada.getAttribute("data-valor");
  document.querySelectorAll(".opcion-btn").forEach((b) => (b.disabled = true));

  const res = await fetch(API_BASE + "/api/quiz/answer", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ preguntaId: String(preguntaId), respuesta }),
  });

  const resultado = await res.json();
  const feedbackEl = document.getElementById("feedbackRespuesta");
  feedbackEl.classList.remove("oculto");

  const explicacion = resultado.explicacion || resultado.respuestaCorrecta || "";

  if (resultado.correcto) {
    numCorrectas++;
    puntajeAcumulado += resultado.puntaje || 50;
    opcionSeleccionada.classList.add("opcion-correcta");
    opcionSeleccionada.innerHTML += `<span class="opcion-icono-check">✓</span>`;
    feedbackEl.innerHTML = `
      <div class="feedback-header correcto">✅ <strong>¡Correcto!</strong></div>
      <div class="feedback-explicacion oculto" id="explicacionTexto">
        <p>${explicacion}</p>
      </div>`;
  } else {
    numIncorrectas++;
    opcionSeleccionada.classList.add("opcion-incorrecta");
    opcionSeleccionada.innerHTML += `<span class="opcion-icono-check">✗</span>`;
    document.querySelectorAll(".opcion-btn").forEach((b) => {
      if (b.getAttribute("data-valor") === resultado.respuestaCorrecta)
        b.classList.add("opcion-correcta");
    });
    feedbackEl.innerHTML = `
      <div class="feedback-header incorrecto">❌ <strong>Incorrecto</strong></div>
      <div class="feedback-respuesta-correcta">Respuesta correcta: <strong>${resultado.respuestaCorrecta}</strong></div>
      <div class="feedback-explicacion oculto" id="explicacionTexto">
        <p>${explicacion}</p>
      </div>`;
  }

  document.getElementById("btnVerExplicacion").style.display = "block";
  document.getElementById("btnSiguiente").style.display = "block";

  const set = (id, v) => {
    const el = document.getElementById(id);
    if (el) el.textContent = v;
  };
  set("numCorrectas", numCorrectas);
  set("numIncorrectas", numIncorrectas);

  opcionSeleccionada = null;
}

function verExplicacion() {
  const exp = document.getElementById("explicacionTexto");
  const btn = document.getElementById("btnVerExplicacion");
  if (!exp) return;
  const oculto = exp.classList.toggle("oculto");
  btn.textContent = oculto ? "📖 Ver explicación" : "📖 Ocultar explicación";
  if (!oculto) exp.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function siguientePregunta() {
  indicePregunta++;
  mostrarPregunta();
}

// ── SCORM 1.2 Export ──────────────────────────────────────────────────────────

/** Descarga toda la aplicación TaskFlow empaquetada como SCORM 1.2 */
function descargarScormApp() {
  toast("Generando paquete SCORM de la aplicación…", "info", 4000);
  fetch(API_BASE + "/api/scorm/app")
    .then(function(res) {
      if (!res.ok) throw new Error("Error " + res.status);
      return res.blob();
    })
    .then(function(blob) {
      var a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "taskflow_scorm_app.zip";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
      toast("✅ Paquete SCORM descargado. Súbelo a tu LMS desde Administrar cursos → Importar.", "success", 7000);
    })
    .catch(function(err) {
      toast("No se pudo generar el paquete SCORM: " + err.message, "error");
    });
}

/** Descarga un cuestionario individual como SCORM 1.2 */
function descargarScorm(cuestionarioId, titulo) {
  toast("Generando paquete SCORM, espera un momento...", "info", 3000);
  var url = API_BASE + "/api/scorm/quiz/" + cuestionarioId;
  fetch(url)
    .then(function(res) {
      if (!res.ok) throw new Error("Error al generar el paquete SCORM");
      return res.blob();
    })
    .then(function(blob) {
      var a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "taskflow_scorm_" + cuestionarioId + ".zip";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
      toast("Paquete SCORM descargado. Súbelo a tu LMS (Moodle, Canvas, etc.)", "success", 5000);
    })
    .catch(function(err) {
      toast("No se pudo generar el paquete SCORM: " + err.message, "error");
    });
}

function volverListaQuiz() {
  document.getElementById("vistaQuiz").classList.add("oculto");
  document.getElementById("vistaTeoria").classList.add("oculto");
  document.getElementById("listaQuizzesWrap").classList.remove("oculto");
  if (nivelActivo) seleccionarNivel(nivelActivo);
  else cargarQuizzes();
}

let _chartInstance = null;
const CHARTJS_CDN =
  "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js";

function _loadChartJs(callback) {
  if (typeof Chart !== "undefined") {
    callback();
    return;
  }
  const s = document.createElement("script");
  s.src = CHARTJS_CDN;
  s.onload = callback;
  s.onerror = () => console.warn("Chart.js no pudo cargarse desde CDN");
  document.head.appendChild(s);
}

function dibujarGrafica(stats) {
  _loadChartJs(() => {
    const canvas = document.getElementById("graficaEstados");
    if (!canvas) return;

    if (_chartInstance) {
      _chartInstance.destroy();
      _chartInstance = null;
    }

    const cs = getComputedStyle(document.documentElement);
    const textMuted = cs.getPropertyValue("--text-muted").trim() || "#7aaacf";
    const borderColor = cs.getPropertyValue("--border2").trim() || "#c8dff2";

    _chartInstance = new Chart(canvas, {
      type: "bar",
      data: {
        labels: ["Pendientes", "En proceso", "Completadas"],
        datasets: [
          {
            data: [
              stats.pendientes || 0,
              stats.enProceso || 0,
              stats.completadas || 0,
            ],
            backgroundColor: ["#ef9a9a", "#ffe082", "#a5d6a7"],
            borderColor: ["#e57373", "#f9a825", "#66bb6a"],
            borderWidth: 1.5,
            borderRadius: 6,
            borderSkipped: false,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: "rgba(0,0,0,0.75)",
            padding: 10,
            titleFont: { size: 12 },
            bodyFont: { size: 13, weight: "bold" },
          },
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: textMuted, font: { size: 11 } },
            border: { color: borderColor },
          },
          y: {
            beginAtZero: true,
            ticks: {
              color: textMuted,
              font: { size: 10 },
              stepSize: 1,
              precision: 0,
            },
            grid: { color: borderColor },
            border: { color: borderColor },
          },
        },
        animation: { duration: 600, easing: "easeOutQuart" },
      },
    });
  });
}

async function cargarInicio() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");

  document.getElementById("bienvenidaTitulo").textContent =
    `Bienvenido, ${usuario.nombre} 👋`;

  const [resStats, resProgreso] = await Promise.all([
    fetch(`${API_BASE}/api/user/stats?usuarioId=${usuario.id}`),
    fetch(`${API_BASE}/api/user/progress?usuarioId=${usuario.id}`),
  ]);

  const stats = await resStats.json();
  const progreso = await resProgreso.json();

  const total = stats.total || 0;
  const completadas = stats.completadas || 0;
  const pct = total > 0 ? Math.round((completadas / total) * 100) : 0;
  const quizzes = progreso.quizzesCompletados || 0;
  const nivel = progreso.nivel === "avanzado" ? "Avanzado" : "Básico";

  const set = (id, v) => {
    const el = document.getElementById(id);
    if (el) el.textContent = v;
  };

  set("inicioTotalTareas", total);
  set("inicioCompletadas", completadas);
  set("inicioQuizzes", quizzes);
  set("inicioNivel", nivel);
  set("inicioBarra", "");
  set("inicioBarraLabel", `${pct}% completado`);
  set("inicioNivelCirculo", progreso.nivel === "avanzado" ? 2 : 1);
  set("inicioNivelDesc", nivel);
  set("inicioQuizzesAside", quizzes);

  const barra = document.getElementById("inicioBarra");
  if (barra) barra.style.width = pct + "%";

  // Últimas 5 tareas
  const resTareas = await fetch(`${API_BASE}/tareas?usuarioId=${usuario.id}`);
  const tareas = await resTareas.json();
  const recientes = tareas.slice(-5).reverse();

  const contenedor = document.getElementById("inicioTareasRecientes");
  if (contenedor) {
    if (recientes.length === 0) {
      contenedor.innerHTML =
        '<p style="color:#7aaacf;font-size:0.88rem;">No tienes tareas aún. ¡Crea tu primera tarea!</p>';
    } else {
      contenedor.innerHTML = recientes
        .map(
          (t) => `
                <div class="tarea-gestion-item">
                    <span class="tarea-estado-badge">${
                      t.estado === "completada"
                        ? "✅"
                        : t.estado === "en_proceso"
                          ? "⚙️"
                          : "📋"
                    } ${t.estado}</span>
                    <span class="tarea-gestion-nombre">${t.titulo}</span>
                </div>
            `,
        )
        .join("");
    }
  }

  // Actualizar sessionStorage con nivel fresco
  sessionStorage.setItem(
    "usuario",
    JSON.stringify({
      ...usuario,
      nivel: progreso.nivel,
      quizzesCompletados: quizzes,
    }),
  );
}

(function init() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  if (!usuario) {
    window.location.href = "login.html";
    return;
  }

  initTema();
  initModoDaltonico();

  document.getElementById("saludo").textContent = `${usuario.nombre}`;

  const puntosEl = document.getElementById("usuarioPuntos");
  if (puntosEl) puntosEl.textContent = (usuario.puntos || 0) + " pts";

  const formulario = document.getElementById("formulario");
  const btnCrear = document.getElementById("btnCrear");
  if (btnCrear)
    btnCrear.addEventListener("click", () =>
      formulario?.classList.remove("oculto"),
    );

  const checkbox = document.getElementById("prioridad");
  if (checkbox) checkbox.addEventListener("change", mostrarPrioridad);

  const hoy = new Date().toISOString().split("T")[0];
  document.getElementById("fechaLimite")?.setAttribute("min", hoy);

  mostrarPrioridad();

  document.querySelectorAll(".columna").forEach((col) => {
    col.addEventListener("dragleave", () =>
      col.classList.remove("drop-activo"),
    );
  });

  mostrarSeccion("inicio");
})();

let conexionActiva = true;

setInterval(async () => {
  try {
    const secTablero = document.getElementById("seccionTablero");
    if (secTablero && !secTablero.classList.contains("oculto"))
      await cargarTareas();
    if (!conexionActiva) {
      conexionActiva = true;
      document.getElementById("indicadorConexion")?.remove();
    }
  } catch (e) {
    if (conexionActiva) {
      conexionActiva = false;
      const ind = document.createElement("div");
      ind.id = "indicadorConexion";
      ind.style.cssText =
        "position:fixed;bottom:16px;right:16px;background:#e53935;color:white;padding:10px 16px;border-radius:8px;font-size:13px;z-index:999;";
      ind.textContent = "⚠️ Sin conexión — reintentando...";
      document.body.appendChild(ind);
    }
  }
}, 10000);
// ============================================================
// VARIABLES GLOBALES — SPRINT 2
// ============================================================
let simulacionActual = null;

// ============================================================
// HU14 — MODO OSCURO
// ============================================================
function initTema() {
  const tema = localStorage.getItem("taskflow-tema") || "claro";
  aplicarTema(tema);
}

function aplicarTema(tema) {
  document.documentElement.setAttribute("data-tema", tema);
  localStorage.setItem("taskflow-tema", tema);
  const btn = document.getElementById("btnTema");
  if (btn)
    btn.textContent = tema === "oscuro" ? "☀️ Modo claro" : "🌙 Modo oscuro";
}

function toggleTema() {
  const actual = localStorage.getItem("taskflow-tema") || "claro";
  aplicarTema(actual === "oscuro" ? "claro" : "oscuro");
}

// ============================================================
// MODOS DALTÓNICOS
// ============================================================
function initModoDaltonico() {
  const modo = localStorage.getItem("taskflow-daltonico") || "normal";
  document.documentElement.setAttribute("data-daltonico", modo);
  const sel = document.getElementById("selectorDaltonico");
  if (sel) sel.value = modo;
}

function cambiarModoDaltonico(modo) {
  document.documentElement.setAttribute("data-daltonico", modo);
  localStorage.setItem("taskflow-daltonico", modo);
}

// ============================================================
// HU12 — NOTIFICACIONES
// ============================================================
function mostrarNotificacion(mensaje, tipo = "info", duracion = 3500) {
  const colores = {
    info: { bg: "#eef4fb", border: "#bdd8ef", icon: "ℹ️" },
    exito: { bg: "#f1f8f1", border: "#4caf50", icon: "✅" },
    error: { bg: "#fff5f5", border: "#e53935", icon: "❌" },
    logro: { bg: "#fffde7", border: "#f9a825", icon: "🏆" },
    nivel: { bg: "#f3e5f5", border: "#8e24aa", icon: "🎓" },
  };
  const c = colores[tipo] || colores.info;
  const notif = document.createElement("div");
  notif.style.cssText = `
        position:fixed; top:20px; right:20px; z-index:9999;
        background:${c.bg}; border:1.5px solid ${c.border};
        border-radius:10px; padding:14px 18px;
        display:flex; align-items:center; gap:10px;
        font-size:0.88rem; box-shadow:0 4px 20px rgba(0,0,0,0.15);
        max-width:320px; animation: slideIn 0.3s ease;
    `;
  notif.innerHTML = `<span style="font-size:1.2rem">${c.icon}</span><span>${mensaje}</span>`;
  document.body.appendChild(notif);
  setTimeout(() => {
    notif.style.opacity = "0";
    notif.style.transition = "opacity 0.3s";
    setTimeout(() => notif.remove(), 300);
  }, duracion);
}

// ============================================================
// HU15 — GAMIFICACIÓN
// ============================================================
async function notificarActividad(tipo) {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  if (!usuario) return;
  try {
    const res = await fetch(API_BASE + "/api/gamificacion/actividad", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ usuarioId: usuario.id, tipo }),
    });
    const data = await res.json();
    if (data.puntosGanados > 0)
      mostrarNotificacion(`+${data.puntosGanados} puntos`, "exito", 2500);
    if (data.nuevosLogros && data.nuevosLogros.length > 0) {
      data.nuevosLogros.forEach((logro, i) => {
        setTimeout(
          () =>
            mostrarNotificacion(
              `${logro.icono} ¡Logro! <strong>${logro.nombre}</strong> +${logro.puntos} pts`,
              "logro",
              5000,
            ),
          i * 800,
        );
      });
    }
    const usuarioAct = { ...usuario, puntos: data.puntosTotal };
    sessionStorage.setItem("usuario", JSON.stringify(usuarioAct));
    const puntosEl = document.getElementById("usuarioPuntos");
    if (puntosEl) puntosEl.textContent = data.puntosTotal + " pts";
  } catch (e) {
    console.error("Error gamificación:", e);
  }
}

// ============================================================
// HU11 — ANIMACIÓN CONFETTI
// ============================================================
function lanzarConfetti() {
  const colores = ["#3a86c8", "#60b8e0", "#daeaf7", "#1a4f7a", "#7aaacf"];
  for (let i = 0; i < 60; i++) {
    const conf = document.createElement("div");
    conf.style.cssText = `
            position:fixed; top:-10px;
            left:${Math.random() * 100}vw;
            width:8px; height:8px;
            background:${colores[Math.floor(Math.random() * colores.length)]};
            border-radius:${Math.random() > 0.5 ? "50%" : "0"};
            z-index:9998; pointer-events:none;
            animation: caerConfetti ${1.5 + Math.random() * 2}s ease forwards;
        `;
    document.body.appendChild(conf);
    setTimeout(() => conf.remove(), 3500);
  }
}

// ============================================================
// HU13 — SIMULACIÓN DE SPRINT
// ============================================================
async function cargarSimulacion() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  const res = await fetch(`${API_BASE}/api/simulacion/activa/${usuario.id}`);
  const data = await res.json();
  const contenedor = document.getElementById("contenidoSimulacion");
  if (!contenedor) return;

  if (!data.activa) {
    mostrarFormInicioSimulacion(contenedor);
  } else {
    simulacionActual = data;
    mostrarTableroSimulacion(data);
  }
}

function mostrarFormInicioSimulacion(contenedor) {
  contenedor.innerHTML = `
        <div class="dash-card" style="max-width:560px;">
            <p class="dash-card-title">🚀 Nueva Simulación de Sprint</p>
            <p style="font-size:0.88rem;color:var(--text-secondary);margin-bottom:20px;">
                Practica cómo organizar un Sprint real de Scrum. Selecciona tareas de pendientes,
                simula la ejecución y obtén retroalimentación.
            </p>
            <div class="form-field">
                <label>Meta del Sprint</label>
                <input id="simMetaSprint" placeholder="Ej: Entregar el módulo de autenticación"
                    value="Completar las funcionalidades principales del Sprint">
            </div>
            <div class="form-field">
                <label>Velocidad del equipo (Puntos de historia por Sprint)</label>
                <input id="simVelocidad" type="number" min="5" max="50" value="20">
            </div>
            <button onclick="iniciarSimulacion()" style="margin-top:8px;">🚀 Iniciar simulación</button>
        </div>
    `;
}

async function iniciarSimulacion() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  const metaSprint =
    document.getElementById("simMetaSprint")?.value || "Completar el Sprint";
  const velocidad = parseInt(
    document.getElementById("simVelocidad")?.value || "20",
  );

  const res = await fetch(API_BASE + "/api/simulacion/iniciar", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ usuarioId: usuario.id, metaSprint, velocidad }),
  });
  const data = await res.json();
  simulacionActual = { activa: true, ...data };
  mostrarTableroSimulacion(simulacionActual);
  mostrarNotificacion(
    "¡Simulación iniciada! Selecciona tareas para el Sprint",
    "info",
  );
}

function mostrarTableroSimulacion(data) {
  const sim = data.simulacion;
  const items = data.items || [];
  const contenedor = document.getElementById("contenidoSimulacion");
  if (!contenedor) return;

  const backlog = items.filter((i) => !i.enSprint);
  const enSprint = items.filter((i) => i.enSprint);
  const completados = enSprint.filter((i) => i.completado);
  const ptsSprint = enSprint.reduce((a, i) => a + (i.storyPoints || 0), 0);
  const ptsCompletados = completados.reduce(
    (a, i) => a + (i.storyPoints || 0),
    0,
  );
  const velocidad = sim.velocidadEquipo || 20;
  const pctCapacidad = Math.min(Math.round((ptsSprint / velocidad) * 100), 100);
  const excede = ptsSprint > velocidad;
  const pctAvance =
    ptsSprint > 0 ? Math.round((ptsCompletados / ptsSprint) * 100) : 0;
  const capColor = excede
    ? "#e53935"
    : pctCapacidad > 80
      ? "#f9a825"
      : "#4caf50";

  contenedor.innerHTML = `
        <!-- Header con goal y capacidad -->
        <div class="sim-header-card">
            <div style="flex:1;min-width:200px;">
                <p style="font-size:0.72rem;text-transform:uppercase;letter-spacing:0.05em;color:var(--text-muted);margin-bottom:4px;">Meta del Sprint</p>
                <p class="sim-goal-text">${sim.metaSprint}</p>
            </div>
            <div class="sim-capacity-wrap">
                <div class="sim-capacity-label">
                    <span>Capacidad usada</span>
                    <strong style="color:${capColor}">${ptsSprint} / ${velocidad} SP ${excede ? "⚠️" : ""}</strong>
                </div>
                <div class="sim-capacity-bar-track">
                    <div class="sim-capacity-fill" style="width:${pctCapacidad}%;background:${capColor}"></div>
                </div>
            </div>
            ${
              enSprint.length > 0
                ? `
            <div class="sim-capacity-wrap">
                <div class="sim-capacity-label">
                    <span>Avance del Sprint</span>
                    <strong style="color:#4caf50">${ptsCompletados} SP completados (${pctAvance}%)</strong>
                </div>
                <div class="sim-capacity-bar-track">
                    <div class="sim-capacity-fill" style="width:${pctAvance}%;background:#4caf50"></div>
                </div>
            </div>`
                : ""
            }
        </div>

        <!-- Guía de pasos -->
        <div class="sim-step-guide">
            ${
              backlog.length > 0 && enSprint.length === 0
                ? "<strong>Paso 1:</strong> Selecciona tareas del Lista de pendientes para agregar al Sprint. No excedas la velocidad del equipo."
                : enSprint.length > 0 && completados.length === 0
                  ? "<strong>Paso 2:</strong> Marca las tareas como completadas (✓) a medida que el equipo las termina. Cuando estés listo, cierra el Sprint."
                  : completados.length > 0
                    ? `<strong>Paso 3:</strong> ${completados.length} de ${enSprint.length} tareas completadas. Puedes cerrar el Sprint para ver la retrospectiva.`
                    : "<strong>¡Listo!</strong> Todos los items de pendientes están en el Sprint."
            }
        </div>

        <!-- Columnas -->
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;">
            <!-- Lista de pendientes -->
            <div class="dash-card" style="padding:18px;">
                <div class="sim-col-header">
                    <p class="dash-card-title" style="margin:0;">📋 Lista de pendientes</p>
                    <span class="sim-col-badge">${backlog.length} items · ${backlog.reduce((a, i) => a + i.storyPoints, 0)} SP</span>
                </div>
                ${
                  backlog.length === 0
                    ? `<div class="sim-empty"><span class="sim-empty-icon">🎯</span>Todos los items están en el Sprint</div>`
                    : backlog
                        .map(
                          (item) => `
                        <div class="sim-item-v2">
                            <span class="sim-sp-badge">${item.storyPoints} SP</span>
                            <span class="sim-titulo" style="flex:1;font-size:0.85rem;color:var(--text-primary);">${item.titulo}</span>
                            <div class="sim-actions">
                                <button class="sim-btn-v2" onclick="moverASprint(${item.id})">→ Al Sprint</button>
                            </div>
                        </div>
                    `,
                        )
                        .join("")
                }
            </div>

            <!-- Tareas del Sprint -->
            <div class="dash-card" style="padding:18px;">
                <div class="sim-col-header">
                    <p class="dash-card-title" style="margin:0;">⚡ Tareas del Sprint</p>
                    <span class="sim-col-badge">${completados.length}/${enSprint.length} hechas</span>
                </div>
                ${
                  enSprint.length === 0
                    ? `<div class="sim-empty"><span class="sim-empty-icon">📥</span>Arrastra tareas desde pendientes</div>`
                    : enSprint
                        .map(
                          (item) => `
                        <div class="sim-item-v2 ${item.completado ? "sim-done" : ""}">
                            <span class="sim-sp-badge" style="${item.completado ? "background:#f1f8f1;border-color:#4caf50;color:#2e7d32;" : ""}">${item.storyPoints} SP</span>
                            <span class="sim-titulo" style="flex:1;font-size:0.85rem;">${item.titulo}</span>
                            <div class="sim-actions">
                                ${
                                  !item.completado
                                    ? `<button class="sim-btn-v2 ok" onclick="completarItemSim(${item.id})" title="Marcar como completado">✓ Hecho</button>`
                                    : `<span style="font-size:0.78rem;color:#4caf50;font-weight:700;">✅ Completado</span>`
                                }
                                <button class="sim-btn-v2 danger" onclick="volverPendientes(${item.id})" title="Devolver a pendientes">←</button>
                            </div>
                        </div>
                    `,
                        )
                        .join("")
                }

                <!-- Botón cerrar sprint -->
                <div style="margin-top:16px;padding-top:14px;border-top:1px solid var(--border);">
                    <button onclick="cerrarSprintSim(${sim.id})"
                        style="width:100%;background:${enSprint.length > 0 ? "#e53935" : "var(--border)"};${enSprint.length > 0 ? "" : "cursor:not-allowed;color:var(--text-muted);"}"
                        ${enSprint.length === 0 ? "disabled" : ""}>
                        🏁 Cerrar Sprint y ver resultados
                    </button>
                </div>
            </div>
        </div>
    `;
}

async function moverASprint(itemId) {
  await fetch(`${API_BASE}/api/simulacion/item/${itemId}/toggle`, {
    method: "PATCH",
  });
  await cargarSimulacion();
}

async function volverPendientes(itemId) {
  await fetch(`${API_BASE}/api/simulacion/item/${itemId}/toggle`, {
    method: "PATCH",
  });
  await cargarSimulacion();
}

async function completarItemSim(itemId) {
  await fetch(`${API_BASE}/api/simulacion/item/${itemId}/completar`, {
    method: "PATCH",
  });
  mostrarNotificacion("Item completado ✓", "exito", 2000);
  await cargarSimulacion();
}

async function cerrarSprintSim(simulacionId) {
  if (!await confirmDialog("Se calcularán los puntos entregados y se cerrará el Sprint.", "¿Cerrar Sprint?", "🏁", "Cerrar Sprint", "ok-blue")) return;
  try {
    const res = await fetch(
      `${API_BASE}/api/simulacion/cerrar/${simulacionId}`,
      { method: "POST" },
    );
    const data = await res.json();
    const contenedor = document.getElementById("contenidoSimulacion");
    const pctEntregado =
      data.puntosPlaneados > 0
        ? Math.round((data.puntosEntregados / data.puntosPlaneados) * 100)
        : 0;
    const exitColor = data.exitoso ? "#4caf50" : "#e53935";
    const failColor = data.exitoso ? "#f1f8f1" : "#fff5f5";

    let html = '<div style="max-width:680px;">';

    // Header
    html +=
      '<div class="sim-header-card" style="background:' +
      failColor +
      ";border-color:" +
      exitColor +
      ';">';
    html +=
      '<div><p style="font-size:1.3rem;font-weight:700;margin-bottom:4px;">' +
      (data.exitoso ? "🎉 ¡Sprint exitoso!" : "⚠️ Sprint incompleto") +
      "</p>";
    html +=
      '<p style="font-size:0.85rem;color:var(--text-secondary);">' +
      (data.exitoso
        ? "El equipo cumplió el Meta del Sprint con al menos el 80% de los puntos planeados."
        : "El equipo no alcanzó el 80% de los puntos comprometidos.") +
      "</p></div></div>";

    // KPIs
    html += '<div class="resultado-kpi-grid">';
    html +=
      '<div class="resultado-kpi"><span class="resultado-kpi-num">' +
      data.puntosPlaneados +
      '</span><span class="resultado-kpi-lbl">SP planeados</span></div>';
    html +=
      '<div class="resultado-kpi"><span class="resultado-kpi-num" style="color:' +
      exitColor +
      '">' +
      data.puntosEntregados +
      '</span><span class="resultado-kpi-lbl">SP entregados</span></div>';
    html +=
      '<div class="resultado-kpi"><span class="resultado-kpi-num" style="color:' +
      (data.exitoso ? "#4caf50" : "#f9a825") +
      '">' +
      pctEntregado +
      '%</span><span class="resultado-kpi-lbl">% completado</span></div>';
    html +=
      '<div class="resultado-kpi"><span class="resultado-kpi-num">' +
      data.completados +
      '</span><span class="resultado-kpi-lbl">Tareas hechas</span></div>';
    html +=
      '<div class="resultado-kpi"><span class="resultado-kpi-num">' +
      (data.total - data.completados) +
      '</span><span class="resultado-kpi-lbl">Sin terminar</span></div>';
    html +=
      '<div class="resultado-kpi"><span class="resultado-kpi-num">' +
      data.velocidadReal +
      '</span><span class="resultado-kpi-lbl">Velocidad real</span></div>';
    html += "</div>";

    // Barra entrega vs compromiso
    const barPct = Math.min(pctEntregado, 100);
    const barColor = data.exitoso ? "#4caf50" : "#f9a825";
    html += '<div class="chart-card" style="margin-bottom:16px;">';
    html += '<p class="chart-card-title">Entrega vs compromiso</p>';
    html +=
      '<div style="display:flex;justify-content:space-between;font-size:0.8rem;color:var(--text-secondary);margin-bottom:6px;">';
    html +=
      "<span>Puntos entregados</span><span>" +
      data.puntosEntregados +
      " / " +
      data.puntosPlaneados +
      " SP</span></div>";
    html +=
      '<div class="sim-capacity-bar-track" style="height:14px;"><div class="sim-capacity-fill" style="width:' +
      barPct +
      "%;background:" +
      barColor +
      ';"></div></div>';
    html +=
      '<p style="font-size:0.75rem;color:var(--text-muted);margin-top:8px;">Umbral de éxito: 80% = ' +
      Math.round(data.puntosPlaneados * 0.8) +
      " SP</p>";
    html += "</div>";

    // Retrospectiva
    html +=
      '<div class="retro-card ' +
      (data.exitoso ? "retro-ok" : "retro-fail") +
      '">';
    html += '<p class="retro-title">📝 Retrospectiva del Sprint</p>';
    html += '<p class="retro-text">' + data.retrospectiva + "</p></div>";

    // Leccion
    html += '<div class="dash-card" style="margin-bottom:16px;">';
    html += '<p class="dash-card-title">🎓 Lección aprendida</p>';
    if (data.exitoso) {
      html +=
        '<p style="font-size:0.85rem;color:var(--text-secondary);line-height:1.7;">Tu <strong>velocidad real</strong> fue <strong>' +
        data.velocidadReal +
        " SP</strong>. Usa este número como base para el próximo Sprint. La consistencia es más valiosa que sprints heróicos ocasionales.</p>";
    } else {
      html +=
        '<p style="font-size:0.85rem;color:var(--text-secondary);line-height:1.7;">El equipo comprometió <strong>' +
        data.puntosPlaneados +
        " SP</strong> pero solo entregó <strong>" +
        data.puntosEntregados +
        " SP</strong>. Reduce el alcance a <strong>" +
        Math.max(data.puntosEntregados, 10) +
        " SP</strong> en el próximo Sprint.</p>";
    }
    html += "</div>";

    // Acciones
    html += '<div style="display:flex;gap:12px;flex-wrap:wrap;">';
    html +=
      "<button onclick=\"mostrarSeccion('simulacion')\">Nueva simulaci\u00f3n</button>";
    html +=
      '<button class="btn-cancel" onclick="mostrarSeccion(\'tablero\')">Ir al tablero</button>';
    html +=
      '<button class="btn-cancel" onclick="mostrarSeccion(\'progreso\')">Ver mi progreso</button>';
    html += "</div></div>";

    contenedor.innerHTML = html;

    if (data.exitoso) {
      mostrarNotificacion(
        "\uD83C\uDF89 \u00A1Sprint exitoso! Velocidad real: " +
          data.velocidadReal +
          " SP",
        "exito",
        5000,
      );
    }
  } catch (e) {
    mostrarNotificacion("Error al cerrar el Sprint", "error", 3000);
  }
}

// ============================================================
// AJUSTES
// ============================================================
function cargarAjustes() {
  const usuario = JSON.parse(sessionStorage.getItem("usuario") || "null");
  if (usuario) {
    const nombreEl = document.getElementById("ajusteNombreActual");
    const emailEl = document.getElementById("ajusteEmailActual");
    if (nombreEl) nombreEl.textContent = usuario.nombre || "—";
    if (emailEl) emailEl.textContent = usuario.email || "—";
  }
  actualizarBotonesAjustes();
}

function actualizarBotonesAjustes() {
  const tema = localStorage.getItem("taskflow-tema") || "claro";
  const btn = document.getElementById("ajusteBtnTema");
  if (btn)
    btn.textContent = tema === "oscuro" ? "☀️ Modo claro" : "🌙 Modo oscuro";

  const modo = localStorage.getItem("taskflow-daltonico") || "normal";
  const sel = document.getElementById("ajusteSelectorDaltonico");
  if (sel) sel.value = modo;

  const notifLogros = localStorage.getItem("notifLogros");
  const notifTareas = localStorage.getItem("notifTareas");
  const sw1 = document.getElementById("switchNotifLogros");
  const sw2 = document.getElementById("switchNotifTareas");
  if (sw1) sw1.checked = notifLogros !== "false";
  if (sw2) sw2.checked = notifTareas !== "false";
}

function guardarPreferencia(clave, valor) {
  localStorage.setItem(clave, valor);
  mostrarNotificacion("Preferencia guardada", "exito", 2000);
}

// ============================================================
// AYUDA — FAQ accordion
// ============================================================
function toggleFaq(el) {
  const respuesta = el.querySelector(".faq-respuesta");
  const arrow = el.querySelector(".faq-arrow");
  const abierto = el.classList.contains("abierto");

  document.querySelectorAll(".faq-item.abierto").forEach((item) => {
    item.classList.remove("abierto");
    item.querySelector(".faq-respuesta").style.maxHeight = null;
    item.querySelector(".faq-arrow").textContent = "▾";
  });

  if (!abierto) {
    el.classList.add("abierto");
    respuesta.style.maxHeight = respuesta.scrollHeight + "px";
    arrow.textContent = "▴";
  }
}

// ============================================================
// SCORM 1.2 — Exportación
// ============================================================

function descargarScormApp() {
  toast("Generando paquete SCORM de la aplicación...", "info", 4000);
  fetch(API_BASE + "/api/scorm/app")
    .then(function (res) {
      if (!res.ok) throw new Error("Error " + res.status);
      return res.blob();
    })
    .then(function (blob) {
      var a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "taskflow_scorm_app.zip";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
      toast("Paquete SCORM descargado. Subelo a tu LMS desde Administrar cursos.", "success", 6000);
    })
    .catch(function (err) {
      toast("No se pudo generar el paquete SCORM: " + err.message, "error");
    });
}

function descargarScorm(cuestionarioId, titulo) {
  toast("Generando paquete SCORM del cuestionario...", "info", 3000);
  fetch(API_BASE + "/api/scorm/quiz/" + cuestionarioId)
    .then(function (res) {
      if (!res.ok) throw new Error("Error " + res.status);
      return res.blob();
    })
    .then(function (blob) {
      var a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "taskflow_scorm_" + cuestionarioId + ".zip";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
      toast("Paquete SCORM descargado. Subelo a tu LMS (Moodle, Canvas, etc.)", "success", 5000);
    })
    .catch(function (err) {
      toast("No se pudo generar el paquete SCORM: " + err.message, "error");
    });
}