function normalizeCategoryDurations() {
  if (typeof normalizeCategoryLinks === 'function') normalizeCategoryLinks();
  q('#category-view .test-list-item>.test-item').forEach(function (item) {
    var links = q('a.linked[test-id],a.linked[data-atf-test-id]', item),
      total = 0,
      count = 0;
    links.forEach(function (link) {
      var id =
        link.getAttribute('data-atf-test-id') || link.getAttribute('test-id');
      if (!id) return;
      var index = link.getAttribute('data-atf-scenario-index');
      if (index === null && typeof categoryScenarioIndex === 'function')
        index = String(categoryScenarioIndex(link, id));
      var card = scenarioCardFor(id, index);
      var value = card && card.getAttribute('data-duration-ms');
      if (value !== null && /^\d+(?:\.\d+)?$/.test(value)) {
        total += Number(value);
        count++;
      }
    });
    if (!count) return;
    var field =
      item.querySelector('.test-detail .duration') ||
      item.querySelector('.test-detail .text-sm');
    if (!field) return;
    var original = field.getAttribute('data-atf-original-label');
    if (original === null) {
      original = text(field, '');
      field.setAttribute('data-atf-original-label', original);
    }
    field.textContent = original + ' · ' + atfFormatDuration(total);
    field.setAttribute('data-duration-ms', String(total));
    field.setAttribute('data-duration-source', 'aggregated');
  });
}
function normalizeOverviewDuration() {
  var timeline = document.getElementById('timeline'),
    host =
      timeline ||
      document.querySelector('#dashboard-view .card:has(#timeline)') ||
      document.getElementById('dashboard-view');
  if (!host) return;
  var total = 0,
    source = window.timeline || {};
  Object.keys(source).forEach(function (key) {
    var value = Number(source[key]);
    if (Number.isFinite(value) && value >= 0) total += Math.round(value * 1000);
  });
  host.setAttribute('data-duration-ms', String(total));
  host.setAttribute('data-duration-source', 'aggregated');
  host.setAttribute(
    'aria-label',
    'Timeline duration ' + atfFormatDuration(total)
  );
  host.setAttribute('title', 'Timeline duration ' + atfFormatDuration(total));
}
function normalizeFailsDurations() {
  q('#atf-fails-view .atf-failure').forEach(function (row) {
    var card = scenarioCardFor(
      row.getAttribute('data-atf-test-id'),
      row.getAttribute('data-atf-scenario-index')
    );
    var value = card && card.getAttribute('data-duration-ms');
    if (value === null) return;
    row.setAttribute('data-duration-ms', value);
    row.setAttribute('data-duration-source', 'aggregated');
  });
}
function normalizeProjectedDurations() {
  normalizeCategoryDurations();
  normalizeOverviewDuration();
  normalizeFailsDurations();
}
