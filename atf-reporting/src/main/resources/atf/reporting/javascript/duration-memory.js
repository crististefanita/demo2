var atfMeasuredDurationsByNodeId = Object.create(null);
var atfMeasuredDurationsByPath = Object.create(null);
function rememberMeasuredDurations() {
  q('#test-view .test-list-item .test-contents .card > .card-header').forEach(
    function (header) {
      var card = header.parentElement;
      if (!card) return;
      var badge =
          header.querySelector('.node-info .badge-default') ||
          header.querySelector('.card-toolbar .badge-default') ||
          header.querySelector('.card-toolbar .badge'),
        value = atfDurationValue(text(badge, ''));
      if (value === null) return;
      var node = directNode(card),
        id = nodeIdentity(card),
        path = nodePathKey(card);
      if (id && atfMeasuredDurationsByNodeId[id] === undefined)
        atfMeasuredDurationsByNodeId[id] = value;
      if (path && atfMeasuredDurationsByPath[path] === undefined)
        atfMeasuredDurationsByPath[path] = value;
      if (node && node.getAttribute('data-atf-measured-duration-ms') === null)
        node.setAttribute('data-atf-measured-duration-ms', String(value));
      if (card.getAttribute('data-atf-measured-duration-ms') === null)
        card.setAttribute('data-atf-measured-duration-ms', String(value));
      if (badge.getAttribute('data-atf-measured-duration-ms') !== String(value))
        badge.setAttribute('data-atf-measured-duration-ms', String(value));
    }
  );
}
function measuredNodeDuration(card) {
  var node = directNode(card),
    nodeMarked = node && node.getAttribute('data-atf-measured-duration-ms');
  if (nodeMarked !== null && nodeMarked !== '') return Number(nodeMarked);
  var remembered = card && card.getAttribute('data-atf-measured-duration-ms');
  if (remembered !== null && remembered !== '') return Number(remembered);
  var id = nodeIdentity(card),
    idValue = id ? atfMeasuredDurationsByNodeId[id] : undefined,
    path = nodePathKey(card),
    pathValue = path ? atfMeasuredDurationsByPath[path] : undefined;
  if (idValue !== undefined) return Number(idValue);
  if (pathValue !== undefined) return Number(pathValue);
  var badge = durationBadge(card),
    marked = badge && badge.getAttribute('data-atf-measured-duration-ms'),
    value =
      marked !== null ? Number(marked) : atfDurationValue(text(badge, ''));
  if (card && value !== null)
    card.setAttribute('data-atf-measured-duration-ms', String(value));
  return value;
}
