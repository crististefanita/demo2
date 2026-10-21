var atfDurationAggregationPending = false;
var atfDurationAggregationTimers = {};
function scheduleDurationAggregation() {
  if (atfDurationAggregationPending) return;
  atfDurationAggregationPending = true;
  setTimeout(function () {
    atfDurationAggregationPending = false;
    aggregateClassDurations();
  }, 0);
}
function scheduleDurationAggregationAt(delay) {
  var key = String(delay);
  if (atfDurationAggregationTimers[key] != null) return;
  atfDurationAggregationTimers[key] = setTimeout(function () {
    atfDurationAggregationTimers[key] = null;
    scheduleDurationAggregation();
  }, delay);
}
function scheduleDurationAggregationAfterClassClick() {
  scheduleDurationAggregation();
  scheduleDurationAggregationAt(100);
  scheduleDurationAggregationAt(500);
}
