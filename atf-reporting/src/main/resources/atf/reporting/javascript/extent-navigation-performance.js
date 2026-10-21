var atfReportPerformanceState = window.__atfReportPerformance || {
  normalizationSchedules: 0,
  normalizationRuns: 0,
  normalizationDurationMs: 0,
  relevantMutationBatches: 0,
  durationMutationBatches: 0,
  scenarioToggles: 0,
  phaseToggles: 0,
  nodeToggles: 0,
  readinessStartedAt: null,
  readinessCompletedAt: null,
  readinessMs: null,
  longTaskObservationSupported: false,
  longTaskCount: 0,
  longTaskDurationMs: 0
};
window.__atfReportPerformance = atfReportPerformanceState;
function atfPerformanceNow() {
  return window.performance && typeof window.performance.now === 'function'
    ? window.performance.now() : Date.now();
}
function atfPerformanceCount(name) {
  atfReportPerformanceState[name] =
    Number(atfReportPerformanceState[name] || 0) + 1;
}
function atfPerformanceStart() {
  return atfPerformanceNow();
}
function atfPerformanceEnd(start, target) {
  if (start == null || !target) return;
  atfReportPerformanceState[target] =
    Number(atfReportPerformanceState[target] || 0) +
    Math.max(0, atfPerformanceNow() - start);
}
function atfPerformanceBeginReadiness() {
  atfReportPerformanceState.readinessStartedAt = atfPerformanceNow();
  atfReportPerformanceState.readinessCompletedAt = null;
  atfReportPerformanceState.readinessMs = null;
  document.documentElement.removeAttribute('data-atf-test-view-ready');
}
function atfPerformanceMarkReady() {
  if (atfReportPerformanceState.readinessStartedAt == null ||
      atfReportPerformanceState.readinessCompletedAt != null) return;
  atfReportPerformanceState.readinessCompletedAt = atfPerformanceNow();
  atfReportPerformanceState.readinessMs =
    Math.max(0, atfReportPerformanceState.readinessCompletedAt -
      atfReportPerformanceState.readinessStartedAt);
  document.documentElement.setAttribute('data-atf-test-view-ready', 'true');
}
function atfPerformanceSnapshot() {
  return Object.assign({}, atfReportPerformanceState);
}
(function observeLongTasks() {
  if (!window.PerformanceObserver ||
      !window.PerformanceObserver.supportedEntryTypes ||
      window.PerformanceObserver.supportedEntryTypes.indexOf('longtask') < 0) return;
  try {
    var observer = new PerformanceObserver(function (list) {
      list.getEntries().forEach(function (entry) {
        atfReportPerformanceState.longTaskCount++;
        atfReportPerformanceState.longTaskDurationMs += Number(entry.duration || 0);
      });
    });
    observer.observe({ entryTypes: ['longtask'] });
    atfReportPerformanceState.longTaskObservationSupported = true;
    window.__atfReportPerformanceObserver = observer;
  } catch (ignored) {}
})();
