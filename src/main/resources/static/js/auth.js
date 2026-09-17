(() => {
  const { t } = window.I18n;
  const form = document.querySelector('#login-form, #register-form'); const status = document.querySelector('#auth-status'); const registering = form.id === 'register-form';
  function show(message, error = false) { status.hidden = !message; status.textContent = t(message || ''); status.classList.toggle('error', error); }
  async function csrf() { const response = await fetch('/api/auth/csrf'); if (!response.ok) throw new Error(t('Security token could not be loaded.')); return response.json(); }
  function destination(user) { if (user.role === 'ADMIN') return '/admin'; if (user.role === 'PROVIDER') return '/provider/dashboard'; return '/my/reservations'; }
  form.addEventListener('submit', async event => { event.preventDefault(); show(registering ? t('Creating account…') : t('Logging in…')); try { const token = await csrf(); const payload = Object.fromEntries(new FormData(form)); const response = await fetch(registering ? '/api/auth/register' : '/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json', [token.headerName]: token.token }, body: JSON.stringify(payload) }); const body = await response.json().catch(() => ({})); if (!response.ok) throw new Error(body.message || (registering ? t('Account could not be created.') : t('Invalid email or password.'))); if (registering) { show(t('Account created. Redirecting to login…')); setTimeout(() => window.location.assign('/login'), 700); } else window.location.assign(destination(body)); } catch (error) { show(error.message, true); } });
})();
