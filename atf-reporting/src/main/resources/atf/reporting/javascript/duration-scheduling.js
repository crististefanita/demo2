var atfDurationAggregationPending = false;
function scheduleDurationAggregation() {
  if (atfDurationAggregationPending) return;
  atfDurationAggregationPending = true;
  setTimeout(function () {
    atfDurationAggregationPending = false;
    aggregateClassDurations();
  }, 0);
}
function scheduleDurationAggregationAfterClassClick() {
  scheduleDurationAggregation();
  setTimeout(aggregateClassDurations, 100);
  setTimeout(aggregateClassDurations, 500);
}
