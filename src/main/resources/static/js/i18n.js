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
  }
})();
