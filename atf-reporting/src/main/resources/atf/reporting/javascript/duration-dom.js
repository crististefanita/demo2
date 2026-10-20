function directCardHeader(card) {
  if (!card || !card.children) return null;
  for (var i = 0; i < card.children.length; i++) {
    if (
      card.children[i].classList &&
      card.children[i].classList.contains('card-header')
    )
      return card.children[i];
  }
  return null;
}
function directNode(card) {
  var header = directCardHeader(card);
  return header && header.querySelector('.card-title a.node');
}
function durationBadge(card) {
  var header = directCardHeader(card);
  if (!header) return null;
  return (
    header.querySelector('.node-info .badge-default') ||
    header.querySelector('.card-toolbar .badge-default') ||
    header.querySelector('.card-toolbar .badge')
  );
}
function nodeLabel(card) {
  var node = directNode(card);
  return String((node && node.textContent) || '')
    .replace(/\s+/g, ' ')
    .trim();
}
function nodePathKey(card) {
  var labels = [],
    current = card;
  while (current) {
    var label = nodeLabel(current);
    if (label) labels.unshift(label);
    current = current.parentElement
      ? current.parentElement.closest('.card')
      : null;
  }
  return labels.join(' > ');
}
function nodeIdentity(card) {
  var node = directNode(card);
  return node && node.id ? String(node.id) : '';
}
function durationChildVisible(card) {
  return (
    String(card.getAttribute('data-atf-filter-hidden') || 'false') !== 'true'
  );
}
function isStructuralNode(card) {
  if (!card) return false;
  if (
    card.getAttribute('data-atf-node-role') ||
    card.getAttribute('data-atf-scenario-id')
  )
    return true;
  var header = directCardHeader(card),
    title = String(
      text(header && header.querySelector('.card-title a.node')) || ''
    )
      .trim()
      .toUpperCase();
  return (
    title === 'SETUP' ||
    title === 'TEST BODY' ||
    title === 'TEARDOWN' ||
    /^(GET|POST|PUT|PATCH|DELETE|HEAD) /.test(title) ||
    /^(VALIDATE[: ]|.*VALIDATION)/.test(title) ||
    /^(GIVEN|WHEN|THEN|AND|BUT)\s+/.test(title)
  );
}
function directStructuralChildren(card) {
  var accordion =
    card && card.children
      ? Array.prototype.slice.call(card.children).find(function (child) {
          return child.classList && child.classList.contains('accordion');
        })
      : null;
  return accordion
    ? Array.prototype.slice
        .call(accordion.children)
        .filter(function (child) {
          return child.classList && child.classList.contains('card');
        })
        .filter(isStructuralNode)
        .filter(durationChildVisible)
    : [];
}
