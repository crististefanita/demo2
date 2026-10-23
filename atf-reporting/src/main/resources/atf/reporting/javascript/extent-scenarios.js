function isScenarioCard(card) {
  var accordion = card && card.parentElement,
    host = accordion && accordion.parentElement,
    contents = host && host.parentElement;
  return !!(
    accordion &&
    accordion.classList.contains('accordion') &&
    host &&
    host.classList.contains('mt-4') &&
    contents &&
    contents.classList.contains('test-contents')
  );
}
function normalizeScenarioAnnotations() {
  q(
    '.test-contents > .mt-4 > .accordion > .card[data-atf-scenario-id]'
  ).forEach(function (card) {
    normalizeScenarioDiagnostics(card);
    var header = card.querySelector(':scope > .card-header'),
      title = header && header.querySelector(':scope > .card-title');
    if (!header || !title) return;
    var captured = readTagValues(card);
    if (captured.length)
      card.setAttribute('data-atf-scenario-tags', captured.join(' '));
    var annotation = title.querySelector(':scope > .atf-scenario-annotations'),
      source = card.querySelector(
        ':scope > .accordion > .card .card-title > .node-attr'
      );
    if (!annotation && source) {
      annotation = document.createElement('div');
      annotation.className = 'atf-scenario-annotations';
      annotation.setAttribute('role', 'list');
      annotation.setAttribute('aria-label', 'Scenario annotations');
      source.querySelectorAll('.badge').forEach(function (badge) {
        var tag = document.createElement('span');
        tag.className = 'atf-tag-annotation atf-tag-annotation-selection';
        tag.setAttribute('role', 'listitem');
        tag.setAttribute('data-atf-tag-kind', 'selection');
        tag.textContent = String(badge.textContent || '')
          .replace(/^\s*[^a-zA-Z0-9_-]\s*/, '')
          .trim();
        if (tag.textContent) annotation.appendChild(tag);
      });
      title.appendChild(annotation);
    }
    if (source && annotation) source.remove();
    title.querySelectorAll(':scope > .node-attr').forEach(function (attrs) {
      attrs.remove();
    });
    card
      .querySelectorAll(':scope > .accordion .node-attr')
      .forEach(function (attrs) {
        attrs.remove();
      });
  });
}

function normalizeScenarioDiagnostics(card) {
  var panel = card.querySelector(':scope > .accordion'),
    body = card.querySelector(':scope > div:not(.card-header):not(.accordion)');
  if (!panel || !body) return;
  var diagnostics = q('details.phase-diagnostics', body),
    before = panel.firstChild;
  diagnostics.forEach(function (diagnostic) {
    var row = diagnostic.closest('tr.event-row');
    panel.insertBefore(diagnostic, before);
    if (row && !row.querySelector('details.phase-diagnostics')) row.remove();
  });
  if (!diagnostics.length) return;
  q('table.table', body).forEach(function (table) {
    if (!table.querySelector('tbody > tr')) table.remove();
  });
  if (!String(body.textContent || '').trim() && !body.querySelector('img,details,table'))
    body.remove();
}
