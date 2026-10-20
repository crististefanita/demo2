function disclosureSummary(title) {
  return (
    '<span class="atf-disclosure-glyph" aria-hidden="true"></span><span class="se-title" title="' +
    esc(title) +
    '" aria-label="' +
    esc(title) +
    '">' +
    esc(title) +
    '</span>'
  );
}
function wrapEvidence(row, kind, title, node) {
  var cell = row && row.cells && row.cells[2];
  if (!cell || !node) return;
  var details = document.createElement('details');
  details.className = 'step-detail ' + kind;
  var summary = document.createElement('summary');
  summary.setAttribute('aria-expanded', 'false');
  summary.innerHTML = disclosureSummary(title);
  details.addEventListener('toggle', function () {
    summary.setAttribute('aria-expanded', details.open ? 'true' : 'false');
  });
  var body = document.createElement('div');
  body.className = 'body';
  if (kind === 'response-evidence') {
    var classification = document.createElement('div');
    classification.className = 'helm-api-classification technical';
    var classificationLabel = document.createElement('span');
    classificationLabel.className = 'helm-api-classification-label';
    classificationLabel.textContent = 'TECHNICAL TRACE';
    classification.appendChild(classificationLabel);
    body.appendChild(classification);
  }
  body.appendChild(node);
  details.appendChild(summary);
  details.appendChild(body);
  cell.innerHTML = '';
  cell.appendChild(details);
}
function normalizeEvidenceDisclosures() {
  q('[data-atf-node-role="request"],[data-atf-node-role="validation"]').forEach(
    function (card) {
      var table = nodeEventTable(card);
      if (!table) return;
      var rows = q(':scope>tbody>tr.event-row', table);
      for (var i = 0; i < rows.length; i++) {
        var row = rows[i],
          next = rows[i + 1],
          label = text(row.cells[2], '').trim();
        // The Java renderer already emits semantic evidence disclosures.
        // Keep this normalizer safe for both native Extent rows and the
        // renderer's canonical form; never replace a disclosure that owns
        // request/response JSON with a neighbouring trace row.
        if (row.querySelector('details')) continue;
        if (next && /^Execution evidence/.test(label)) {
          var executionBody = document.createElement('div');
          executionBody.className = 'execution-evidence-body';
          var executionTable = next.querySelector('.execution-table');
          if (executionTable) executionBody.appendChild(executionTable);
          var retryRow = rows[i + 2],
            retryTable =
              retryRow && retryRow.querySelector('.retry-history-table');
          if (retryTable) {
            executionBody.appendChild(retryTable);
            retryRow.remove();
          }
          if (executionBody.children.length) {
            wrapEvidence(row, 'execution-evidence', label, executionBody);
            next.remove();
            i--;
            continue;
          }
        }
        if (
          next &&
          (label.indexOf('TECHNICAL TRACE') === 0 ||
            label.indexOf('Response details') === 0)
        ) {
          var trace = next.querySelector('.helm-trace-table');
          if (trace) {
            var fields =
              trace.tBodies && trace.tBodies[0]
                ? trace.tBodies[0].rows.length
                : 0;
            wrapEvidence(
              row,
              'response-evidence',
              'Response details · ' + fields + ' fields',
              trace
            );
            next.remove();
            i--;
            continue;
          }
        }
        if (next && /^(REQUEST|RESPONSE) JSON$/.test(label)) {
          var pre = next.querySelector('pre.code-block');
          if (pre) {
            var scroll = document.createElement('div');
            scroll.className = 'helm-scroll-block';
            var heading = document.createElement('div');
            heading.className = 'helm-scroll-block-title';
            heading.textContent = label;
            var content = document.createElement('div');
            content.className = 'helm-scroll-block-body';
            content.appendChild(pre);
            scroll.appendChild(heading);
            scroll.appendChild(content);
            wrapEvidence(row, 'json-evidence', label, scroll);
            next.remove();
            i--;
            continue;
          }
        }
        var assertion = row.querySelector('.helm-validation-table'),
          validationTitle = text(card, '.card-title a.node').trim();
        if (
          assertion &&
          !assertion.closest('details') &&
          validationTitle !== 'Contract validation'
        ) {
          var count =
            assertion.tBodies && assertion.tBodies[0]
              ? assertion.tBodies[0].rows.length
              : 0;
          wrapEvidence(
            row,
            'assertion-evidence',
            'Assertions · ' + count,
            assertion
          );
        }
      }
    }
  );
  normalizePhaseFailureDisclosures();
}
function wrapSemanticEvidence(row, kind, title, node) {
  var cell = row && row.cells && row.cells[2];
  if (!cell || !node) return;
  var details = document.createElement('details');
  details.className = 'step-detail ' + kind;
  var summary = document.createElement('summary');
  summary.setAttribute('aria-expanded', 'false');
  summary.innerHTML = disclosureSummary(title);
  details.addEventListener('toggle', function () {
    summary.setAttribute('aria-expanded', details.open ? 'true' : 'false');
  });
  var body = document.createElement('div');
  body.className = 'body';
  body.appendChild(node);
  details.appendChild(summary);
  details.appendChild(body);
  cell.innerHTML = '';
  cell.appendChild(details);
}
function normalizeSemanticEvidenceDisclosures() {
  q('.helm-request-details-content,.helm-response-details-content').forEach(
    function (node) {
      if (node.closest('details')) return;
      var row = node.closest('tr.event-row'),
        table = row && row.closest('table.table'),
        summaryRow = row && row.previousElementSibling;
      if (
        !row ||
        !table ||
        !summaryRow ||
        !summaryRow.classList.contains('event-row')
      )
        return;
      var label = text(summaryRow.cells[2], '').trim(),
        kind = node.classList.contains('helm-request-details-content')
          ? 'request-evidence'
          : 'response-evidence';
      if (!/^Request details|^Response details/.test(label)) return;
      wrapSemanticEvidence(summaryRow, kind, label, node);
      row.remove();
    }
  );
}
function normalizePhaseFailureDisclosures() {
  q('[data-atf-node-role="phase"]').forEach(function (card) {
    var direct = phaseDirectBody(card),
      body = direct && (direct.querySelector(':scope>.card-body') || direct),
      table = body && body.querySelector(':scope>table.table');
    if (!table) return;
    var rows = q(':scope>tbody>tr.event-row', table);
    for (var i = 0; i < rows.length; i++) {
      var row = rows[i],
        next = rows[i + 1],
        label = text(row.cells[2], '').trim();
      if (
        !next ||
        row.querySelector('details') ||
        !next.querySelector('.helm-trace-table')
      )
        continue;
      var trace = next.querySelector('.helm-trace-table'),
        classification = next.querySelector('.helm-api-classification'),
        failureBody = document.createElement('div');
      failureBody.className = 'failure-evidence-body';
      if (classification) failureBody.appendChild(classification);
      failureBody.appendChild(trace);
      wrapEvidence(row, 'failure-evidence', 'Failure details', failureBody);
      next.remove();
      i--;
    }
  });
}
