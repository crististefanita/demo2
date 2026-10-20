(function () {
  try {
    window.${RUNTIME_BOOT_FLAG} = 'starting';
    ${RUNTIME_MODULES}
    window.${RUNTIME_READY_FLAG} = true;
    document.documentElement.setAttribute(
      '${RUNTIME_READY_ATTRIBUTE}',
      'true'
    );
  } catch (error) {
    window.${RUNTIME_ERROR_FLAG} = String(
      (error && error.stack) || error
    );
  }
})();
