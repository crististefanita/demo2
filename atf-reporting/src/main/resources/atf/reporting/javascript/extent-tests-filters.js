var atfFilterState = {
  ${FILTER_STATE_STATUS}: '',
  ${FILTER_STATE_TAG}: '',
};
function ${FILTER_MATCHES}(item) {
  return (
    (!atfFilterState.${FILTER_STATE_STATUS} ||
      ${STATUS_OF}(item) ===
        atfFilterState.${FILTER_STATE_STATUS}) &&
    (!atfFilterState.${FILTER_STATE_TAG} ||
      ${TEST_TAGS}(item).some(function (tag) {
        return tag.toLowerCase() === atfFilterState.${FILTER_STATE_TAG};
      }))
  );
}
function ${REFRESH_FILTER_SELECTION}() {
  ${QUERY_ALL}(${FILTER_ITEMS}).forEach(function (item) {
    var value = String(
        item.getAttribute(${STATUS_ATTRIBUTE}) ||
          ${TEXT}(item, ''),
      )
        .trim()
        .toLowerCase(),
      selected =
        item.getAttribute(${STATUS_ATTRIBUTE}) === ${CLEAR_VALUE}
          ? !atfFilterState.${FILTER_STATE_STATUS}
          : item.closest(${STATUS_MENU})
            ? value === atfFilterState.${FILTER_STATE_STATUS}
            : value === atfFilterState.${FILTER_STATE_TAG};
    item.classList.toggle(${ACTIVE_CLASS}, !!selected);
    item.setAttribute(${ARIA_PRESSED_ATTRIBUTE}, String(!!selected));
  });
}
function ${APPLY_TEST_FILTERS}() {
  var selected = document.querySelector(${ACTIVE_TEST}),
    selectedVisible = false,
    firstVisible = null;
  ${CLASS_ITEMS}().forEach(function (root) {
    var scenarios = ${IS_CLASS_ROOT}(root)
        ? ${DIRECT_SCENARIO_CARDS}(root)
        : [root],
      visible = 0,
      rootStatusMatches =
        !atfFilterState.${FILTER_STATE_STATUS} ||
        ${STATUS_OF}(root) ===
          atfFilterState.${FILTER_STATE_STATUS};
    scenarios.forEach(function (card) {
      var match = rootStatusMatches && ${FILTER_MATCHES}(card);
      card.style.setProperty(
        ${DISPLAY_PROPERTY},
        match ? ${EMPTY_VALUE} : ${NONE_VALUE},
        ${IMPORTANT_PRIORITY}
      );
      card.setAttribute(${FILTER_HIDDEN_ATTRIBUTE}, String(!match));
      if (match) visible++;
    });
    var rootVisible = visible > 0;
    root.style.setProperty(
      ${DISPLAY_PROPERTY},
      rootVisible ? ${EMPTY_VALUE} : ${NONE_VALUE},
      ${IMPORTANT_PRIORITY}
    );
    root.setAttribute(${FILTER_HIDDEN_ATTRIBUTE}, String(!rootVisible));
    if (root === selected) selectedVisible = rootVisible;
    if (rootVisible && !firstVisible) firstVisible = root;
  });
  if (!selectedVisible && firstVisible) {
    var detail = firstVisible.querySelector(${TEST_DETAIL});
    if (detail && detail.click) detail.click();
  }
  ${REFRESH_FILTER_SELECTION}();
  ${AGGREGATE_CLASS_DURATIONS}();
  ${CAPTURE_DOM_CONTRACT}();
}
function ${SET_TEST_FILTER}(kind, value) {
  var normalized = String(value || '')
    .trim()
    .toLowerCase();
  atfFilterState[kind] =
    value === ${CLEAR_VALUE}
      ? ${EMPTY_VALUE}
      : atfFilterState[kind] === normalized
        ? ${EMPTY_VALUE}
        : normalized;
  ${APPLY_TEST_FILTERS}();
}
function ${BIND_TEST_FILTERS}() {
  ${QUERY_ALL}(${FILTER_ITEMS}).forEach(function (item) {
    if (item.getAttribute(${FILTER_BOUND_ATTRIBUTE})) return;
    item.setAttribute(${FILTER_BOUND_ATTRIBUTE}, ${TRUE_VALUE});
    item.addEventListener(
      ${CLICK_EVENT},
      function (e) {
        e.preventDefault();
        e.stopPropagation();
        var status = item.getAttribute(${STATUS_ATTRIBUTE});
        if (status) {
          ${SET_TEST_FILTER}('${FILTER_STATE_STATUS}', status);
        } else {
          ${SET_TEST_FILTER}(
            '${FILTER_STATE_TAG}',
            ${TEXT}(item).toLowerCase()
          );
        }
        return false;
      },
      true
    );
  });
}
function ${NORMALIZE_TAG_FILTER}() {
  var excluded = { ${EXCLUDED_TAGS} };
  ${QUERY_ALL}(${TAG_FILTER_ITEMS}).forEach(function (item) {
    var value = String(item.textContent || "")
      .trim()
      .toLowerCase();
    if (excluded[value]) item.remove();
  });
}
