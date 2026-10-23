function validationCellLength(value) {
  return String(value == null ? '' : value)
    .replace(/\s+/g, ' ')
    .trim().length;
}
function validationClamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}
var evidenceTableMeasureContext = document.createElement('canvas').getContext('2d'),
  EVIDENCE_STATUS_COLUMN = /(^|\b)(result|status|outcome)(\b|$)/,
  EVIDENCE_TIME_COLUMN = /(^|\b)(time|duration)(\b|$)/,
  EVIDENCE_ATTEMPT_COLUMN = /(^|\b)(attempt|attempts)(\b|$)/;
function evidenceTableCellLength(cell) {
  return validationCellLength(cell && cell.textContent);
}
function evidenceTableIntrinsicWidth(cell) {
  var value = String(cell && cell.textContent ? cell.textContent : '')
      .replace(/\s+/g, ' ')
      .trim(),
    style = cell && window.getComputedStyle ? window.getComputedStyle(cell) : null,
    context = evidenceTableMeasureContext;
  if (!value) return 0;
  if (!context) return validationClamp(value.length * 7.2 + 16, 24, 560);
  context.font = style && style.font ? style.font : '12px sans-serif';
  return validationClamp(context.measureText(value).width + 16, 24, 560);
}
function evidenceTableHeader(table, index) {
  var header = table.tHead && table.tHead.rows && table.tHead.rows[0];
  return String(header && header.cells[index] ? header.cells[index].textContent : '')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}
