function scenarioCardFor(id, index) {
  var root = classItems().find(function (item) {
    return String(item.getAttribute('test-id') || '') === String(id || '');
  });
  if (!root) return null;
  var cards = directScenarioCards(root);
  return cards[Number(index) || 0] || cards[0] || null;
}
