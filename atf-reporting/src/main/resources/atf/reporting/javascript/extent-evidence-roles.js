function assignNodeRoles() {
  var cards = q(
    '.test-contents .card-title a.node,.atf-failure-test-detail .card-title a.node'
  );
  cards.forEach(function (node) {
    var title = text(node, ''),
      card = node.closest('.card');
    if (!card || isScenarioCard(card)) return;
    var upper = title.toUpperCase(),
      role = '';
    if (upper === 'SETUP' || upper === 'TEST BODY' || upper === 'TEARDOWN')
      role = 'phase';
    else if (
      /^GET |^POST |^PUT |^PATCH |^DELETE |^HEAD /.test(upper) ||
      /^(GIVEN|WHEN|THEN|AND|BUT)\s+/.test(upper) ||
      /^TRACE\s+/.test(upper) ||
      /NETWORK/.test(upper)
    )
      role = 'request';
    else if (/^VALIDATE[: ]/.test(upper)||upper.indexOf('VALIDATION')>=0||upper==='RUNTIME DIAGNOSTIC')
      role = 'validation';
    if (role) card.setAttribute('data-atf-node-role', role);
    if (role === 'phase')
      card.setAttribute('data-atf-phase-name', upper.toLowerCase().replace(/\s+/g, '-'));
    if (card.querySelector('.helm-trace-table'))
      card.setAttribute('data-atf-evidence-kind', 'trace');
    if (card.querySelector('pre.code-block,textarea.code-block'))
      card.setAttribute('data-atf-evidence-kind', 'json');
    if (card.querySelector('.helm-validation-table'))
      card.setAttribute('data-atf-evidence-kind', 'assertions');
    var evidenceType = technicalEvidenceType(card);
    if (evidenceType) {
      card.setAttribute('data-atf-evidence-type', evidenceType);
      if (evidenceType === 'runtime-diagnostic' || evidenceType === 'api-trace')
        card.setAttribute('data-atf-evidence-family', 'technical');
    }
  });
  q('.card[data-atf-evidence-family="technical"]').forEach(function (technical) {
    technical.querySelectorAll('.card-title a.node').forEach(function (node) {
      var child = node.closest('.card');
      if (child) child.setAttribute('data-atf-evidence-family', 'technical');
    });
  });
}
function technicalEvidenceType(card) {
  if (!card) return '';
  var table = card.querySelector(
    ':scope>.collapse>.card-body>.table .helm-trace-table'
  );
  if (!table) return '';
  var rows = table.querySelectorAll('tbody>tr');
  for (var i = 0; i < rows.length; i++) {
    var cells = rows[i].querySelectorAll('td');
    if (cells.length < 2) continue;
    if (text(cells[0], '').trim().toUpperCase() === 'EVIDENCE TYPE')
      return text(cells[1], '').trim().toLowerCase();
  }
  return '';
}
function nodeEventTable(card) {
  if (!card) return null;
  var panel =
    card.querySelector(':scope>.collapse') ||
    card.querySelector(':scope>.show') ||
    card.querySelector(':scope>.accordion');
  return (
    (panel && panel.querySelector('table.table')) ||
    card.querySelector('table.table')
  );
}
function directEvidenceTable(card) {
  return (
    card.querySelector(':scope>.card-body>.table') ||
    card.querySelector(':scope>.collapse>.card-body>.table') ||
    card.querySelector(':scope>.show>.card-body>.table') ||
    card.querySelector(
      ':scope>div:not(.card-header):not(.accordion)>.card-body>.table'
    )
  );
}
