var atfBaseActivate = window.atfActivate;
window.atfActivate = function (id) {
  rememberMeasuredDurations();
  atfBaseActivate(id);
  aggregateClassDurations();
  scheduleDurationAggregation();
  scheduleDurationAggregationAt(150);
  scheduleDurationAggregationAt(500);
};
document.addEventListener('DOMContentLoaded', function () {
  scheduleDurationAggregation();
});
window.addEventListener('load', function () {
  scheduleDurationAggregation();
});
function bindDurationClassClicks() {
  classItems().forEach(function (item) {
    if (item.getAttribute('data-atf-duration-click-bound')) return;
    item.setAttribute('data-atf-duration-click-bound', 'true');
    item.addEventListener(
      'click',
      scheduleDurationAggregationAfterClassClick,
      true
    );
  });
}
function hasRelevantDurationNodes(r) {
  var s = '.test-item,.test-detail,[data-duration-ms],[data-duration]';
  return r.some(function (x) {
    return [].some.call(x.addedNodes || [], function (n) {
      return n && ((n.matches && n.matches(s)) ||
        (n.querySelector && n.querySelector(s)));
    });
  });
}
function watchDurationNodes() {
  var root = document.getElementById('test-view');
  if (!root) return;
  bindDurationClassClicks();
  if (root.getAttribute('data-atf-duration-watch')) return;
  if (window.MutationObserver) {
    root.setAttribute('data-atf-duration-watch', 'true');
    var observer = new MutationObserver(function (records) {
      if (!hasRelevantDurationNodes(records)) return;
      atfPerformanceCount('durationMutationBatches');
      bindDurationClassClicks();
      scheduleDurationAggregation();
    });
    observer.observe(root, { childList: true, subtree: true });
  }
}
