const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const resources = path.resolve(__dirname, '../../main/resources');

function load({ saved, search = '', unavailableStorage = false } = {}) {
  const storage = new Map(saved ? [['najdi-usluga.language', saved]] : []);
  const document = { documentElement: {}, querySelectorAll: () => [], querySelector: () => null };
  const context = vm.createContext({
    window: { location: { search } }, document, URLSearchParams, Intl,
    localStorage: {
      getItem(key) { if (unavailableStorage) throw Error('blocked'); return storage.get(key); },
      setItem(key, value) { if (unavailableStorage) throw Error('blocked'); storage.set(key, value); }
    }
  });
  for (const file of ['i18n-messages.js', 'i18n.js']) {
    vm.runInContext(fs.readFileSync(path.join(resources, 'static/js', file), 'utf8'), context);
  }
  return { ...context.window.I18n, storage, document };
}

test('Macedonian is the default and saved English survives navigation', () => {
  assert.equal(load().language, 'mk');
  assert.equal(load().document.documentElement.lang, 'mk');
  assert.equal(load({saved: 'en'}).t('Log in'), 'Log in');
  assert.equal(load({saved: 'invalid'}).language, 'mk');
});

test('explicit language overrides and updates the preference; unsupported values are ignored', () => {
  const result = load({saved: 'mk', search: '?query=cleaning&lang=en'});
  assert.equal(result.language, 'en');
  assert.equal(result.storage.get('najdi-usluga.language'), 'en');
  assert.equal(load({search: '?lang=xx'}).language, 'mk');
});

test('language selection still works when browser storage is blocked', () => {
  assert.equal(load({unavailableStorage: true}).language, 'mk');
  assert.equal(load({search: '?lang=en', unavailableStorage: true}).language, 'en');
});

test('formats interpolated messages, titles, statuses and server errors without corrupting values', () => {
  const {t, number} = load();
  assert.equal(t('Page {page} of {total}', {page: 2, total: 7}), 'Страница 2 од 7');
  assert.equal(t('Log in · Najdi Usluga'), 'Најави се · Најди Услуга');
  assert.equal(t('Reservation cannot transition from COMPLETED'), 'Не е дозволена промена од статусот Завршена');
  assert.equal(t('Invalid email or password'), 'Неточна е-пошта или лозинка');
  assert.equal(t('Searching around {location}', {location: '$& <Skopje>'}), 'Пребарување во близина на $& <Skopje>');
  assert.equal(t('Unknown message'), 'Unknown message');
  assert.equal(t('constructor'), 'constructor');
  assert.equal(number(4.5), '4,5');
});

test('every page includes a language selector and loads translations before page scripts', () => {
  for (const file of fs.readdirSync(path.join(resources, 'templates'))) {
    if (!file.endsWith('.html')) continue;
    const html = fs.readFileSync(path.join(resources, 'templates', file), 'utf8');
    assert.match(html, /<html lang="mk"/, file);
    assert.equal((html.match(/id="language-switcher"/g) || []).length, 1, file);
    const scripts = [...html.matchAll(/<script[^>]+(?:src|th:src)="([^"]+)"/g)].map(match => match[1]);
    assert.deepEqual(scripts.slice(0, 2), ['/js/i18n-messages.js', '/js/i18n.js'], file);
  }
});
