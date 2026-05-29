package com.ingenieria.taskflow.service;

import com.ingenieria.taskflow.exception.ResourceNotFoundException;
import com.ingenieria.taskflow.model.Cuestionario;
import com.ingenieria.taskflow.model.Pregunta;
import com.ingenieria.taskflow.model.Teoria;
import com.ingenieria.taskflow.repository.CuestionarioRepository;
import com.ingenieria.taskflow.repository.PreguntaRepository;
import com.ingenieria.taskflow.repository.TeoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ScormService {

    private static final Logger log = LoggerFactory.getLogger(ScormService.class);

    @Autowired private CuestionarioRepository cuestionarioRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private TeoriaRepository teoriaRepository;
    @Autowired private ResourceLoader resourceLoader;

    /** URL pública del backend — los JS del paquete SCORM apuntan aquí */
    private static final String BACKEND_URL = "https://taskflow-1t03.onrender.com";

    // ═══════════════════════════════════════════════════════════════════════════
    // PAQUETE SCORM — APLICACIÓN COMPLETA
    // Empaqueta todo el frontend de TaskFlow en un ZIP SCORM 1.2 listo para LMS
    // ═══════════════════════════════════════════════════════════════════════════

    public byte[] generarPaqueteAplicacion() throws IOException {
        log.info("Generando paquete SCORM completo de la aplicación TaskFlow");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 1. Manifest SCORM 1.2
            addText(zos, "imsmanifest.xml", buildAppManifest());

            // 2. Bridge SCORM (inicialización y tracking)
            addText(zos, "scorm_init.js", buildScormInitJs());

            // 3. Archivos estáticos — CSS
            addStatic(zos, "style.css");
            addStatic(zos, "style.min.css");
            addStatic(zos, "style_additions.css");
            addStatic(zos, "style_additions.min.css");
            addStatic(zos, "login.css");
            addStatic(zos, "login.min.css");

            // 4. JS — reemplazar API_BASE y parchear navegación para SCORM
            String scriptJs  = injectApiBase(readStaticText("script.js"));
            // patchLoginNavigation cambia window.location.href por window.location.replace
            // para navegar a index.html sin cerrar la sesión SCORM (beforeunload solo hace commit).
            String loginJs   = patchLoginNavigation(injectApiBase(readStaticText("login.js")));
            addText(zos, "script.js",      scriptJs);
            addText(zos, "script.min.js",  scriptJs);
            addText(zos, "login.js",       loginJs);
            addText(zos, "login.min.js",   loginJs);

            // 5. HTML — quitar UserWay (rompe en el sandbox de SCORM Cloud)
            // scorm_init.js SOLO va en index.html: si se inyecta también en login.html,
            // LMSInitialize se llama dos veces (login + index). La segunda llamada
            // devuelve error 101 (Already Initialized) y en SCORM Cloud deja LMSSetValue
            // inoperante, por lo que score y session_time nunca se reportan.
            String loginHtml = removeUserWayWidget(readStaticText("login.html"));
            String indexHtml = removeUserWayWidget(readStaticText("index.html"));
            addText(zos, "login.html", loginHtml);                  // sin SCORM bridge
            addText(zos, "index.html", injectScormScript(indexHtml)); // LMSInitialize aquí, una sola vez
        }

        log.info("Paquete SCORM completo generado: {} bytes", baos.size());
        return baos.toByteArray();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PAQUETE SCORM — CUESTIONARIO INDIVIDUAL
    // ═══════════════════════════════════════════════════════════════════════════

    public byte[] generarPaqueteCuestionario(Long cuestionarioId) throws IOException {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuestionario no encontrado: " + cuestionarioId));

        List<Pregunta> preguntas = preguntaRepository.findByCuestionarioId(cuestionarioId);
        List<Teoria> teorias = teoriaRepository.findByCuestionarioIdOrderByOrden(cuestionarioId);

        log.info("Generando paquete SCORM para cuestionario {} ({} preguntas)", cuestionarioId, preguntas.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            addText(zos, "imsmanifest.xml", buildQuizManifest(cuestionario));
            addText(zos, "scorm12.js",      buildScorm12Js());
            addText(zos, "style.css",       buildQuizCss());
            addText(zos, "index.html",      buildQuizHtml(cuestionario, preguntas, teorias));
        }

        log.info("Paquete SCORM de cuestionario generado: {} bytes", baos.size());
        return baos.toByteArray();
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Manifest — App completa
    // ───────────────────────────────────────────────────────────────────────────

    private String buildAppManifest() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             + "<manifest identifier=\"taskflow_app_scorm\" version=\"1.0\"\n"
             + "  xmlns=\"http://www.imsproject.org/xsd/imscp_rootv1p1p2\"\n"
             + "  xmlns:adlcp=\"http://www.adlnet.org/xsd/adlcp_rootv1p2\"\n"
             + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
             + "  xsi:schemaLocation=\"http://www.imsproject.org/xsd/imscp_rootv1p1p2 imscp_rootv1p1p2.xsd\n"
             + "                      http://www.adlnet.org/xsd/adlcp_rootv1p2 adlcp_rootv1p2.xsd\">\n"
             + "\n"
             + "  <metadata>\n"
             + "    <schema>ADL SCORM</schema>\n"
             + "    <schemaversion>1.2</schemaversion>\n"
             + "  </metadata>\n"
             + "\n"
             + "  <organizations default=\"org_taskflow\">\n"
             + "    <organization identifier=\"org_taskflow\">\n"
             + "      <title>TaskFlow — Plataforma de Aprendizaje Scrum</title>\n"
             + "      <item identifier=\"item_taskflow\" identifierref=\"res_taskflow\" isvisible=\"true\">\n"
             + "        <title>TaskFlow — Plataforma de Aprendizaje Scrum</title>\n"
             + "        <adlcp:masteryscore>60</adlcp:masteryscore>\n"
             + "      </item>\n"
             + "    </organization>\n"
             + "  </organizations>\n"
             + "\n"
             + "  <resources>\n"
             + "    <resource identifier=\"res_taskflow\" type=\"webcontent\" adlcp:scormtype=\"sco\" href=\"login.html\">\n"
             + "      <file href=\"login.html\"/>\n"
             + "      <file href=\"index.html\"/>\n"
             + "      <file href=\"scorm_init.js\"/>\n"
             + "      <file href=\"style.css\"/>\n"
             + "      <file href=\"style.min.css\"/>\n"
             + "      <file href=\"style_additions.css\"/>\n"
             + "      <file href=\"style_additions.min.css\"/>\n"
             + "      <file href=\"login.css\"/>\n"
             + "      <file href=\"login.min.css\"/>\n"
             + "      <file href=\"script.js\"/>\n"
             + "      <file href=\"script.min.js\"/>\n"
             + "      <file href=\"login.js\"/>\n"
             + "      <file href=\"login.min.js\"/>\n"
             + "    </resource>\n"
             + "  </resources>\n"
             + "\n"
             + "</manifest>\n";
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Parche de navegación para el paquete SCORM
    // Usa window.location.replace('index.html') para navegar al tablero.
    // El beforeunload de scorm_init.js detecta que estamos en login.html y solo
    // llama scormSave() (commit), NO scormFinish(), por lo que la sesión SCORM
    // permanece abierta. index.html reinicializa el bridge correctamente.
    // NOTA: document.open/write/close fue descartado porque en el sandbox de
    // SCORM Cloud los scripts externos inyectados no se ejecutan, dejando
    // mostrarSeccion() y otras funciones de script.js sin definir.
    // ───────────────────────────────────────────────────────────────────────────

    private String patchLoginNavigation(String js) {
        return js.replace(
            "window.location.href = \"index.html\";",
            "window.location.replace('index.html');"
        );
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Elimina el widget de UserWay del HTML para el paquete SCORM.
    // En SCORM Cloud (iframe), UserWay intenta redefinir la propiedad "uwautoplay"
    // con Object.defineProperty, pero ya existe en el entorno del LMS y lanza
    // TypeError: Cannot redefine property: uwautoplay, rompiendo todo el JS.
    // ───────────────────────────────────────────────────────────────────────────

    private String removeUserWayWidget(String html) {
        // Eliminar comentario + tag <script> de UserWay en una sola pasada
        return html
            .replaceAll("(?s)[ \\t]*<!--[^>]*[Uu]ser[Ww]ay[^>]*-->\\s*<script[^>]*cdn\\.userway\\.org[^>]*></script>", "")
            .replaceAll("<script[^>]*cdn\\.userway\\.org[^>]*></script>", "");
    }

    // ───────────────────────────────────────────────────────────────────────────
    // scorm_init.js — Bridge SCORM 1.2 para la app completa
    // ───────────────────────────────────────────────────────────────────────────

    private String buildScormInitJs() {
        return "/**\n"
             + " * TaskFlow — SCORM 1.2 Bridge\n"
             + " * Compatible con: Moodle, Canvas, Blackboard, ILIAS, TalentLMS, SCORM Cloud\n"
             + " */\n"
             + "(function () {\n"
             + "  'use strict';\n"
             + "\n"
             + "  var api         = null;\n"
             + "  var initialized = false;\n"
             + "  var sessionStart = null;          // para calcular session_time\n"
             + "  var quizzesAprobados        = 0;\n"
             + "  var puntajeAcumuladoScorm   = 0;\n"
             + "  var totalQuizzesReportados  = 0;\n"
             + "\n"
             + "  // Detecta si estamos en login.html para NO llamar LMSFinish al navegar\n"
             + "  var enLoginPage = window.location.href.indexOf('login.html') !== -1\n"
             + "                 || window.location.pathname === '/';\n"
             + "\n"
             + "  /** Formato SCORM 1.2: HHHH:MM:SS */\n"
             + "  function formatScormTime(ms) {\n"
             + "    var totalSec = Math.round(ms / 1000);\n"
             + "    var hh = Math.floor(totalSec / 3600);\n"
             + "    var mm = Math.floor((totalSec % 3600) / 60);\n"
             + "    var ss = totalSec % 60;\n"
             + "    return (hh < 10 ? '0' : '') + hh + ':'\n"
             + "         + (mm < 10 ? '0' : '') + mm + ':'\n"
             + "         + (ss < 10 ? '0' : '') + ss;\n"
             + "  }\n"
             + "\n"
             + "  /** Busca el objeto API SCORM 1.2 en la jerarquía de ventanas */\n"
             + "  function findAPI(win) {\n"
             + "    var tries = 0;\n"
             + "    while (!win.API && win.parent && win.parent !== win && tries < 7) {\n"
             + "      tries++;\n"
             + "      win = win.parent;\n"
             + "    }\n"
             + "    return win.API || null;\n"
             + "  }\n"
             + "\n"
             + "  function scormInit() {\n"
             + "    api = findAPI(window);\n"
             + "    if (!api) {\n"
             + "      console.info('[TaskFlow SCORM] API no encontrada. Modo standalone.');\n"
             + "      return;\n"
             + "    }\n"
             + "    var r = api.LMSInitialize('');\n"
             + "    var freshInit = (r === 'true' || r === true);\n"
             + "    initialized = true;   // siempre true si hay API (puede ser sesión retomada)\n"
             + "    sessionStart = new Date();\n"
             + "    if (freshInit) {\n"
             + "      api.LMSSetValue('cmi.core.lesson_status', 'incomplete');\n"
             + "      api.LMSSetValue('cmi.core.score.raw',     '0');\n"
             + "      api.LMSSetValue('cmi.core.score.min',     '0');\n"
             + "      api.LMSSetValue('cmi.core.score.max',     '100');\n"
             + "      api.LMSCommit('');\n"
             + "      console.log('[TaskFlow SCORM] Sesión iniciada.');\n"
             + "    } else {\n"
             + "      console.log('[TaskFlow SCORM] Reconectado a sesión existente.');\n"
             + "    }\n"
             + "  }\n"
             + "\n"
             + "  /**\n"
             + "   * Llamado desde script.js al completar un quiz.\n"
             + "   * @param {number}  porcentaje  0-100\n"
             + "   * @param {boolean} aprobado    true si >= 60%\n"
             + "   */\n"
             + "  function reportarQuiz(porcentaje, aprobado) {\n"
             + "    if (!api || !initialized) return;\n"
             + "\n"
             + "    totalQuizzesReportados++;\n"
             + "    puntajeAcumuladoScorm += porcentaje;\n"
             + "    if (aprobado) quizzesAprobados++;\n"
             + "\n"
             + "    var promedio = Math.round(puntajeAcumuladoScorm / totalQuizzesReportados);\n"
             + "    var iIdx     = totalQuizzesReportados - 1;\n"
             + "\n"
             + "    api.LMSSetValue('cmi.interactions.' + iIdx + '.id',               'quiz_' + totalQuizzesReportados);\n"
             + "    api.LMSSetValue('cmi.interactions.' + iIdx + '.result',            aprobado ? 'correct' : 'wrong');\n"
             + "    api.LMSSetValue('cmi.interactions.' + iIdx + '.student_response',  String(porcentaje) + '%');\n"
             + "    api.LMSSetValue('cmi.core.score.raw', String(promedio));\n"
             + "\n"
             + "    if (promedio >= 60) {\n"
             + "      api.LMSSetValue('cmi.core.lesson_status', 'passed');\n"
             + "    }\n"
             + "\n"
             + "    // Registrar tiempo de sesión acumulado\n"
             + "    if (sessionStart) {\n"
             + "      api.LMSSetValue('cmi.core.session_time', formatScormTime(new Date() - sessionStart));\n"
             + "    }\n"
             + "\n"
             + "    api.LMSCommit('');\n"
             + "    console.log('[TaskFlow SCORM] Quiz reportado. Promedio: ' + promedio + '%');\n"
             + "\n"
             + "    // Cerrar sesión limpiamente tras reportar aprobado\n"
             + "    if (promedio >= 60) {\n"
             + "      scormFinish();\n"
             + "    }\n"
             + "  }\n"
             + "\n"
             + "  /**\n"
             + "   * Cierra la sesión SCORM reportando el tiempo transcurrido.\n"
             + "   * Solo se llama desde index.html (no desde login.html para no\n"
             + "   * cerrar la sesión al navegar entre páginas del paquete).\n"
             + "   */\n"
             + "  function scormFinish() {\n"
             + "    if (!api || !initialized) return;\n"
             + "    if (sessionStart) {\n"
             + "      api.LMSSetValue('cmi.core.session_time', formatScormTime(new Date() - sessionStart));\n"
             + "    }\n"
             + "    api.LMSCommit('');\n"
             + "    api.LMSFinish('');\n"
             + "    initialized = false;\n"
             + "    console.log('[TaskFlow SCORM] Sesión cerrada.');\n"
             + "  }\n"
             + "\n"
             + "  /** En login.html solo hace commit (guarda estado), NO cierra la sesión */\n"
             + "  function scormSave() {\n"
             + "    if (!api || !initialized) return;\n"
             + "    if (sessionStart) {\n"
             + "      api.LMSSetValue('cmi.core.session_time', formatScormTime(new Date() - sessionStart));\n"
             + "    }\n"
             + "    api.LMSCommit('');\n"
             + "  }\n"
             + "\n"
             + "  // Al salir de login.html: solo guardar, no cerrar la sesión SCORM\n"
             + "  // Al salir de index.html: cerrar la sesión correctamente\n"
             + "  window.addEventListener('beforeunload', function () {\n"
             + "    if (enLoginPage) {\n"
             + "      scormSave();\n"
             + "    } else {\n"
             + "      scormFinish();\n"
             + "    }\n"
             + "  });\n"
             + "\n"
             + "  window.TaskFlowSCORM = {\n"
             + "    isActive:     function () { return initialized; },\n"
             + "    reportarQuiz: reportarQuiz,\n"
             + "    finish:       scormFinish\n"
             + "  };\n"
             + "\n"
             + "  if (document.readyState === 'loading') {\n"
             + "    document.addEventListener('DOMContentLoaded', scormInit);\n"
             + "  } else {\n"
             + "    scormInit();\n"
             + "  }\n"
             + "\n"
             + "})();\n";
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Reemplazar API_BASE vacío con la URL real del backend
    // ───────────────────────────────────────────────────────────────────────────

    private String injectApiBase(String js) {
        // script.js y login.js definen: const API_BASE = "";
        // En el paquete SCORM necesitamos la URL absoluta de Render
        return js.replace(
            "const API_BASE = \"\";",
            "const API_BASE = \"" + BACKEND_URL + "\";"
        );
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Inyectar <script src="scorm_init.js"></script> antes de </body>
    // ───────────────────────────────────────────────────────────────────────────

    private String injectScormScript(String html) {
        String tag = "  <script src=\"scorm_init.js\"></script>\n";
        // Insertar antes de </body>
        int idx = html.lastIndexOf("</body>");
        if (idx >= 0) {
            return html.substring(0, idx) + tag + html.substring(idx);
        }
        // Si no hay </body>, agregar al final
        return html + "\n" + tag;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Leer archivos estáticos desde classpath:static/
    // ───────────────────────────────────────────────────────────────────────────

    private String readStaticText(String filename) throws IOException {
        Resource r = resourceLoader.getResource("classpath:static/" + filename);
        if (!r.exists()) {
            log.warn("Archivo estático no encontrado: {}", filename);
            return "";
        }
        try (InputStream is = r.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private byte[] readStaticBytes(String filename) throws IOException {
        Resource r = resourceLoader.getResource("classpath:static/" + filename);
        if (!r.exists()) {
            log.warn("Archivo estático no encontrado: {}", filename);
            return new byte[0];
        }
        try (InputStream is = r.getInputStream()) {
            return is.readAllBytes();
        }
    }

    private void addStatic(ZipOutputStream zos, String filename) throws IOException {
        byte[] bytes = readStaticBytes(filename);
        if (bytes.length == 0) return;
        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(bytes.length);
        zos.putNextEntry(entry);
        zos.write(bytes);
        zos.closeEntry();
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Helpers comunes
    // ───────────────────────────────────────────────────────────────────────────

    private void addText(ZipOutputStream zos, String name, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ZipEntry entry = new ZipEntry(name);
        entry.setSize(bytes.length);
        zos.putNextEntry(entry);
        zos.write(bytes);
        zos.closeEntry();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PAQUETE CUESTIONARIO INDIVIDUAL — helpers internos
    // ═══════════════════════════════════════════════════════════════════════════

    private String buildQuizManifest(Cuestionario c) {
        String uid   = "taskflow_scorm_" + c.getId();
        String title = escXml(c.getTitulo());
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             + "<manifest identifier=\"" + uid + "\" version=\"1.0\"\n"
             + "  xmlns=\"http://www.imsproject.org/xsd/imscp_rootv1p1p2\"\n"
             + "  xmlns:adlcp=\"http://www.adlnet.org/xsd/adlcp_rootv1p2\"\n"
             + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
             + "  xsi:schemaLocation=\"http://www.imsproject.org/xsd/imscp_rootv1p1p2 imscp_rootv1p1p2.xsd\n"
             + "                      http://www.adlnet.org/xsd/adlcp_rootv1p2 adlcp_rootv1p2.xsd\">\n"
             + "  <metadata>\n"
             + "    <schema>ADL SCORM</schema>\n"
             + "    <schemaversion>1.2</schemaversion>\n"
             + "  </metadata>\n"
             + "  <organizations default=\"org_" + c.getId() + "\">\n"
             + "    <organization identifier=\"org_" + c.getId() + "\">\n"
             + "      <title>" + title + "</title>\n"
             + "      <item identifier=\"item_1\" identifierref=\"res_1\" isvisible=\"true\">\n"
             + "        <title>" + title + "</title>\n"
             + "        <adlcp:masteryscore>60</adlcp:masteryscore>\n"
             + "      </item>\n"
             + "    </organization>\n"
             + "  </organizations>\n"
             + "  <resources>\n"
             + "    <resource identifier=\"res_1\" type=\"webcontent\" adlcp:scormtype=\"sco\" href=\"index.html\">\n"
             + "      <file href=\"index.html\"/>\n"
             + "      <file href=\"scorm12.js\"/>\n"
             + "      <file href=\"style.css\"/>\n"
             + "    </resource>\n"
             + "  </resources>\n"
             + "</manifest>\n";
    }

    private String buildScorm12Js() {
        return "// SCORM 1.2 API Wrapper — TaskFlow Quiz\n"
             + "var ScormAPI={\n"
             + "  api:null,initialized:false,\n"
             + "  findAPI:function(w){var t=0;while(!w.API&&w.parent&&w.parent!==w&&t<7){t++;w=w.parent;}return w.API||null;},\n"
             + "  init:function(){this.api=this.findAPI(window);if(!this.api){console.warn('[SCORM] No API');return false;}\n"
             + "    var r=this.api.LMSInitialize('');this.initialized=(r==='true'||r===true);return this.initialized;},\n"
             + "  set:function(k,v){if(this.api)this.api.LMSSetValue(k,String(v));},\n"
             + "  commit:function(){if(this.api)this.api.LMSCommit('');},\n"
             + "  finish:function(status,raw,max){\n"
             + "    if(!this.api)return;\n"
             + "    this.set('cmi.core.lesson_status',status);\n"
             + "    if(raw!==undefined){this.set('cmi.core.score.raw',raw);this.set('cmi.core.score.min',0);this.set('cmi.core.score.max',max||100);}\n"
             + "    this.commit();this.api.LMSFinish('');}\n"
             + "};\n";
    }

    private String buildQuizCss() {
        return "*{box-sizing:border-box;margin:0;padding:0}\n"
             + "body{font-family:'Segoe UI',system-ui,sans-serif;background:#f0f2f5;color:#1a1a2e;line-height:1.6}\n"
             + ".container{max-width:820px;margin:0 auto;padding:24px 16px}\n"
             + ".header{background:linear-gradient(135deg,#6c63ff,#5a52d5);color:#fff;padding:32px;border-radius:16px;margin-bottom:24px;text-align:center}\n"
             + ".header h1{font-size:1.75rem;margin-bottom:6px}.header p{opacity:.85;font-size:.95rem}\n"
             + ".badge{display:inline-block;background:rgba(255,255,255,.2);padding:4px 14px;border-radius:20px;font-size:.8rem;margin-top:10px}\n"
             + ".tabs{display:flex;gap:8px;margin-bottom:20px}\n"
             + ".tab-btn{flex:1;padding:12px;border:none;border-radius:10px;background:#fff;color:#666;font-weight:600;cursor:pointer;transition:all .2s;font-size:.9rem}\n"
             + ".tab-btn.active{background:#6c63ff;color:#fff}.tab-btn:hover:not(.active){background:#e8e7ff;color:#6c63ff}\n"
             + ".teoria-card{background:#fff;border-radius:12px;padding:24px;margin-bottom:16px;box-shadow:0 2px 8px rgba(0,0,0,.06)}\n"
             + ".teoria-card h3{color:#6c63ff;margin-bottom:10px}.teoria-card p{color:#444;white-space:pre-wrap}\n"
             + ".fuente{font-size:.8rem;color:#999;margin-top:10px;font-style:italic}\n"
             + ".btn-ir-quiz{display:block;width:fit-content;margin:24px auto 0;padding:13px 32px;background:#6c63ff;color:#fff;border:none;border-radius:10px;font-weight:700;font-size:.95rem;cursor:pointer}\n"
             + ".progress-bar{background:#e0e0e0;border-radius:99px;height:8px;margin-bottom:24px;overflow:hidden}\n"
             + ".progress-fill{background:#6c63ff;height:100%;border-radius:99px;transition:width .4s}\n"
             + ".pregunta-card{background:#fff;border-radius:12px;padding:28px;box-shadow:0 2px 8px rgba(0,0,0,.06)}\n"
             + ".pregunta-num{font-size:.8rem;color:#999;margin-bottom:6px}\n"
             + ".pregunta-texto{font-size:1.1rem;font-weight:600;margin-bottom:20px}\n"
             + ".opciones{display:flex;flex-direction:column;gap:10px}\n"
             + ".opcion-btn{padding:14px 18px;border:2px solid #e8e7ff;border-radius:10px;background:#fff;text-align:left;cursor:pointer;font-size:.95rem;transition:all .2s;color:#333}\n"
             + ".opcion-btn:hover:not(:disabled){border-color:#6c63ff;background:#f5f4ff}\n"
             + ".opcion-btn.correcta{border-color:#22c55e;background:#f0fdf4;color:#166534}\n"
             + ".opcion-btn.incorrecta{border-color:#ef4444;background:#fef2f2;color:#991b1b}\n"
             + ".opcion-btn:disabled{cursor:not-allowed}\n"
             + ".feedback{margin-top:16px;padding:16px;border-radius:10px;display:none}\n"
             + ".feedback.show{display:block}.feedback.correcto{background:#f0fdf4;border:1px solid #bbf7d0;color:#166534}\n"
             + ".feedback.incorrecto{background:#fef2f2;border:1px solid #fecaca;color:#991b1b}\n"
             + ".feedback-header{font-weight:700;font-size:1rem;margin-bottom:4px}\n"
             + ".feedback-resp{margin-top:4px;font-size:.9rem}.feedback-exp{margin-top:10px;font-size:.9rem;color:#555;padding-top:10px;border-top:1px solid rgba(0,0,0,.1)}\n"
             + ".nav-btns{display:flex;justify-content:flex-end;margin-top:20px}\n"
             + ".btn-next{background:#6c63ff;color:#fff;border:none;padding:12px 28px;border-radius:10px;font-weight:600;cursor:pointer;font-size:.95rem;display:none}\n"
             + ".btn-next.visible{display:block}\n"
             + ".resultado-card{background:#fff;border-radius:16px;padding:40px;text-align:center;box-shadow:0 4px 16px rgba(0,0,0,.1)}\n"
             + ".score-circle{width:140px;height:140px;border-radius:50%;display:flex;flex-direction:column;align-items:center;justify-content:center;margin:0 auto 24px;font-weight:700}\n"
             + ".score-circle.aprobado{background:#f0fdf4;border:6px solid #22c55e;color:#166534}\n"
             + ".score-circle.reprobado{background:#fef2f2;border:6px solid #ef4444;color:#991b1b}\n"
             + ".score-num{font-size:2.4rem}.score-pct{font-size:.8rem;opacity:.7}\n"
             + ".res-titulo{font-size:1.5rem;font-weight:700;margin-bottom:6px}.res-sub{color:#666;margin-bottom:24px}\n"
             + ".stats{display:flex;justify-content:center;gap:40px;margin-bottom:28px}\n"
             + ".stat-num{font-size:1.6rem;font-weight:700;color:#6c63ff}.stat-label{font-size:.8rem;color:#999}\n"
             + ".btn-retry{padding:12px 28px;background:#6c63ff;color:#fff;border:none;border-radius:10px;font-weight:700;cursor:pointer}\n"
             + ".hidden{display:none}\n";
    }

    private String buildQuizHtml(Cuestionario c, List<Pregunta> preguntas, List<Teoria> teorias) {
        String titulo  = escHtml(c.getTitulo());
        String desc    = c.getDescripcion() != null ? escHtml(c.getDescripcion()) : "Cuestionario de Scrum";
        String nivel   = c.getNivel() != null ? escHtml(c.getNivel()) : "Básico";
        int duracion   = c.getDuracionMinutos() != null ? c.getDuracionMinutos() : 15;
        int totalPts   = preguntas.stream().mapToInt(p -> p.getPuntaje() != null ? p.getPuntaje() : 50).sum();

        StringBuilder teoriaHtml = new StringBuilder();
        if (teorias == null || teorias.isEmpty()) {
            teoriaHtml.append("<div class=\"teoria-card\"><p>No hay contenido teórico disponible.</p></div>");
        } else {
            for (Teoria t : teorias) {
                teoriaHtml.append("<div class=\"teoria-card\">");
                if (t.getTitulo()   != null) teoriaHtml.append("<h3>").append(escHtml(t.getTitulo())).append("</h3>");
                if (t.getContenido()!= null) teoriaHtml.append("<p>").append(escHtml(t.getContenido())).append("</p>");
                if (t.getFuente()   != null) teoriaHtml.append("<p class=\"fuente\">&#128218; ").append(escHtml(t.getFuente())).append("</p>");
                teoriaHtml.append("</div>");
            }
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(p.getId()).append(",")
                .append("\"texto\":").append(js(p.getTexto())).append(",")
                .append("\"opciones\":").append(js(p.getOpciones())).append(",")
                .append("\"respuesta\":").append(js(p.getRespuestaCorrecta())).append(",")
                .append("\"puntaje\":").append(p.getPuntaje() != null ? p.getPuntaje() : 50).append(",")
                .append("\"explicacion\":").append(js(p.getExplicacion() != null ? p.getExplicacion() : ""))
                .append("}");
        }
        json.append("]");

        return "<!DOCTYPE html><html lang=\"es\"><head>\n"
             + "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n"
             + "<title>" + titulo + "</title>\n"
             + "<link rel=\"stylesheet\" href=\"style.css\">\n"
             + "<script src=\"scorm12.js\"></script>\n"
             + "</head><body>\n"
             + "<div class=\"container\">\n"
             + "  <div class=\"header\"><h1>" + titulo + "</h1><p>" + desc + "</p>\n"
             + "  <span class=\"badge\">&#128218; SCORM 1.2 &nbsp;|&nbsp; Nivel " + nivel + " &nbsp;|&nbsp; " + duracion + " min</span></div>\n"
             + "  <div class=\"tabs\">\n"
             + "    <button class=\"tab-btn active\" id=\"btnT\" onclick=\"showTab('t')\">&#128214; Teor&iacute;a</button>\n"
             + "    <button class=\"tab-btn\" id=\"btnQ\" onclick=\"showTab('q')\">&#9998; Quiz</button>\n"
             + "  </div>\n"
             + "  <div id=\"tabT\">" + teoriaHtml
             + "    <button class=\"btn-ir-quiz\" onclick=\"showTab('q')\">Ir al Quiz &rarr;</button></div>\n"
             + "  <div id=\"tabQ\" class=\"hidden\">\n"
             + "    <div class=\"progress-bar\"><div class=\"progress-fill\" id=\"pFill\" style=\"width:0%\"></div></div>\n"
             + "    <div class=\"pregunta-card\" id=\"pCard\">\n"
             + "      <div class=\"pregunta-num\" id=\"pNum\"></div>\n"
             + "      <div class=\"pregunta-texto\" id=\"pTxt\"></div>\n"
             + "      <div class=\"opciones\" id=\"pOpts\"></div>\n"
             + "      <div class=\"feedback\" id=\"fb\"><div class=\"feedback-header\" id=\"fbH\"></div><div class=\"feedback-resp\" id=\"fbR\"></div><div class=\"feedback-exp\" id=\"fbE\"></div></div>\n"
             + "      <div class=\"nav-btns\"><button class=\"btn-next\" id=\"btnN\" onclick=\"siguiente()\">Siguiente &rarr;</button></div>\n"
             + "    </div>\n"
             + "    <div class=\"resultado-card hidden\" id=\"resCard\">\n"
             + "      <div class=\"score-circle\" id=\"sCircle\"><span class=\"score-num\" id=\"sNum\"></span><span class=\"score-pct\" id=\"sPct\"></span></div>\n"
             + "      <div class=\"res-titulo\" id=\"resTit\"></div><div class=\"res-sub\" id=\"resSub\"></div>\n"
             + "      <div class=\"stats\">\n"
             + "        <div class=\"stat\"><div class=\"stat-num\" id=\"stC\">0</div><div class=\"stat-label\">Correctas</div></div>\n"
             + "        <div class=\"stat\"><div class=\"stat-num\" id=\"stI\">0</div><div class=\"stat-label\">Incorrectas</div></div>\n"
             + "        <div class=\"stat\"><div class=\"stat-num\" id=\"stP\">0%</div><div class=\"stat-label\">Porcentaje</div></div>\n"
             + "      </div>\n"
             + "      <button class=\"btn-retry\" onclick=\"reiniciar()\">&#128260; Reintentar</button>\n"
             + "    </div>\n"
             + "  </div>\n"
             + "</div>\n"
             + "<script>\n"
             + "var Q=" + json + ",TP=" + totalPts + ",idx=0,corr=0,inc=0,pts=0,resp=false;\n"
             + "window.onload=function(){ScormAPI.init();ScormAPI.set('cmi.core.lesson_status','incomplete');ScormAPI.commit();if(Q.length)show(0);};\n"
             + "window.onbeforeunload=function(){if(ScormAPI.initialized){ScormAPI.set('cmi.core.lesson_status','incomplete');ScormAPI.commit();}};\n"
             + "function showTab(t){document.getElementById('tabT').classList.toggle('hidden',t!=='t');document.getElementById('tabQ').classList.toggle('hidden',t!=='q');document.getElementById('btnT').classList.toggle('active',t==='t');document.getElementById('btnQ').classList.toggle('active',t==='q');}\n"
             + "function show(i){var p=Q[i];resp=false;\n"
             + "  document.getElementById('pNum').textContent='Pregunta '+(i+1)+' de '+Q.length;\n"
             + "  document.getElementById('pTxt').textContent=p.texto;\n"
             + "  document.getElementById('pFill').style.width=((i/Q.length)*100)+'%';\n"
             + "  document.getElementById('fb').className='feedback';\n"
             + "  document.getElementById('btnN').classList.remove('visible');\n"
             + "  var opts=[];try{opts=JSON.parse(p.opciones);}catch(e){opts=[p.respuesta];}\n"
             + "  var c=document.getElementById('pOpts');c.innerHTML='';\n"
             + "  opts.forEach(function(o){var b=document.createElement('button');b.className='opcion-btn';b.textContent=o;b.setAttribute('data-v',o);b.onclick=function(){answer(o,b,p);};c.appendChild(b);});}\n"
             + "function answer(val,btn,p){if(resp)return;resp=true;\n"
             + "  var ok=val.trim().toLowerCase()===p.respuesta.trim().toLowerCase();\n"
             + "  document.querySelectorAll('.opcion-btn').forEach(function(b){b.disabled=true;if(b.getAttribute('data-v').trim().toLowerCase()===p.respuesta.trim().toLowerCase())b.classList.add('correcta');});\n"
             + "  if(ok){corr++;pts+=(p.puntaje||50);btn.classList.add('correcta');\n"
             + "    document.getElementById('fb').className='feedback correcto show';document.getElementById('fbH').textContent='\\u2705 \\u00a1Correcto!';document.getElementById('fbR').textContent='';}\n"
             + "  else{inc++;btn.classList.add('incorrecta');\n"
             + "    document.getElementById('fb').className='feedback incorrecto show';document.getElementById('fbH').textContent='\\u274C Incorrecto';document.getElementById('fbR').textContent='Respuesta correcta: '+p.respuesta;}\n"
             + "  document.getElementById('fbE').textContent=p.explicacion?'\\uD83D\\uDCA1 '+p.explicacion:'';\n"
             + "  ScormAPI.set('cmi.interactions.'+idx+'.id','p_'+p.id);ScormAPI.set('cmi.interactions.'+idx+'.student_response',val);ScormAPI.set('cmi.interactions.'+idx+'.result',ok?'correct':'wrong');ScormAPI.commit();\n"
             + "  document.getElementById('btnN').classList.add('visible');}\n"
             + "function siguiente(){idx++;if(idx<Q.length)show(idx);else resultado();}\n"
             + "function resultado(){\n"
             + "  document.getElementById('pCard').classList.add('hidden');document.getElementById('resCard').classList.remove('hidden');\n"
             + "  document.getElementById('pFill').style.width='100%';\n"
             + "  var t=corr+inc,p=t>0?Math.round((corr/t)*100):0,ap=p>=60,s=TP>0?Math.round((pts/TP)*100):p;\n"
             + "  document.getElementById('sNum').textContent=pts;document.getElementById('sPct').textContent=p+'%';\n"
             + "  document.getElementById('sCircle').className='score-circle '+(ap?'aprobado':'reprobado');\n"
             + "  document.getElementById('resTit').textContent=ap?'\\uD83C\\uDF89 \\u00a1Aprobado!':'\\uD83D\\uDE14 No aprobado';\n"
             + "  document.getElementById('resSub').textContent=ap?'\\u00a1Excelente! Demostraste dominio del contenido.':'Revisa la teor\\u00eda e int\\u00e9ntalo de nuevo.';\n"
             + "  document.getElementById('stC').textContent=corr;document.getElementById('stI').textContent=inc;document.getElementById('stP').textContent=p+'%';\n"
             + "  ScormAPI.finish(ap?'passed':'failed',s,100);}\n"
             + "function reiniciar(){idx=0;corr=0;inc=0;pts=0;resp=false;\n"
             + "  document.getElementById('pCard').classList.remove('hidden');document.getElementById('resCard').classList.add('hidden');\n"
             + "  ScormAPI.init();ScormAPI.set('cmi.core.lesson_status','incomplete');show(0);}\n"
             + "</script></body></html>\n";
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Helpers de escape
    // ───────────────────────────────────────────────────────────────────────────

    private String escXml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }

    private String js(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\","\\\\").replace("\"","\\\"")
                        .replace("\n","\\n").replace("\r","\\r").replace("\t","\\t") + "\"";
    }
}
