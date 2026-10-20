function removeUnsupportedOverviewCards() {
  q('#events-analysis').forEach(function (canvas) {
    var column = canvas.closest('.col-md-3'),
      card = canvas.closest('.card');
    (column || card || canvas).remove();
  });
  q('.dashboard-view .card-header .card-title').forEach(function (title) {
    if (
      String(title.textContent || '')
        .trim()
        .toLowerCase() === 'log events'
    ) {
      var card = title.closest('.card'),
        column = card && card.closest('.col-md-3');
      (column || card || title).remove();
    }
  });
}
