var atfCategoryState = { selectedName: '' };
var atfCategoryFilterState = { status: '', tag: '' };
function categoryItems() {
  return q('#category-view .test-list-wrapper>.test-list-item>.test-item');
}
function categoryStatusValues(item, selector) {
  return q(selector || '.test-contents .info [status],.test-contents tr.tag-test-status[status]', item)
    .map(node => String(node.getAttribute('status') || '').trim().toLowerCase())
    .filter(Boolean);
}
function categoryHasStatus(item, status) {
  var wanted = String(status || '')
      .trim()
      .toLowerCase(),
    values = categoryStatusValues(item);
  return (wanted === 'pass' || wanted === 'fail' || wanted === 'warning' || wanted === 'skip')
    && values.indexOf(wanted) >= 0;
}
function categoryStatus(item) {
  var values = categoryStatusValues(item);
  return ['fail', 'warning', 'skip'].find(status =>
    values.indexOf(status) >= 0) || 'pass';
}
function categoryName(item) {
  return text(item, '.test-detail .name').trim().toLowerCase();
}
function categoryFilterMarkup() {
  var statuses = [['pass', 'Pass', 'success'], ['fail', 'Fail', 'danger'], ['warning', 'Warning', 'warning']];
  var items = statuses.map(item =>
    '<a class="dropdown-item" data-filter-kind="status" data-filter-value="' + item[0] +
    '" href="#"><span>' + item[1] + '</span><span class="status ' + item[2] + '"></span></a>'
  ).join('');
  return '<li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown" aria-label="Filter categories by status"><i class="fa fa-exclamation-circle"></i></a><ul id="category-status-toggle" class="dropdown-menu dropdown-md p-v-0">' + items +
    '<a class="dropdown-item" data-filter-kind="status" data-filter-value="skip" href="#"><span>Skip</span><span class="status warning"></span></a><div class="dropdown-divider"></div><a class="dropdown-item" data-filter-kind="status" data-filter-value="clear" href="#"><span>Clear</span><span class="pull-right"><i class="fa fa-close"></i></span></a></ul></li><li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown" aria-label="Filter categories by tag"><i class="fa fa-tag"></i></a><ul id="category-tag-toggle" class="dropdown-menu dropdown-md p-v-0"></ul></li>';
}
function refreshCategoryFilterOptions() {
  var menu = document.querySelector('#category-tag-toggle');
  if (!menu) return;
  menu.innerHTML =
    '<a class="dropdown-item" data-filter-kind="tag" data-filter-value="clear" href="#">All categories</a>';
  Array.from(new Set(categoryItems().map(categoryName).filter(Boolean)))
    .sort()
    .forEach(value => {
      var item = document.createElement('a');
      item.className = 'dropdown-item';
      item.href = '#';
      item.setAttribute('data-filter-kind', 'tag');
      item.setAttribute('data-filter-value', value);
      item.textContent = value;
      menu.appendChild(item);
    });
}
function refreshCategoryFilterSelection() {
  q(
    '#category-status-toggle .dropdown-item,#category-tag-toggle .dropdown-item'
  ).forEach(item => {
    var kind = item.getAttribute('data-filter-kind'),
      value = item.getAttribute('data-filter-value'),
      selected =
        value === 'clear'
          ? !atfCategoryFilterState[kind]
          : value === atfCategoryFilterState[kind];
    item.classList.toggle('active', !!selected);
    item.setAttribute('aria-pressed', String(!!selected));
  });
}
function applyCategoryFilters() {
  var selected = document.querySelector(
      '#category-view .test-list-item>.test-item.active'
    ),
    selectedVisible = false,
    firstVisible = null;
  categoryItems().forEach(item => {
    var match =
      (!atfCategoryFilterState.status ||
        categoryHasStatus(item, atfCategoryFilterState.status)) &&
      (!atfCategoryFilterState.tag ||
        categoryName(item) === atfCategoryFilterState.tag);
    item.style.setProperty('display', match ? '' : 'none', 'important');
    item.setAttribute('data-atf-filter-hidden', String(!match));
    if (item === selected) selectedVisible = match;
    if (match && !firstVisible) firstVisible = item;
  });
  if (!selectedVisible) {
    if (firstVisible) {
      selectCategory(firstVisible);
    } else {
      categoryItems().forEach(item => item.classList.remove('active'));
      atfCategoryState.selectedName = '';
      renderCategoryDetail(null);
    }
  }
  refreshCategoryFilterSelection();
}
function setCategoryFilter(kind, value) {
  var normalized = String(value || '')
    .trim()
    .toLowerCase();
  atfCategoryFilterState[kind] =
    value === 'clear'
      ? ''
      : atfCategoryFilterState[kind] === normalized
        ? ''
        : normalized;
  applyCategoryFilters();
}
function bindCategoryFilters() {
  q(
    '#category-status-toggle .dropdown-item,#category-tag-toggle .dropdown-item'
  ).forEach(item => {
    if (item.getAttribute('data-atf-filter-bound')) return;
    item.setAttribute('data-atf-filter-bound', 'true');
    item.addEventListener(
      'click',
      function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        e.stopPropagation();
        setCategoryFilter(
          item.getAttribute('data-filter-kind'),
          item.getAttribute('data-filter-value')
        );
        return false;
      },
      true
    );
  });
}
function ensureCategoryFilters() {
  var tools = document.querySelector(
      '#category-view>.test-list>.test-list-tools'
    ),
    right = tools && tools.querySelector(':scope>.tools.text-right');
  if (!right) return;
  if (!right.querySelector('#category-status-toggle'))
    right.insertAdjacentHTML('afterbegin', categoryFilterMarkup());
  refreshCategoryFilterOptions();
  bindCategoryFilters();
  applyCategoryFilters();
}
function categoryScenarioIndex(link, id) {
  var root = document.querySelector(
    '#test-view .test-item[test-id="' + id + '"]'
  );
  if (!root) return 0;
  var expected = text(link, '').replace(/\s+/g, ' ').trim(),
    cards = directScenarioCards(root);
  for (var i = 0; i < cards.length; i++) {
    if (
      text(cards[i], '.card-title .node').replace(/\s+/g, ' ').trim() ===
      expected
    )
      return i;
  }
  return 0;
}
function openCategoryTest(id, index) {
  atfOpenTest(id, index);
}
function normalizeCategoryLinks() {
  q('#category-view .test-content-detail a.linked[test-id]').forEach(
    function (link) {
      var id = link.getAttribute('test-id');
      if (!id) return;
      var index = categoryScenarioIndex(link, id);
      link.setAttribute('data-atf-test-id', id);
      link.setAttribute('data-atf-scenario-index', String(index));
      link.setAttribute('href', '#test-id=' + id + '&scenario-index=' + index);
      link.setAttribute('title', 'Open this test in Tests');
      link.onclick = function (e) {
        e.preventDefault();
        e.stopPropagation();
        openCategoryTest(id, index);
        return false;
      };
    }
  );
}
function renderCategoryDetail(item) {
  var detail = document.querySelector(
    '#category-view .test-content-detail .detail-body'
  );
  if (!detail) return;
  detail.innerHTML = '';
  var source = item && item.querySelector(':scope>.test-contents');
  if (!source) return;
  var projection = source.cloneNode(true);
  projection.classList.remove('d-none');
  projection.classList.add('atf-category-detail');
  detail.appendChild(projection);
  var title = projection.querySelector(':scope>.info>h4');
  if (title) title.setAttribute('data-atf-category-title', 'true');
  normalizeCategoryLinks();
}
function selectCategory(item) {
  if (!item) return;
  categoryItems().forEach(candidate => {
    candidate.classList.toggle('active', candidate === item);
  });
  atfCategoryState.selectedName = text(item, '.test-detail .name');
  renderCategoryDetail(item);
}
function bindCategorySelection() {
  categoryItems().forEach(item => {
    if (item.getAttribute('data-atf-category-bound')) return;
    item.setAttribute('data-atf-category-bound', 'true');
    item.addEventListener(
      'click',
      function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        e.stopPropagation();
        selectCategory(item);
      },
      true
    );
  });
}
function ensureCategoryDetail() {
  bindCategorySelection();
  var selected =
    categoryItems().find(function (item) {
      return item.classList.contains('active');
    }) || categoryItems()[0];
  if (selected) selectCategory(selected);
}
