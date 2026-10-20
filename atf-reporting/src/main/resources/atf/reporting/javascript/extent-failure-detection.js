function statusDisplay(value) {
  var key = String(value || '').trim(),
    map = {
      200: '200 OK',
      201: '201 Created',
      202: '202 Accepted',
      204: '204 No Content',
      400: '400 Bad Request',
      401: '401 Unauthorized',
      403: '403 Forbidden',
      404: '404 Not Found',
      409: '409 Conflict',
      500: '500 Internal Server Error'
    };
  return map[key] || key;
}
function failedSetupEvidence(card) {
  var zone = card.querySelector(
    '.atf-setup-evidence[data-atf-evidence-status="fail"]'
  );
  if (!zone) return null;
  var rows = q('tbody tr', zone),
    row = rows.find(function (candidate) {
      return String(candidate.textContent || '').indexOf('❌') >= 0;
    });
  if (!row) return null;
  var cells = Array.prototype.slice.call(row.cells || []).map(function (cell) {
    return text(cell, '');
  });
  return {
    ui: true,
    step: 'Setup',
    validation: 'Setup evidence',
    assertion:
      'Setup check · ' +
      (cells[0] || 'unknown') +
      ' → ' +
      (cells[1] || '') +
      ' → ' +
      (cells[2] || ''),
    message:
      'Setup check failed: ' +
      (cells[0] || 'unknown') +
      ' expected ' +
      (cells[1] || '') +
      ', actual ' +
      (cells[2] || '')
  };
}
function failedUiEvidence(card) {
  var zones = q(
    'details[data-atf-evidence-status="fail"], .atf-ui-assertions[data-atf-evidence-status="fail"], details.step-detail.assertion-evidence',
    card
  );
  for (var index = 0; index < zones.length; index++) {
    var zone = zones[index],
      rows = q('tbody tr', zone),
      row = rows.find(function (candidate) {
        return (
          !!candidate.querySelector('.atf-ui-action-result-fail') ||
          String(candidate.textContent || '').indexOf('❌') >= 0
        );
      });
    if (!row) continue;
    var cells = Array.prototype.slice
        .call(row.cells || [])
        .map(function (cell) {
          return text(cell, '');
        }),
      observation = zone.classList.contains('atf-ui-observations'),
      assertion =
        zone.classList.contains('atf-ui-assertions') ||
        zone.classList.contains('assertion-evidence'),
      label = observation
        ? 'Audit observation'
        : assertion
          ? 'UI assertion'
          : 'Playwright action';
    return {
      ui: true,
      step: label,
      validation: text(card, '.card-title a.node'),
      assertion:
        label +
        ' · ' +
        (cells[0] || 'unknown') +
        ' → ' +
        (cells[1] || '') +
        ' → ' +
        (cells[2] || '') +
        ' → ' +
        (cells[3] || ''),
      message:
        label + ' failed: ' + (cells[3] || 'see the concrete evidence row')
    };
  }
  return null;
}
function failureEvidenceCandidate(card) {
  var title = text(card, '.card-title a.node');
  if (title.toLowerCase() !== 'failure evidence') return null;
  var actionZone = card.querySelector(
      '.atf-ui-action-sequence[data-atf-evidence-status="fail"]'
    ),
    actionRow = actionZone
      ? q('tbody tr', actionZone).find(function (row) {
          return !!row.querySelector('.atf-ui-action-result-fail');
        })
      : null,
    assertionZone = card.querySelector(
      '.atf-ui-assertions[data-atf-evidence-status="fail"]'
    ),
    assertionRow = assertionZone
      ? q('tbody tr', assertionZone).find(function (row) {
          return String(row.textContent || '').indexOf('❌') >= 0;
        })
      : null,
    row = actionRow || assertionRow;
  if (!row) return null;
  var cells = Array.prototype.slice.call(row.cells || []).map(function (cell) {
    return text(cell, '');
  });
  if (actionRow) {
    var observed = cells[3] || 'see the concrete action evidence';
    return {
      ui: true,
      step: 'Failure evidence',
      validation: title,
      assertion:
        'Playwright action · ' +
        (cells[0] || 'unknown') +
        ' → ' +
        (cells[1] || '') +
        ' → ' +
        observed,
      message: 'Failure evidence: ' + observed
    };
  }
  return {
    ui: true,
    step: 'Failure evidence',
    validation: title,
    assertion:
      'Failure cause · ' +
      (cells[0] || 'unknown') +
      ' → ' +
      (cells[1] || '') +
      ' → ' +
      (cells[2] || '') +
      ' → ' +
      (cells[3] || ''),
    message:
      'Failure evidence: ' +
      (cells[2] || 'see the concrete failure evidence')
  };
}
function failedAssertionRows(item) {
  return q('.helm-validation-table tbody tr', item).filter(function (row) {
    var result = String(row.textContent || '').toLowerCase(),
      status = row.getAttribute('data-atf-assertion-status') || '';
    return (
      status === 'fail' ||
      result.indexOf('❌') >= 0 ||
      !!row.querySelector('.badge-danger')
    );
  });
}
