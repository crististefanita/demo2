(function () {
  if (window.__atfTableViewportBound) return;
  window.__atfTableViewportBound = true;
  var surfaces = '${SCROLL_SURFACES}';
  function makeFocusable() {
    document.querySelectorAll(surfaces).forEach(function (viewport) {
      if (!viewport.hasAttribute('tabindex')) viewport.tabIndex = 0;
    });
  }
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', makeFocusable, { once: true });
  } else makeFocusable();
  // Handle only viewport focus; nested controls retain their own keys.
  document.addEventListener('keydown', function (event) {
    var viewport = event.target;
    if (!viewport || !viewport.matches || !viewport.matches(surfaces)
        || event.altKey || event.ctrlKey || event.metaKey || event.isComposing) return;
    var next = viewport.scrollTop;
    switch (event.key) {
      case 'Home': next = 0; break;
      case 'End': next = viewport.scrollHeight; break;
      case 'PageDown': next += viewport.clientHeight; break;
      case 'PageUp': next -= viewport.clientHeight; break;
      case 'ArrowDown': next += 40; break;
      case 'ArrowUp': next -= 40; break;
      case ' ': next += (event.shiftKey ? -1 : 1) * viewport.clientHeight; break;
      default: return;
    }
    event.preventDefault();
    viewport.scrollTop = next;
  }, true);
  // Keep vertical wheel gestures local, ahead of vendor pane listeners.
  window.addEventListener('wheel', function (event) {
    var target = event.target;
    if (!target || !target.closest || event.ctrlKey || event.metaKey || event.altKey || event.shiftKey
        || !event.deltaY || Math.abs(event.deltaX) > Math.abs(event.deltaY)
        || target.closest('pre,input,textarea,select,[contenteditable="true"]')) return;
    var viewport = target.closest(surfaces);
    if (!viewport || viewport.scrollHeight <= viewport.clientHeight) return;
    var unit = event.deltaMode === 2 ? viewport.clientHeight : event.deltaMode === 1 ? 16 : 1;
    event.preventDefault();
    event.stopPropagation();
    viewport.scrollTop += event.deltaY * unit;
  }, { capture: true, passive: false });
}());
