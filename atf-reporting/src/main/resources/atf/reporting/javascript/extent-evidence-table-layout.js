function validationCellLength(value) {
  return String(value == null ? '' : value)
    .replace(/\s+/g, ' ')
    .trim().length;
}
function validationClamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}
function evidenceTableCellLength(cell) {
  return validationCellLength(cell && cell.textContent);
}
function evidenceTableIntrinsicWidth(cell) {
  var value = String(cell && cell.textContent ? cell.textContent : '')
      .replace(/\s+/g, ' ')
      .trim(),
    style = cell && window.getComputedStyle ? window.getComputedStyle(cell) : null,
    canvas = document.createElement ? document.createElement('canvas') : null,
    context = canvas && canvas.getContext ? canvas.getContext('2d') : null;
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
function evidenceTableIsReservedColumn(table, index, count) {
  var header = evidenceTableHeader(table, index);
  return (
    /(^|\b)(result|status|outcome)(\b|$)/.test(header) ||
    /(^|\b)(time|duration|attempt|attempts)(\b|$)/.test(header)
  );
}
function evidenceTableReservedWidth(table, index, count, longest, intrinsic) {
  var header = evidenceTableHeader(table, index);
  var tableWidth = table && table.clientWidth ? table.clientWidth : 0;
  var measuredWidth = intrinsic && intrinsic[index] ? intrinsic[index] : 0;
  var measuredPercent = tableWidth
    ? (measuredWidth * 100) / tableWidth + 1.5
    : 0;
  if (/(^|\b)(result|status|outcome)(\b|$)/.test(header)) {
    return validationClamp(
      Math.max(count === 4 ? 8.5 : 9.5, measuredPercent),
      count === 4 ? 8.5 : 9.5,
      24
    );
  }
  if (/(^|\b)(time|duration)(\b|$)/.test(header)) {
    return validationClamp(Math.max(10.5, measuredPercent), 10.5, 24);
  }
  if (/(^|\b)(attempt|attempts)(\b|$)/.test(header)) {
    return validationClamp(Math.max(10, measuredPercent), 10, 24);
  }
  return validationClamp(8 + (longest[index] || 0) * 0.05, 8, 14);
}
function evidenceTableWidthWeights(intrinsic, longest, contentIndexes) {
  return contentIndexes.map(function (index) {
    /* Keep the maximum measured value visible without letting it consume all space. */
    return validationClamp(
      intrinsic[index] || (longest[index] || 0) * 7.2 + 16,
      24,
      560
    );
  });
}
function evidenceTableContentMinimumWidth(table, index, count) {
  var header = evidenceTableHeader(table, index);
  var semanticMinimum;
  if (/(^|\b)action(\b|$)/.test(header)) semanticMinimum = count >= 5 ? 18 : 20;
  else if (/(^|\b)target(\b|$)/.test(header)) semanticMinimum = count >= 5 ? 15 : 18;
  else if (/(^|\b)(assertion|check|field)(\b|$)/.test(header)) semanticMinimum = 18;
  else if (/(^|\b)(expected|actual|observed|value)(\b|$)/.test(header)) semanticMinimum = count >= 5 ? 13 : 18;
  else semanticMinimum = count <= 3 ? 16 : 13;
  var headerCell = table && table.tHead && table.tHead.rows
    && table.tHead.rows[0] && table.tHead.rows[0].cells
    ? table.tHead.rows[0].cells[index] : null,
    headerWidth = evidenceTableIntrinsicWidth(headerCell),
    tableWidth = table && table.clientWidth ? table.clientWidth : 0,
    headerPercent = tableWidth ? (headerWidth * 100) / tableWidth + 1.5 : 0;
  return Math.max(semanticMinimum, headerPercent);
}
function evidenceTableReservedMaximumWidth(table, index, count) {
  var header = evidenceTableHeader(table, index);
  if (/(^|\b)(result|status|outcome)(\b|$)/.test(header)) {
    return count === 4 ? 13 : 14;
  }
  if (/(^|\b)(time|duration)(\b|$)/.test(header)) return 15;
  if (/(^|\b)(attempt|attempts)(\b|$)/.test(header)) return 14;
  return 16;
}
function evidenceTableDistributeReservedSlack(
  widths,
  reservedIndexes,
  table,
  count
) {
  var total = widths.reduce(function (sum, value) {
      return sum + (value || 0);
    }, 0),
    remaining = Math.max(0, 100 - total),
    activeIndexes = reservedIndexes.slice();
  for (var pass = 0; pass < reservedIndexes.length && remaining > 0.01; pass++) {
    var capacities = activeIndexes.map(function (index) {
        return Math.max(
          0,
          evidenceTableReservedMaximumWidth(table, index, count)
            - (widths[index] || 0)
        );
      }),
      capacityTotal = capacities.reduce(function (sum, value) {
        return sum + value;
      }, 0);
    if (!capacityTotal) break;
    var distributed = 0;
    activeIndexes.forEach(function (index, position) {
      var addition = Math.min(
        capacities[position],
        remaining * capacities[position] / capacityTotal
      );
      widths[index] += addition;
      distributed += addition;
    });
    remaining -= distributed;
    activeIndexes = activeIndexes.filter(function (index) {
      return evidenceTableReservedMaximumWidth(table, index, count)
        - widths[index] > 0.01;
    });
    if (distributed < 0.01) break;
  }
  return widths;
}
function evidenceTableAllocateWidths(
  intrinsic,
  longest,
  reservedIndexes,
  count,
  table
) {
  var widths = [],
    contentIndexes = [],
    reservedTotal = 0;
  for (var index = 0; index < count; index++) {
    if (reservedIndexes.indexOf(index) >= 0) {
      widths[index] = evidenceTableReservedWidth(
        table,
        index,
        count,
        longest,
        intrinsic
      );
      reservedTotal += widths[index];
    } else {
      contentIndexes.push(index);
      widths[index] = 0;
    }
  }
  if (!contentIndexes.length) return widths;
  var maximum = contentIndexes.length <= 3 ? 52 : 44,
    minimums = contentIndexes.map(function (index) {
      return evidenceTableContentMinimumWidth(table, index, count);
    }),
    minimumTotal = minimums.reduce(function (sum, value) {
      return sum + value;
    }, 0);
  if (reservedTotal > Math.max(0, 100 - minimumTotal)) {
    var reservedScale = Math.max(0, 100 - minimumTotal)
      / Math.max(1, reservedTotal);
    reservedIndexes.forEach(function (index) {
      widths[index] *= reservedScale;
    });
    reservedTotal = widths.reduce(function (sum, value) {
      return sum + (value || 0);
    }, 0);
  }
  var available = Math.max(0, 100 - reservedTotal);
  if (minimumTotal > available) {
    var minimumScale = available / Math.max(1, minimumTotal);
    minimums = minimums.map(function (value) {
      return value * minimumScale;
    });
  }
  contentIndexes.forEach(function (index, position) {
    widths[index] = 0;
  });
  var weights = evidenceTableWidthWeights(intrinsic, longest, contentIndexes);
  var activeIndexes = contentIndexes.slice(),
    remainingAvailable = available;
  for (var pass = 0; pass < contentIndexes.length + 1; pass++) {
    if (!activeIndexes.length) break;
    var activeWeight = activeIndexes.reduce(function (sum, index) {
        return sum + weights[contentIndexes.indexOf(index)];
      }, 0),
      constrained = false;
    activeIndexes.slice().forEach(function (index) {
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
      activeIndexes.forEach(function (index) {
        var position = contentIndexes.indexOf(index);
        widths[index] = remainingAvailable * weights[position]
          / Math.max(1, activeWeight);
      });
      break;
    }
    remainingAvailable = Math.max(0, remainingAvailable);
  }
  evidenceTableDistributeReservedSlack(widths, reservedIndexes, table, count);
  var total = widths.reduce(function (sum, value) {
    return sum + value;
  }, 0);
  return total
    ? widths.map(function (value) {
        return (value * 100) / total;
      })
    : widths;
}
var ATF_EVIDENCE_TABLE_SELECTOR =
  'table.atf-ui-action-table,' +
  'table.atf-ui-observation-table,' +
  'table.assertions,' +
  'table.atf-setup-evidence-table,' +
  'table.helm-evidence-table,' +
  'table.helm-validation-table';

function normalizeEvidenceTableColumns() {
  q(ATF_EVIDENCE_TABLE_SELECTOR).forEach(function (table) {
    var rows = table.rows ? Array.prototype.slice.call(table.rows) : [],
      count = rows.length ? rows[0].cells.length : 0;
    if (count < 2) return;
    var longest = [],
      intrinsic = [];
    rows.forEach(function (row) {
      Array.prototype.slice
        .call(row.cells || [])
        .forEach(function (cell, index) {
          longest[index] = Math.max(
            longest[index] || 0,
            evidenceTableCellLength(cell)
          );
          intrinsic[index] = Math.max(
            intrinsic[index] || 0,
            evidenceTableIntrinsicWidth(cell)
          );
        });
    });
    var reservedIndexes = [];
    for (var index = 0; index < count; index++) {
      if (evidenceTableIsReservedColumn(table, index, count)) {
        reservedIndexes.push(index);
      }
    }
    var widths = evidenceTableAllocateWidths(
      intrinsic,
      longest,
      reservedIndexes,
      count,
      table
    );
    rows.forEach(function (row) {
      Array.prototype.slice
        .call(row.cells || [])
        .forEach(function (cell, index) {
          var width = widths[index];
          if (width)
            cell.style.setProperty(
              'width',
              width.toFixed(2) + '%',
              'important'
            );
        });
    });
    table.setAttribute('data-atf-content-layout', 'dynamic-longest-value');
    table.setAttribute('data-atf-content-column-count', String(count));
    table.setAttribute(
      'data-atf-content-column-max-px',
      intrinsic
        .map(function (value) {
          return Math.round(value || 0) + 'px';
        })
        .join('|')
    );
    table.setAttribute(
      'data-atf-content-column-widths',
      widths
        .map(function (value) {
          return value.toFixed(2) + '%';
        })
        .join('|')
    );
    if (table.classList.contains('helm-validation-table')) {
      table.setAttribute('data-atf-validation-layout', 'dynamic-longest-value');
      table.setAttribute(
        'data-atf-validation-column-widths',
        widths
          .map(function (value) {
            return value.toFixed(2) + '%';
          })
          .join('|')
      );
    }
  });
}
function normalizeValidationTableColumns() {
  normalizeEvidenceTableColumns();
}
function bindValidationTableLayout() {
  normalizeValidationTableColumns();
  if (window.__atfValidationTableLayoutBound) return;
  window.__atfValidationTableLayoutBound = true;
  var schedule = function () {
    clearTimeout(window.__atfValidationTableLayoutTimer);
    window.__atfValidationTableLayoutTimer = setTimeout(
      normalizeValidationTableColumns,
      0
    );
  };
  window.addEventListener('resize', function () {
    schedule();
  });
  if (window.MutationObserver && document.body) {
    new MutationObserver(schedule).observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['open', 'class']
    });
  }
}
setTimeout(bindValidationTableLayout, 0);
