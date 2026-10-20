function failureTransportEvidence(card, state) {
  var trace = card.querySelector('.helm-trace-table'),
    execution = card.querySelector('.execution-table');
  [trace, execution]
    .filter(function (table) {
      return !!table;
    })
    .forEach(function (evidence) {
      var evidenceRows =
        evidence.tBodies && evidence.tBodies[0]
          ? Array.prototype.slice.call(evidence.tBodies[0].rows)
          : [];
      evidenceRows.forEach(function (evidenceRow) {
        var evidenceCells = Array.prototype.slice.call(evidenceRow.cells || []),
          evidenceLabel = String(text(evidenceCells[0], '')).toUpperCase();
        if (evidenceLabel === 'FAILURE CAUSE')
          state.message = text(evidenceCells[1], '');
        if (
          (evidenceLabel === 'RESPONSE DURATION' ||
            evidenceLabel === 'RESPONSE DURATION (MS)' ||
            evidenceLabel === 'RESPONSE.DURATION.MS') &&
          !state.duration
        ) {
          var rawDuration = text(evidenceCells[1], '');
          var durationMs = atfDurationValue(rawDuration);
          state.duration =
            durationMs === null ? rawDuration : atfFormatDuration(durationMs);
        }
      });
    });
}
function failureValidationCandidate(card) {
  var table = card.querySelector('.helm-validation-table');
  if (!table) return null;
  var body = table.tBodies && table.tBodies[0],
    rows = body ? Array.prototype.slice.call(body.rows) : [],
    row =
      rows.find(function (candidate) {
        var cells = Array.prototype.slice.call(candidate.cells || []);
        return text(cells[3], '').indexOf('❌') >= 0;
      }) || rows[0];
  if (!row) return null;
  var cells = Array.prototype.slice.call(row.cells || []);
  return {
    card: card,
    label: text(cells[0], ''),
    expected: text(cells[1], ''),
    actual: text(cells[2], ''),
    result: text(cells[3], '')
  };
}
function collectFailureState(item) {
  var state = {
    phase: '',
    phaseCard: null,
    requests: [],
    validations: [],
    uiFailures: [],
    message: '',
    duration: ''
  };
  q('.card', item).forEach(function (card) {
    var title = text(card, '.card-title a.node'),
      upper = title.toUpperCase();
    if (
      !state.phase &&
      /^(SETUP|TEST BODY|TEARDOWN)$/.test(upper) &&
      statusOf(card) === 'fail'
    ) {
      state.phase = title;
      state.phaseCard = card;
    }
    if (/^(GET|POST|PUT|PATCH|DELETE|HEAD) /.test(title))
      state.requests.push({ card: card, title: title });
    var setupFailure = failedSetupEvidence(card),
      uiFailure = failedUiEvidence(card);
    if (setupFailure) state.uiFailures.push(setupFailure);
    if (uiFailure) state.uiFailures.push(uiFailure);
    failureTransportEvidence(card, state);
    var candidate = failureValidationCandidate(card);
    if (candidate) state.validations.push(candidate);
  });
  return state;
}
function failureEntryBelongsToPhase(entry, phaseCard) {
  return (
    !phaseCard ||
    !!(
      phaseCard.compareDocumentPosition(entry.card) &
      Node.DOCUMENT_POSITION_PRECEDING
    )
  );
}
function latestFailureEntry(entries, phaseCard) {
  var scoped = entries.filter(function (entry) {
    return failureEntryBelongsToPhase(entry, phaseCard);
  });
  return scoped.length ? scoped[scoped.length - 1] : entries[0] || null;
}
function failureContextFromState(state) {
  var request = '',
    validation = '',
    assertion = '',
    message = state.message,
    fallback = null;
  if (state.phase !== 'TEARDOWN') {
    var requestEntry = latestFailureEntry(state.requests, state.phaseCard);
    request = requestEntry ? requestEntry.title : '';
    fallback = latestFailureEntry(state.validations, state.phaseCard);
    if (fallback) {
      validation = /status/i.test(fallback.label)
        ? 'Validate: status=' + fallback.expected
        : fallback.label;
      assertion =
        fallback.label +
        ' → ' +
        statusDisplay(fallback.expected) +
        ' → ' +
        statusDisplay(fallback.actual);
      if (
        !message &&
        ((fallback.result || '').indexOf('❌') >= 0 ||
          state.phase === 'TEST BODY')
      )
        message =
          fallback.label +
          ' expected ' +
          statusDisplay(fallback.expected) +
          ', actual ' +
          statusDisplay(fallback.actual);
    }
  }
  var context = {
    phase: state.phase || 'Unknown phase',
    ui: false,
    request: request,
    duration: state.duration,
    step: '',
    validation: validation || 'No contract validation recorded',
    assertion:
      assertion || 'Failure recorded; see exact test for full evidence',
    message: message || 'Failure recorded; see exact test for full evidence'
  };
  if (state.uiFailures.length) {
    var concrete = state.uiFailures[0];
    context.ui = true;
    context.step = concrete.step || 'UI step';
    context.request = '';
    context.duration = '';
    context.validation = concrete.validation;
    context.assertion = concrete.assertion;
    context.message = concrete.message;
  }
  return context;
}
function failureContext(item) {
  var context = failureContextFromState(collectFailureState(item));
  if (typeof scopeFailureContextToPhase === 'function')
    context = scopeFailureContextToPhase(item, context);
  if (typeof failureCauseChain === 'function') {
    var chain = failureCauseChain(item);
    if (chain) context.message = chain;
  }
  return context;
}
function failureContextFor(item, anchor) {
  var ctx = failureContext(item);
  if (!anchor) return ctx;
  var cells = Array.prototype.slice
      .call(anchor.cells || [])
      .map(function (cell) {
        return text(cell, '');
      }),
    label = cells[0] || 'Assertion',
    expected = cells[1] || '',
    actual = cells[2] || '';
  var validation = anchor.closest('.card');
  if (validation) {
    var title = text(validation, '.card-title a.node');
    if (title) ctx.validation = title;
  }
  ctx.assertion =
    label + ' → ' + statusDisplay(expected) + ' → ' + statusDisplay(actual);
  if (!ctx.message || ctx.message.indexOf('PRIMARY:') < 0)
    ctx.message =
      label +
      ' expected ' +
      statusDisplay(expected) +
      ', actual ' +
      statusDisplay(actual);
  return ctx;
}
