/* ============================================================
   HandOver.AI — app.js  (Core helpers)
   ============================================================ */

/** Currently authenticated user (populated by checkAuth) */
let currentUser = null;

/**
 * Wrapper around fetch that:
 *   • sets credentials: 'same-origin'
 *   • auto-sets Content-Type for JSON bodies
 *   • redirects to login.html on 401
 *   • returns parsed JSON (or null for 204/empty)
 */
/**
 * Return the CSRF token issued by Spring Security in the XSRF-TOKEN cookie.
 * If the cookie is not present yet (e.g. the first request after loading the
 * login page), ask the server to create one via /api/auth/csrf.
 */
let csrfBootstrap = null;

async function ensureCsrf() {
  if (getCookie('XSRF-TOKEN')) return;
  if (!csrfBootstrap) {
    csrfBootstrap = fetch('/api/auth/csrf', { credentials: 'same-origin' })
      .then(res => (res.ok ? res.json() : null))
      .then(data => { if (data && data.token) window.__csrfToken = data.token; })
      .catch(() => null)
      .finally(() => { csrfBootstrap = null; });
  }
  await csrfBootstrap;
}

async function api(url, options = {}) {
  const defaults = { credentials: 'same-origin', headers: {} };
  const opts = { ...defaults, ...options, headers: { ...defaults.headers, ...options.headers } };

  if (opts.body && typeof opts.body === 'object' && !(opts.body instanceof FormData)) {
    opts.body = JSON.stringify(opts.body);
    opts.headers['Content-Type'] = 'application/json';
  }

  const method = (opts.method || 'GET').toUpperCase();
  if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
    await ensureCsrf();
    const csrf = getCookie('XSRF-TOKEN') || window.__csrfToken;
    if (csrf) opts.headers['X-XSRF-TOKEN'] = csrf;
  }

  const res = await fetch(url, opts);

  // Login/face endpoints must surface their own 401 message instead of redirecting.
  const isAuthEndpoint = url.includes('/api/auth/login') || url.includes('/api/auth/face/verify');
  if (res.status === 401 && !isAuthEndpoint) {
    window.location.href = '/login.html';
    throw new Error('Unauthorized');
  }

  if (!res.ok) {
    let msg = `Error ${res.status}`;
    try {
      const err = await res.json();
      msg = err.message || err.error || msg;
    } catch (_) { /* no json body */ }
    throw new Error(msg);
  }

  const ct = res.headers.get('Content-Type') || '';
  if (res.status === 204 || !ct.includes('application/json')) return null;
  return res.json();
}

function getCookie(name) {
  return document.cookie.split('; ').find(row => row.startsWith(name + '='))?.split('=')[1] || null;
}

/* ---------- Friendly error message helper ---------- */
function errorText(err) {
  if (!err) return 'Something went wrong.';
  return err.message || String(err);
}

/* ---------- Authentication ---------- */
async function checkAuth() {
  try {
    currentUser = await api('/api/auth/me');
    return currentUser;
  } catch {
    window.location.href = '/login.html';
    return null;
  }
}

async function logout() {
  try { await api('/api/auth/logout', { method: 'POST' }); } catch { /* ok */ }
  currentUser = null;
  window.location.href = '/login.html';
}

/* ---------- Theme Toggle ---------- */
function initTheme() {
  const saved = localStorage.getItem('ho-theme') || 'dark';
  applyTheme(saved);
}

function applyTheme(mode) {
  document.documentElement.setAttribute('data-bs-theme', mode);
  localStorage.setItem('ho-theme', mode);
  const icon = document.getElementById('themeIcon');
  if (icon) icon.className = mode === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
}

function toggleTheme() {
  const current = document.documentElement.getAttribute('data-bs-theme');
  applyTheme(current === 'dark' ? 'light' : 'dark');
}

/* ---------- Toast Notifications ---------- */
let toastContainer = null;

function showToast(message, type = 'info') {
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.className = 'ho-toast-container';
    toastContainer.setAttribute('aria-live', 'polite');
    document.body.appendChild(toastContainer);
  }

  const colorMap = {
    success: 'bg-success',
    error:   'bg-danger',
    warning: 'bg-warning text-dark',
    info:    'bg-info text-dark'
  };

  const id = 'toast-' + Date.now();
  const html = `
    <div id="${id}" class="toast align-items-center text-white ${colorMap[type] || colorMap.info} border-0 mb-2" role="alert" aria-live="assertive" aria-atomic="true">
      <div class="d-flex">
        <div class="toast-body fw-semibold">${escapeHtml(message)}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
      </div>
    </div>`;

  toastContainer.insertAdjacentHTML('beforeend', html);
  const el = document.getElementById(id);
  const toast = new bootstrap.Toast(el, { delay: 4500 });
  toast.show();
  el.addEventListener('hidden.bs.toast', () => el.remove());
}

/* ---------- Utility Helpers ---------- */
function escapeHtml(text) {
  const d = document.createElement('div');
  d.textContent = text;
  return d.innerHTML;
}

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function statusBadge(status) {
  if (!status) return '';
  const s = status.toUpperCase().replace(/[\s_]+/g, '_');
  const map = {
    APPROVED: 'badge-approved',
    COMPLETED: 'badge-approved',
    DONE: 'badge-approved',
    PENDING: 'badge-pending',
    PENDING_APPROVAL: 'badge-pending',
    IN_PROGRESS: 'badge-info',
    OVERDUE: 'badge-overdue',
    BLOCKED: 'badge-blocked',
    REJECTED: 'badge-overdue',
    CRITICAL: 'badge-overdue',
    HIGH: 'badge-overdue',
    MEDIUM: 'badge-pending',
    LOW: 'badge-info',
  };
  const cls = map[s] || 'badge-info';
  return `<span class="badge ${cls}">${escapeHtml(status)}</span>`;
}

function getProjectId() {
  return new URLSearchParams(window.location.search).get('id');
}

/* ---------- Render current user in navbar ---------- */
function renderUserNav() {
  if (!currentUser) return;
  const nameEl = document.getElementById('navUserName');
  const roleEl = document.getElementById('navUserRole');
  if (nameEl) nameEl.textContent = currentUser.name || currentUser.email;
  if (roleEl) {
    const r = (currentUser.role || '').replace('ROLE_', '');
    roleEl.textContent = r;
    roleEl.className = 'badge badge-teal ms-1';
  }
}

/* ---------- Init on every protected page ---------- */
function initPage() {
  initTheme();
}

// Run theme immediately
initTheme();
