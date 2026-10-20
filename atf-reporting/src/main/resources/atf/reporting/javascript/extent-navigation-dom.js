function normalizeCodeBlocks() {
  q('textarea.code-block').forEach(function (area) {
    var pre = document.createElement('pre');
    pre.className = 'code-block';
    pre.textContent = area.value;
    area.parentNode.replaceChild(pre, area);
  });
}
function normalizeDisclosureState() {
  q(
    '.test-view .test-content-detail .card[data-atf-scenario-id],.test-view .test-content-detail .card[data-atf-node-role]'
  ).forEach(function (card) {
    var header = card.querySelector(':scope>.card-header'),
      panel =
        card.querySelector(':scope>.accordion') ||
        card.querySelector(':scope>.collapse');
    if (!header) return;
    var open =
      card.getAttribute('data-atf-scenario-open') === 'true' ||
      card.getAttribute('data-atf-phase-open') === 'true' ||
      card.getAttribute('data-atf-node-open') === 'true' ||
      !!(panel && panel.classList.contains('show'));
    header.setAttribute('role', 'button');
    header.setAttribute('tabindex', '0');
    header.setAttribute('aria-expanded', String(open));
  });
  if (window.__atfDisclosureStateBound) return;
  window.__atfDisclosureStateBound = true;
  document.addEventListener(
    'click',
    function () {
      setTimeout(normalizeDisclosureState, 0);
    },
    true
  );
  document.addEventListener(
    'keydown',
    function () {
      setTimeout(normalizeDisclosureState, 0);
    },
    true
  );
}
setTimeout(function () {
  normalizeDisclosureState();
}, 0);
function captureDomContract() {
  assignNodeRoles();
  var requests = q('[data-atf-node-role="request"]'),
    validations = q('[data-atf-node-role="validation"]');
  var violations = validations.filter(function (v) {
    return !v.closest('[data-atf-node-role="request"]');
  }).length;
  window.__atfDomContract = {
    tests: testItems().length,
    phases: q('[data-atf-node-role="phase"]').length,
    requests: requests.length,
    validations: validations.length,
    traceTables: q('[data-atf-evidence-kind="trace"] .helm-trace-table').length,
    jsonBlocks: q('[data-atf-evidence-kind="json"] pre.code-block').length,
    assertionTables: q('.helm-validation-table').length,
    categoryLinks: q('.category-view a[data-atf-test-id]').length,
    violations: violations,
    openEvidence: q('.collapse.show').length
  };
}