function isKeyValue(headers) {
  return headers.length === 2 && headers[0] === 'field' && headers[1] === 'value';
}
function evidenceTableColumnKind(header) {
  return EVIDENCE_STATUS_COLUMN.test(header) ? 1
    : EVIDENCE_TIME_COLUMN.test(header) ? 2
      : EVIDENCE_ATTEMPT_COLUMN.test(header) ? 3 : 0;
}
function evidenceTableReservedWidth(kind, count, index, longest, intrinsic, tableWidth) {
  var measuredPercent = tableWidth && intrinsic[index]
    ? (intrinsic[index] * 100) / tableWidth + 1.5 : 0;
  if (kind === 1) {
    return validationClamp(measuredPercent, count === 4 ? 8.5 : 9.5, 24);
  }
  if (kind === 2) {
    return validationClamp(Math.max(10.5, measuredPercent), 10.5, 24);
  }
  if (kind === 3) {
    return validationClamp(measuredPercent, 10, 24);
  }
  return validationClamp(8 + (longest[index] || 0) * 0.05, 8, 14);
}
function evidenceTableWidthWeights(intrinsic, longest, contentIndexes) {
  return contentIndexes.map(index =>
    validationClamp(intrinsic[index] || (longest[index] || 0) * 7.2 + 16, 24, 560));
}
function evidenceTableContentMinimumWidth(header, count, headerWidth, tableWidth) {
  var semanticMinimum;
  if (/(^|\b)action(\b|$)/.test(header)) semanticMinimum = count >= 5 ? 18 : 20;
  else if (/(^|\b)target(\b|$)/.test(header)) semanticMinimum = count >= 5 ? 15 : 18;
  else if (/(^|\b)(assertion|check|field)(\b|$)/.test(header)) semanticMinimum = 18;
  else if (/(^|\b)(expected|actual|observed|value)(\b|$)/.test(header)) semanticMinimum = count >= 5 ? 13 : 18;
  else semanticMinimum = count <= 3 ? 16 : 13;
  var headerPercent = tableWidth ? (headerWidth * 100) / tableWidth + 1.5 : 0;
  return Math.max(semanticMinimum, headerPercent);
}
function evidenceTableReservedMaximumWidth(kind, count) {
  if (kind === 1) {
    return count === 4 ? 13 : 14;
  }
  if (kind === 2) return 15;
  if (kind === 3) return 14;
  return 16;
}
function evidenceTableDistributeReservedSlack(widths, reservedIndexes, columnKinds, count) {
  var total = widths.reduce((sum, value) => sum + (value || 0), 0),
    remaining = Math.max(0, 100 - total),
    capacities = reservedIndexes.map(index => Math.max(0,
      evidenceTableReservedMaximumWidth(columnKinds[index], count) - (widths[index] || 0))),
    capacityTotal = capacities.reduce((sum, value) => sum + value, 0);
  // Proportional capacity allocation exhausts either slack or capacity in one pass.
  if (remaining > 0.01 && capacityTotal) reservedIndexes.forEach((index, position) => {
    widths[index] += Math.min(capacities[position], remaining * capacities[position] / capacityTotal);
  });
  return widths;
}
function evidenceTableAllocateWidths(intrinsic, longest, reservedIndexes, count, table, headers, headerWidths, columnKinds) {
  var indexes = Array.from({ length: count }, (_, index) => index),
    contentIndexes = indexes.filter(index => reservedIndexes.indexOf(index) < 0),
    widths = indexes.map(index => reservedIndexes.indexOf(index) < 0
      ? 0 : evidenceTableReservedWidth(columnKinds[index], count, index, longest,
        intrinsic, table.clientWidth)),
    reservedTotal = widths.reduce((sum, value) => sum + value, 0);
  if (!contentIndexes.length) return widths;
  var maximum = contentIndexes.length <= 3 ? 52 : 44,
    minimums = contentIndexes.map(index => evidenceTableContentMinimumWidth(headers[index],
      count, headerWidths[index], table.clientWidth)),
    minimumTotal = minimums.reduce((sum, value) => sum + value, 0);
  if (reservedTotal > Math.max(0, 100 - minimumTotal)) {
    var reservedScale = Math.max(0, 100 - minimumTotal)
      / Math.max(1, reservedTotal);
    reservedIndexes.forEach(index => { widths[index] *= reservedScale; });
    reservedTotal = widths.reduce((sum, value) => sum + (value || 0), 0);
  }
  var available = Math.max(0, 100 - reservedTotal);
  if (minimumTotal > available) {
    var minimumScale = available / Math.max(1, minimumTotal);
    minimums = minimums.map(value => value * minimumScale);
  }
  if (isKeyValue(headers)) {
    var k = contentIndexes[0],
      v = contentIndexes[1],
      tableWidth = table.clientWidth,
      keyWidth = validationClamp(Math.max(20,
        tableWidth ? intrinsic[0] * 100 / tableWidth + 1.5 : 28), 20, 38);
    keyWidth = Math.min(keyWidth, Math.max(20, available - 62));
    widths[k] = keyWidth;
    widths[v] = Math.max(0, available - keyWidth);
    return widths;
  }
  var weights = evidenceTableWidthWeights(intrinsic, longest, contentIndexes);
  var activeIndexes = contentIndexes.slice(),
    remainingAvailable = available;
  for (var pass = 0; pass < contentIndexes.length + 1; pass++) {
    if (!activeIndexes.length) break;
    var activeWeight = activeIndexes.reduce((sum, index) => sum + weights[contentIndexes.indexOf(index)], 0),
      constrained = false;
    activeIndexes.slice().forEach(index => {
      var position = contentIndexes.indexOf(index),
        proposed = remainingAvailable * weights[position] / Math.max(1, activeWeight),
        minimum = minimums[position];
      if (proposed < minimum) {
        widths[index] = minimum;
        remainingAvailable -= minimum;
        activeIndexes.splice(activeIndexes.indexOf(index), 1);
        constrained = true;
      } else if (proposed > maximum) {
        widths[index] = maximum;
        remainingAvailable -= maximum;
        activeIndexes.splice(activeIndexes.indexOf(index), 1);
        constrained = true;
      }
    });
    if (!constrained) {
      activeIndexes.forEach(index => {
        var position = contentIndexes.indexOf(index);
        widths[index] = remainingAvailable * weights[position]
          / Math.max(1, activeWeight);
      });
      break;
    }
    remainingAvailable = Math.max(0, remainingAvailable);
  }
  evidenceTableDistributeReservedSlack(widths, reservedIndexes, columnKinds, count);
  var total = widths.reduce((sum, value) => sum + value, 0);
  return total ? widths.map(value => value * 100 / total) : widths;
}
var ATF_EVIDENCE_TABLE_SELECTOR =
  'table.atf-ui-action-table,' +
  'table.atf-ui-observation-table,' +
  'table.assertions,' +
  'table.atf-setup-evidence-table,' +
  'table.helm-evidence-table,' +
  'table.helm-validation-table';
