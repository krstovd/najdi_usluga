(() => {
  const container = document.querySelector('[data-auth-nav]'); if (!container) return;
  function link(text, href) { const item = document.createElement('a'); item.className = 'text-link'; item.textContent = text; item.href = href; return item; }
  async function logout() { const csrfResponse = await fetch('/api/auth/csrf'); const token = await csrfResponse.json(); await fetch('/api/auth/logout', { method: 'POST', headers: { [token.headerName]: token.token } }); window.location.assign('/'); }
  fetch('/api/auth/me').then(async response => response.ok ? response.json() : null).then(user => {
    container.replaceChildren();
    if (!user) { container.append(link('Log in', '/login'), link('Register', '/register')); return; }

    container.classList.add('account-nav');
    const account = link('', '/my/profile'); account.className = 'account-chip'; account.setAttribute('aria-label', 'Open my profile');
    const avatar = document.createElement('span'); avatar.className = 'account-avatar'; avatar.textContent = `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();
    const identity = document.createElement('span'); identity.className = 'account-identity';
    const name = document.createElement('strong'); name.textContent = `${user.firstName} ${user.lastName}`;
    const role = document.createElement('small'); role.textContent = user.role === 'PROVIDER' ? 'Service provider' : user.role === 'ADMIN' ? 'Administrator' : 'Customer';
    identity.append(name, role); account.append(avatar, identity);

    const destinations = { USER: ['/my/reservations', 'My bookings'], PROVIDER: ['/provider/dashboard', 'Dashboard'], ADMIN: ['/admin', 'Admin panel'] };
    const destination = destinations[user.role]; const dashboard = link(destination[1], destination[0]); dashboard.className = 'dashboard-link';
    const button = document.createElement('button'); button.className = 'logout-button'; button.type = 'button'; button.setAttribute('aria-label', 'Log out'); button.title = 'Log out'; button.innerHTML = '<span>Log out</span><b aria-hidden="true">↗</b>'; button.addEventListener('click', logout);
    container.append(account, dashboard, button);
  }).catch(() => container.append(link('Log in', '/login')));
})();
