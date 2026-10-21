function countStatuses(items) {
  var result = { pass: 0, fail: 0, skip: 0, warning: 0 };
  items.forEach(function (item) {
    var status = statusOf(item);
    if (result[status] !== undefined) result[status]++;
  });
  return result;
}
function passLikeCount(statuses) {
  return statuses.pass + statuses.warning;
}
function updateOverviewSummaryFooter(card, statuses, noun) {
  if (!card) return;
  var footer = card.querySelectorAll('.card-footer small');
  if (!footer || footer.length < 2) return;
  var total = statuses.pass + statuses.fail + statuses.skip + statuses.warning,
    passLike = passLikeCount(statuses),
    percentage = total ? Math.round((passLike * 100) / total) : 0,
    failedPercentage = total ? Math.round((statuses.fail * 100) / total) : 0;
  footer[0].setAttribute('data-tooltip', percentage + '%');
  footer[0].innerHTML = '<b>' + passLike + '</b> ' + noun + ' passed';
  footer[1].setAttribute('data-tooltip', failedPercentage + '%');
  footer[1].innerHTML =
    '<b>' +
    statuses.fail +
    '</b> ' +
    noun +
    ' failed, <b>' +
    statuses.skip +
    '</b> skipped, <b data-tooltip="' +
    (total ? Math.round((statuses.warning * 100) / total) : 0) +
    '%">' +
    statuses.warning +
    '</b> warnings';
}
function overviewChartCard(id) {
  var chart = document.getElementById(id);
  return chart ? chart.closest('.card') : null;
}
function auditRows(id, data, share, total, keepOrder) {
  var body = document.querySelector('#' + id + ' tbody');
  if (!body) return;
  body.innerHTML = '';
  var keys = Object.keys(data);
  if (!keepOrder) keys.sort();
  keys.forEach(function (k) {
    var tr = document.createElement('tr'),
      cells = share
        ? [k, data[k], total ? Math.round((data[k] * 100) / total) + '%' : '0%']
        : [k, data[k]];
    tr.innerHTML = cells
      .map(function (v) {
        return '<td>' + esc(v) + '</td>';
      })
      .join('');
    body.appendChild(tr);
  });
}
function populateAudit() {
  var roots = classItems(),
    items = testItems();
  normalizeScenarioAnnotations();
  var total = items.length,
    scenarios = {},
    classes = {},
    packages = {},
    tags = {},
    classCounts = countStatuses(roots),
    scenarioCounts = countStatuses(items);
  items.forEach(function (item) {
    var c = testClass(item);
    classes[c] = (classes[c] || 0) + 1;
    var identity = c + ' · ' + testName(item);
    scenarios[identity] = (scenarios[identity] || 0) + 1;
    var packageName = testPackage(item);
    packages[packageName] = (packages[packageName] || 0) + 1;
    testTags(item).forEach(function (tag) {
      tags[tag] = (tags[tag] || 0) + 1;
    });
  });
  auditRows('atf-audit-scenarios', scenarios, true, total, true);
  auditRows('atf-audit-packages', packages, true, total, false);
  auditRows('atf-audit-tags', tags, false, total, false);
  q('.dashboard-view .card-body').forEach(function (body) {
    var label = text(body, 'p'),
      h = body.querySelector('h3');
    if (!h) return;
    if (label === 'Tests Passed') h.textContent = passLikeCount(scenarioCounts);
    if (label === 'Tests Failed') h.textContent = scenarioCounts.fail;
  });
  updateOverviewSummaryFooter(
    overviewChartCard('parent-analysis'),
    classCounts,
    'test classes'
  );
  updateOverviewSummaryFooter(
    overviewChartCard('child-analysis'),
    scenarioCounts,
    'steps'
  );
  var grid = document.getElementById('atf-execution-metadata-grid');
  if (grid) {
    grid.innerHTML = [
      ['Test classes in this run', roots.length],
      ['Scenarios in this run', total],
      ['Scenario pass', scenarioCounts.pass],
      ['Scenario fail', scenarioCounts.fail],
      ['Scenario skipped', scenarioCounts.skip],
      ['Scenario warnings', scenarioCounts.warning]
    ]
      .map(function (pair) {
        return (
          '<div class="atf-secondary-metric"><small>' +
          esc(pair[0]) +
          '</small><strong>' +
          esc(pair[1]) +
          '</strong></div>'
        );
      })
      .join('');
  }
}