var ATF_EVIDENCE_TABLE_LAYOUT_CACHE = '__atfEvidenceTableLayout';

function evidenceTableCandidates(root) {
  if (!root) return q(ATF_EVIDENCE_TABLE_SELECTOR);
  if (Array.isArray(root)) return root;
  if (root.matches && root.matches(ATF_EVIDENCE_TABLE_SELECTOR)) return [root];
  return root.querySelectorAll
    ? Array.from(root.querySelectorAll(ATF_EVIDENCE_TABLE_SELECTOR)) : [];
}
function evidenceTableFingerprint(table, count) {
  var style = window.getComputedStyle ? window.getComputedStyle(table) : null,
    font = style
      ? [style.fontFamily, style.fontSize, style.fontWeight, style.lineHeight].join('/')
      : '';
  var parts = [String(count), String(table.clientWidth || 0), font];
  Array.from(table.rows || []).forEach(row => {
    Array.from(row.cells || []).forEach(cell => {
      parts.push(String(cell.textContent || '').replace(/\s+/g, ' ').trim());
    });
  });
  return parts.join('|');
}
function evidenceTableSetAttribute(table, name, value) {
  if (table.getAttribute(name) !== value) table.setAttribute(name, value);
}
function evidenceTableCache(table) {
  return table[ATF_EVIDENCE_TABLE_LAYOUT_CACHE]
    || (table[ATF_EVIDENCE_TABLE_LAYOUT_CACHE] = {});
}
function evidenceTablesFromMutation(record) {
  var tables = new Set();
  var add = function (node) {
    if (!node || node.nodeType !== 1) return;
    if (node.matches && node.matches(ATF_EVIDENCE_TABLE_SELECTOR)) tables.add(node);
    if (node.closest) {
      var table = node.closest(ATF_EVIDENCE_TABLE_SELECTOR);
      if (table) tables.add(table);
    }
    if (node.querySelectorAll) {
      node.querySelectorAll(ATF_EVIDENCE_TABLE_SELECTOR).forEach(table => tables.add(table));
    }
  };
  add(record.target);
  Array.from(record.addedNodes || []).forEach(add);
  return Array.from(tables);
}

