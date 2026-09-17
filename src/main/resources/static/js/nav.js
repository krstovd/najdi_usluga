(() => {
  const { t } = window.I18n;
  const container = document.querySelector('[data-auth-nav]'); if (!container) return;
  function link(text, href) { const item = document.createElement('a'); item.className = 'text-link'; item.textContent = text; item.href = href; return item; }
  async function logout() { const csrfResponse = await fetch('/api/auth/csrf'); const token = await csrfResponse.json(); await fetch('/api/auth/logout', { method: 'POST', headers: { [token.headerName]: token.token } }); window.location.assign('/'); }
  fetch('/api/auth/me').then(async response => response.ok ? response.json() : null).then(user => {
    container.replaceChildren();
    if (!user) { container.append(link(t('Log in'), '/login'), link(t('Register'), '/register')); return; }

    container.classList.add('account-nav');
    const account = link('', '/my/profile'); account.className = 'account-chip'; account.setAttribute('aria-label', t('Open my profile'));
    const avatar = document.createElement('span'); avatar.className = 'account-avatar'; avatar.textContent = `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();
    const identity = document.createElement('span'); identity.className = 'account-identity';
    const name = document.createElement('strong'); name.textContent = `${user.firstName} ${user.lastName}`;
    const role = document.createElement('small'); role.textContent = user.role === 'PROVIDER' ? t('Service provider') : user.role === 'ADMIN' ? t('Administrator') : t('Customer');
    identity.append(name, role); account.append(avatar, identity);

    const destinations = { USER: ['/my/reservations', t('My bookings')], PROVIDER: ['/provider/dashboard', t('Dashboard')], ADMIN: ['/admin', t('Admin panel')] };
    const destination = destinations[user.role]; const dashboard = link(destination[1], destination[0]); dashboard.className = 'dashboard-link';
    const button = document.createElement('button'); button.className = 'logout-button'; button.type = 'button'; button.setAttribute('aria-label', t('Log out')); button.title = t('Log out'); button.innerHTML = '<span></span><b aria-hidden="true">↗</b>'; button.firstElementChild.textContent = t('Log out'); button.addEventListener('click', logout);
    container.append(account, dashboard, button);
  }).catch(() => container.append(link(t('Log in'), '/login')));
})();
