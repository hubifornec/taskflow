// Toast lightweight para login
function toast(msg, tipo = "success") {
  let c = document.getElementById("ltoast");
  if (!c) { c = document.createElement("div"); c.id = "ltoast";
    c.style = "position:fixed;bottom:24px;right:24px;z-index:9999;display:flex;flex-direction:column;gap:8px;"; 
    document.body.appendChild(c); }
  const colors = {success:"#22c55e",error:"#ef4444",warning:"#f59e0b",info:"#3b82f6"};
  const t = document.createElement("div");
  t.style = `background:${colors[tipo]||"#3b82f6"};color:#fff;padding:13px 18px;border-radius:10px;font-size:0.88rem;font-weight:500;box-shadow:0 8px 24px rgba(0,0,0,0.18);max-width:320px;opacity:0;transform:translateX(40px);transition:0.3s;`;
  t.textContent = msg;
  c.appendChild(t);
  requestAnimationFrame(() => { t.style.opacity = "1"; t.style.transform = "translateX(0)"; });
  setTimeout(() => { t.style.opacity = "0"; t.style.transform = "translateX(40px)"; setTimeout(() => t.remove(), 350); }, 3500);
}

// URL BASE DEL BACKEND — debe coincidir con la de script.js
const API_BASE = "";

function mostrarRegistro() {
  document.getElementById("loginForm").classList.add("oculto");
  document.getElementById("registroForm").classList.remove("oculto");
  document.getElementById("sidebarLogin").classList.add("oculto");
  document.getElementById("sidebarRegistro").classList.remove("oculto");
}

function mostrarLogin() {
  document.getElementById("registroForm").classList.add("oculto");
  document.getElementById("loginForm").classList.remove("oculto");
  document.getElementById("sidebarRegistro").classList.add("oculto");
  document.getElementById("sidebarLogin").classList.remove("oculto");
}

async function registrar() {
  const nombre = document.getElementById("nombre").value.trim();
  const email = document.getElementById("emailRegistro").value.trim();
  const password = document.getElementById("passwordRegistro").value;
  const passwordConfirm = document.getElementById("passwordConfirm").value;
  const errorEl = document.getElementById("errorRegistro");

  if (!nombre || !email || !password) {
    mostrarErrorRegistro("Todos los campos son obligatorios");
    return;
  }

  if (password !== passwordConfirm) {
    mostrarErrorRegistro("Las contraseñas no coinciden");
    return;
  }

  const regexMayuscula = /[A-Z]/;
  const regexNumero = /[0-9]/;
  const regexEspecial = /[!@#$%^&*()_+\-=\[\]{}|;':",./<>?]/;

  if (password.length < 8) {
    mostrarErrorRegistro("La contraseña debe tener mínimo 8 caracteres");
    return;
  }
  if (!regexMayuscula.test(password)) {
    mostrarErrorRegistro("La contraseña debe tener al menos una mayúscula");
    return;
  }
  if (!regexNumero.test(password)) {
    mostrarErrorRegistro("La contraseña debe tener al menos un número");
    return;
  }
  if (!regexEspecial.test(password)) {
    mostrarErrorRegistro(
      "La contraseña debe tener al menos un carácter especial (!@#$%...)",
    );
    return;
  }

  errorEl.classList.add("oculto");

  const res = await fetch(API_BASE + "/usuarios", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nombre, email, password }),
  });

  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    mostrarErrorRegistro(data.error || "Error al registrar usuario");
    return;
  }

  toast("¡Cuenta creada! Ya puedes iniciar sesión.", "success");
  setTimeout(mostrarLogin, 1500);
}

function mostrarErrorRegistro(msg) {
  const el = document.getElementById("errorRegistro");
  el.textContent = msg;
  el.classList.remove("oculto");
}

async function login() {
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const errorEl = document.getElementById("errorLogin");

  errorEl.classList.add("oculto");

  const res = await fetch(API_BASE + "/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (res.status === 401) {
    errorEl.classList.remove("oculto");
    return;
  }

  if (!res.ok) {
    errorEl.classList.remove("oculto");
    return;
  }

  const usuario = await res.json();
  sessionStorage.setItem("usuario", JSON.stringify(usuario));
  window.location.href = "index.html";
}