function normalizeEvidenceTableColumns(root) {
  evidenceTableCandidates(root).forEach(table => {
    // Legacy/failure adapter: inspect only the data table's parent; Java markup owns new viewports.
    var parent = table.parentElement;
    if (parent && !parent.matches('${TABLE_VIEWPORTS}')) {
      var viewport = document.createElement('div');
      viewport.className = '${DATA_TABLE_VIEWPORT}';
      viewport.tabIndex = 0;
      parent.insertBefore(viewport, table);
      viewport.appendChild(table);
    } else if (parent && !parent.hasAttribute('tabindex')) {
      parent.tabIndex = 0;
    }
    var rows = Array.from(table.rows || []),
      count = rows.length ? rows[0].cells.length : 0;
    if (count < 2) return;
    var headers = Array.from({ length: count }, (_, index) => evidenceTableHeader(table, index)),
      columnKinds = headers.map(evidenceTableColumnKind),
      headerRow = table.tHead && table.tHead.rows[0],
      headerWidths = headers.map((_, index) => evidenceTableIntrinsicWidth(
        headerRow && headerRow.cells[index])),
      tableWidth = table.clientWidth;
    var cache = evidenceTableCache(table),
      fingerprint = evidenceTableFingerprint(table, count);
    if (cache.fingerprint === fingerprint) return;
    var longest = [],
      intrinsic = [];
    rows.forEach(row => {
      Array.from(row.cells || []).forEach((cell, index) => {
        longest[index] = Math.max(longest[index] || 0, evidenceTableCellLength(cell));
        intrinsic[index] = Math.max(intrinsic[index] || 0, evidenceTableIntrinsicWidth(cell));
      });
    });
    var reservedIndexes = [];
    for (var index = 0; index < count; index++) {
      if (columnKinds[index]) reservedIndexes.push(index);
    }
    var widths = evidenceTableAllocateWidths(intrinsic, longest, reservedIndexes, count,
      table, headers, headerWidths, columnKinds);
    var widthSignature = widths
        .map(value => value.toFixed(2) + '%')
        .join('|'),
      maxSignature = intrinsic
        .map(value => Math.round(value || 0) + 'px')
        .join('|');
    if (cache.widthSignature !== widthSignature) rows.forEach(row => {
      Array.from(row.cells || []).forEach((cell, index) => {
        var width = widths[index];
        if (width) cell.style.setProperty('width', width.toFixed(2) + '%', 'important');
      });
    });
    evidenceTableSetAttribute(table, 'data-atf-content-layout', 'dynamic-longest-value');
    evidenceTableSetAttribute(table, 'data-atf-content-column-count', String(count));
    if (isKeyValue(headers)) {
      evidenceTableSetAttribute(table, 'data-atf-key-value-layout', 'field-value');
      evidenceTableSetAttribute(table, 'data-atf-key-column-width', widths[0].toFixed(2) + '%');
    }
    evidenceTableSetAttribute(table, 'data-atf-content-column-max-px', maxSignature);
    evidenceTableSetAttribute(table, 'data-atf-content-column-widths', widthSignature);
    if (table.classList.contains('helm-validation-table')) {
      evidenceTableSetAttribute(table, 'data-atf-validation-layout', 'dynamic-longest-value');
      evidenceTableSetAttribute(table, 'data-atf-validation-column-widths', widthSignature);
    }
    cache.fingerprint = fingerprint;
    cache.widthSignature = widthSignature;
    cache.maxSignature = maxSignature;
  });
}
function normalizeValidationTableColumns(root) {
  normalizeEvidenceTableColumns(root);
}
function bindValidationTableLayout() {
  normalizeValidationTableColumns();
  if (window.__atfValidationTableLayoutBound) return;
  window.__atfValidationTableLayoutBound = true;
  var pendingTables = new Set(),
    schedule = function (tables) {
      (tables || q(ATF_EVIDENCE_TABLE_SELECTOR)).forEach(table => {
        pendingTables.add(table);
      });
      if (window.__atfValidationTableLayoutFrame != null) return;
      var flush = function () {
        window.__atfValidationTableLayoutFrame = null;
        var current = Array.from(pendingTables);
        pendingTables.clear();
        normalizeValidationTableColumns(current);
      };
      window.__atfValidationTableLayoutFrame = window.requestAnimationFrame
        ? window.requestAnimationFrame(flush)
        : setTimeout(flush, 0);
  };
  var initialTables = q(ATF_EVIDENCE_TABLE_SELECTOR);
  if (window.ResizeObserver) {
    window.__atfValidationTableResizeObserver = new ResizeObserver(function (entries) {
      var tables = Array.from(entries, entry => entry.target);
      schedule(tables);
    });
    initialTables.forEach(table => {
      window.__atfValidationTableResizeObserver.observe(table);
    });
  } else {
    window.addEventListener('resize', function () { schedule(); });
  }
  if (window.MutationObserver && document.body) {
    new MutationObserver(function (records) {
      var tables = new Set();
      records.forEach(record => {
        evidenceTablesFromMutation(record).forEach(table => {
          tables.add(table);
        });
      });
      if (window.__atfValidationTableResizeObserver) tables.forEach(table => {
        window.__atfValidationTableResizeObserver.observe(table);
      });
      if (tables.size) schedule(Array.from(tables));
    }).observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['open', 'class']
    });
  }
}
setTimeout(bindValidationTableLayout, 0);
