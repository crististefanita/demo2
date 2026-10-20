function q(s, c) {
  return Array.prototype.slice.call((c || document).querySelectorAll(s));
}
function esc(v) {
  return String(v == null ? '' : v).replace(/[&<>"']/g, function (c) {
    return {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    }[c];
  });
}
function text(e, s) {
  var n = s ? e && e.querySelector(s) : e;
  return n ? String(n.textContent || '').trim() : '';
}
