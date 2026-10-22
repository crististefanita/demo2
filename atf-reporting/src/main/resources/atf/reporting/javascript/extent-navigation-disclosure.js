function openScenarioCard(card) {
  if (!card) return;
  var panel = card.querySelector(':scope>.accordion');
  if (!panel) return;
  collapseDirectScenarioCards();
  card.setAttribute('data-atf-selected', 'true');
  setDisclosureState(card, panel, true, 'data-atf-scenario-open');
  normalizeCodeBlocks();
  assignNodeRoles();
  aggregateClassDurations();
  normalizeEvidenceDisclosures();
  bindPhaseCollapse();
  bindNodeCollapse();
  if (card.scrollIntoView) card.scrollIntoView({ block: 'center' });
}
function setDisclosureState(card, panel, open, attribute) {
  if (!panel) return;
  var state = String(!!open),
    header = card.firstElementChild,
    node = card.querySelector(':scope>.card-header .card-title .node');
  card.setAttribute(attribute, state);
  if (header) header.setAttribute('aria-expanded', state);
  if (node) node.classList.toggle('collapsed', !open);
  var panels = attribute === 'data-atf-node-open' ? nodePanels(card) : [panel];
  panels.forEach(function (child) {
    child.classList.toggle('show', open);
    child.style.setProperty('display', open ? 'block' : 'none', 'important');
  });
}
function bindDisclosureKeyboard(header, toggle) {
  if (!header.hasAttribute('tabindex')) header.setAttribute('tabindex', '0');
  if (!header.hasAttribute('role')) header.setAttribute('role', 'button');
  header.addEventListener('keydown', function (e) {
    if ((e.key === 'Enter' || e.key === ' ') && e.target === header) toggle(e);
  }, true);
}
function openDeepLinkCard(card, open) {
  if (!card || card.getAttribute('data-atf-node-open') === String(open)) return;
  var header = card.querySelector(':scope>.card-header');
  if (header) header.click();
}
function openScenarioAuditPath(card) {
  if (!card) return;
  var testBody = card.querySelector(
      ':scope>.accordion>[data-atf-node-role="phase"][data-atf-phase-name="test-body"]'
    );
  if (!testBody) return;
  var phaseNode = testBody.querySelector(':scope>.card-header .card-title .node');
  if (testBody.getAttribute('data-atf-phase-open') !== 'true' && phaseNode)
    phaseNode.click();
  bindPhaseCollapse();
  var thenNode = testBody.querySelector(
      ':scope>.accordion>[data-atf-node-role="request"]'
    );
  openDeepLinkCard(thenNode, true);
  var validation = thenNode && thenNode.querySelector(
      ':scope>.accordion>[data-atf-node-role="validation"]'
    );
  openDeepLinkCard(validation, true);
  if (validation) {
    validation.querySelectorAll(':scope details').forEach(function (detail) {
      detail.open = true;
    });
    if (validation.scrollIntoView)
      validation.scrollIntoView({ block: 'center' });
  }
}
function collapseDirectScenarioCards() {
  q('.test-content-detail .test-contents > .mt-4 > .accordion > .card').forEach(
    function (card) {
      var panel = card.querySelector(':scope > .accordion');
      if (!panel || card.getAttribute('data-atf-scenario-open') === 'true')
        return;
      setDisclosureState(card, panel, false, 'data-atf-scenario-open');
    }
  );
}
function collapseScenarioCards(force) {
  q('.test-content-detail .test-contents > .mt-4 > .accordion > .card').forEach(
    function (scenario) {
      scenario.querySelectorAll(':scope [data-atf-node-role]').forEach(function (card) {
        var role = card.getAttribute('data-atf-node-role'),
          panel = role === 'phase'
            ? card.querySelector(':scope>.accordion')
            : nodePanel(card),
          attribute = role === 'phase'
            ? 'data-atf-phase-open' : 'data-atf-node-open';
        if (!panel) return;
        if (!force && card.getAttribute(attribute) === 'true') return;
        setDisclosureState(card, panel, false, attribute);
        if (role === 'phase') applyPhaseBodyState(card, false);
      });
    }
  );
}
function toggleScenario(card, panel, e) {
  if (e) {
    e.preventDefault();
    e.stopPropagation();
    if (e.stopImmediatePropagation) e.stopImmediatePropagation();
  }
  var startedAt = atfPerformanceStart();
  normalizeScenarioAnnotations();
  var open = card.getAttribute('data-atf-scenario-open') === 'true',
    next = !open;
  atfPerformanceCount('scenarioToggles');
  setDisclosureState(card, panel, next, 'data-atf-scenario-open');
  if (next) {
    assignNodeRoles();
    aggregateClassDurations();
    normalizeEvidenceDisclosures();
    bindPhaseCollapse();
    bindNodeCollapse();
  }
  collapseScenarioCards(true);
  atfPerformanceRecordDisclosureToggle(startedAt);
}
function initializeScenarioCard(card, panel) {
  if (card.getAttribute('data-atf-scenario-open') !== null) return;
  setDisclosureState(card, panel, false, 'data-atf-scenario-open');
}
function bindScenarioCollapse() {
  q('#test-view .test-list .test-item').forEach(function (item) {
    if (item.getAttribute('data-atf-collapse-bound')) return;
    item.setAttribute('data-atf-collapse-bound', 'true');
    item.addEventListener('click', function () {
      setTimeout(function () {
        normalizeScenarioAnnotations();
        q(
          '.test-content-detail .test-contents > .mt-4 > .accordion > .card'
        ).forEach(function (card) {
          var panel = card.querySelector(':scope > .accordion');
          if (panel) initializeScenarioCard(card, panel);
        });
        collapseScenarioCards();
      }, 0);
    });
  });
  q(
    '#test-view .test-content-detail .test-contents > .mt-4 > .accordion > .card'
  ).forEach(function (card) {
    var header = card.querySelector(':scope > .card-header'),
      panel = card.querySelector(':scope > .accordion'),
      node = header && header.querySelector(':scope > .card-title .node');
    if (!header || !panel) return;
    initializeScenarioCard(card, panel);
    if (header.getAttribute('data-atf-scenario-collapse-bound')) return;
    header.setAttribute('data-atf-scenario-collapse-bound', 'true');
    var toggle = function (e) {
      toggleScenario(card, panel, e);
    };
    if (node && !node.getAttribute('data-atf-scenario-node-collapse-bound')) {
      node.setAttribute('data-atf-scenario-node-collapse-bound', 'true');
      node.addEventListener('click', toggle, true);
    }
    header.addEventListener('click', function (e) {
      if (node && e.target && node.contains(e.target)) return;
      toggle(e);
    });
    bindDisclosureKeyboard(header, toggle);
  });
  q('#test-view .test-content-detail [data-atf-node-role="phase"]').forEach(
    function (card) {
      var node = card.querySelector(':scope > .card-header .card-title .node'),
        panel = card.querySelector(':scope > .accordion');
      if (!node || !panel || node.getAttribute('data-atf-phase-collapse-bound'))
        return;
      node.setAttribute('data-atf-phase-collapse-bound', 'true');
      node.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        var open = card.getAttribute('data-atf-phase-open') === 'true',
          next = !open;
        atfPerformanceCount('phaseToggles');
        setDisclosureState(card, panel, next, 'data-atf-phase-open');
        applyPhaseBodyState(card, next);
      });
    }
  );
}
function hasRelevantScenarioNodes(r) {
  var s = '.test-contents > .mt-4 > .accordion > .card,[data-atf-node-role]';
  return r.some(function (x) {
    return [].some.call(x.addedNodes || [], function (n) {
      return n && ((n.matches && n.matches(s)) ||
        (n.querySelector && n.querySelector(s)));
    });
  });
}
function watchScenarioCards() {
  var root = document.getElementById('test-view');
  if (!root || root.getAttribute('data-atf-scenario-watch')) return;
  if (window.MutationObserver) {
    root.setAttribute('data-atf-scenario-watch', 'true');
    var observer = new MutationObserver(function (records) {
      if (hasRelevantScenarioNodes(records)) {
        atfPerformanceCount('relevantMutationBatches');
        bindScenarioCollapse();
        scheduleTestViewNormalization();
      }
    });
    observer.observe(root, { childList: true, subtree: true });
  }
}
function phaseDirectBody(card) {
  return (
    card &&
    (card.querySelector(':scope>.card-body') ||
      card.querySelector(':scope>div:not(.card-header):not(.accordion)'))
  );
}
function applyPhaseBodyState(card, open) {
  var body = phaseDirectBody(card);
  if (body)
    body.style.setProperty('display', open ? 'block' : 'none', 'important');
}
function bindPhaseCollapse() {
  q('#test-view .test-content-detail [data-atf-node-role="phase"]').forEach(
    function (card) {
      var header = card.querySelector(':scope>.card-header'),
        panel = card.querySelector(':scope>.accordion');
      if (!header || !panel) return;
      var open = card.getAttribute('data-atf-phase-open') === 'true';
      setDisclosureState(card, panel, open, 'data-atf-phase-open');
      applyPhaseBodyState(card, open);
      if (card.getAttribute('data-atf-phase-header-bound')) return;
      card.setAttribute('data-atf-phase-header-bound', 'true');
      var toggle = function (e) {
        if (e) {
          e.preventDefault();
          e.stopImmediatePropagation();
          e.stopPropagation();
        }
        var startedAt = atfPerformanceStart(),
          next = card.getAttribute('data-atf-phase-open') !== 'true';
        atfPerformanceCount('phaseToggles');
        setDisclosureState(card, panel, next, 'data-atf-phase-open');
        applyPhaseBodyState(card, next);
        atfPerformanceRecordDisclosureToggle(startedAt);
      };
      header.addEventListener('click', toggle, true);
      bindDisclosureKeyboard(header, toggle);
    }
  );
}
function nodePanel(card) {
  return nodePanels(card)[0] || null;
}
function nodePanels(card) {
  var header = card.querySelector(':scope>.card-header');
  return [].filter.call(card.children, function (child) {
    return child !== header;
  });
}
function bindNodeCollapse() {
  q(
    '#test-view .test-content-detail [data-atf-node-role="request"],#test-view .test-content-detail [data-atf-node-role="validation"]'
  ).forEach(function (card) {
    var header = card.querySelector(':scope>.card-header'),
      panel = nodePanel(card);
    if (!header || !panel) return;
    var open = card.getAttribute('data-atf-node-open') === 'true';
    setDisclosureState(card, panel, open, 'data-atf-node-open');
    if (header.getAttribute('data-atf-node-collapse-bound')) return;
    header.setAttribute('data-atf-node-collapse-bound', 'true');
    var toggle = function (e) {
      if (e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        e.stopPropagation();
      }
      var open = card.getAttribute('data-atf-node-open') === 'true',
        startedAt = atfPerformanceStart(),
        next = !open;
      atfPerformanceCount('nodeToggles');
      setDisclosureState(card, panel, next, 'data-atf-node-open');
      atfPerformanceRecordDisclosureToggle(startedAt);
    };
    header.addEventListener('click', toggle, true);
    bindDisclosureKeyboard(header, toggle);
  });
}
