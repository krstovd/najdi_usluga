(() => {
  'use strict';
  const messages = window.I18nMessages;
  const storageKey = 'najdi-usluga.language';
  const requested = new URLSearchParams(window.location.search).get('lang');
  let saved;
  try { saved = localStorage.getItem(storageKey); } catch { /* Storage may be disabled. */ }
  const language = ['mk', 'en'].includes(requested) ? requested : saved === 'en' ? 'en' : 'mk';
  if (requested === 'mk' || requested === 'en') {
    try { localStorage.setItem(storageKey, language); } catch { /* URL still selects the language. */ }
  }
  document.documentElement.lang = language;

  function t(key, values = {}) {
    if (key == null) return '';
    let result = language === 'mk' && Object.hasOwn(messages, key) ? messages[key] : key;
    if (language === 'mk' && key.endsWith(' · Najdi Usluga')) {
      result = `${t(key.slice(0, -15))} · ${t('Najdi Usluga')}`;
    }
    if (language === 'mk' && !Object.hasOwn(messages, key) && key.startsWith('Reservation cannot transition from ')) {
      return t('Reservation cannot transition from {status}', {status: t(key.slice(35))});
    }
    return result.replace(/\{(\w+)\}/g, (match, name) => values[name] ?? match);
  }

  window.I18n = Object.freeze({
    language,
    t,
    number: value => new Intl.NumberFormat(language === 'mk' ? 'mk-MK' : 'en-GB').format(value),
    date: value => new Date(value).toLocaleDateString(language === 'mk' ? 'mk-MK' : 'en-GB')
  });

  // Only explicitly marked template text is translated. User-written content is never scanned.
  document.querySelectorAll('[data-i18n-text]').forEach(element => {
    const nodes = [...element.childNodes].filter(node => node.nodeType === Node.TEXT_NODE);
    JSON.parse(element.dataset.i18nText).forEach(([index, key]) => {
      const node = nodes[index];
      if (node) node.textContent = node.textContent.replace(/\S[\s\S]*\S|\S/, () => t(key));
    });
  });
  ['placeholder', 'aria-label', 'alt', 'title', 'content'].forEach(attribute => {
    document.querySelectorAll(`[data-i18n-${attribute}]`).forEach(element => {
      element.setAttribute(attribute, t(element.getAttribute(`data-i18n-${attribute}`)));
    });
  });
  const switcher = document.querySelector('#language-switcher');
  if (switcher) {
    switcher.value = language;
    switcher.addEventListener('change', () => {
      try { localStorage.setItem(storageKey, switcher.value); } catch { /* Use URL fallback. */ }
      const url = new URL(window.location.href);
      url.searchParams.set('lang', switcher.value);
      window.location.assign(url.href);
    });

    const options = [...switcher.options];
    const control = document.createElement('div');
    control.className = 'language-menu';
    control.innerHTML = `
      <button class="language-menu__trigger" type="button" aria-haspopup="listbox" aria-expanded="false" aria-label="${switcher.getAttribute('aria-label') || 'Language'}">
        <span></span><svg viewBox="0 0 20 20" aria-hidden="true"><path d="m5 7.5 5 5 5-5"/></svg>
      </button>
      <div class="language-menu__list" role="listbox" hidden></div>`;
    const trigger = control.querySelector('.language-menu__trigger');
    const triggerLabel = trigger.querySelector('span');
    const list = control.querySelector('.language-menu__list');

    function selectLanguage(value) {
      switcher.value = value;
      switcher.dispatchEvent(new Event('change', { bubbles: true }));
    }
    function syncSelection() {
      const selected = options.find(option => option.value === switcher.value);
      triggerLabel.textContent = selected?.textContent || switcher.value;
      list.querySelectorAll('[role="option"]').forEach(option => {
        const active = option.dataset.value === switcher.value;
        option.setAttribute('aria-selected', String(active));
        option.classList.toggle('is-selected', active);
      });
    }

    options.forEach(option => {
      const item = document.createElement('button');
      item.type = 'button';
      item.className = 'language-menu__option';
      item.setAttribute('role', 'option');
      item.dataset.value = option.value;
      item.lang = option.lang;
      item.textContent = option.textContent;
      item.addEventListener('click', () => selectLanguage(option.value));
      list.append(item);
    });
    trigger.addEventListener('click', () => {
      const opening = list.hidden;
      list.hidden = !opening;
      trigger.setAttribute('aria-expanded', String(opening));
      if (opening) list.querySelector('[aria-selected="true"]')?.focus();
    });
    control.addEventListener('keydown', event => {
      if (event.key === 'Escape' && !list.hidden) {
        list.hidden = true;
        trigger.setAttribute('aria-expanded', 'false');
        trigger.focus();
      }
    });
    document.addEventListener('click', event => {
      if (!control.contains(event.target)) {
        list.hidden = true;
        trigger.setAttribute('aria-expanded', 'false');
      }
    });
    syncSelection();
    switcher.hidden = true;
    switcher.insertAdjacentElement('afterend', control);
  }
})();
